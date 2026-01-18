package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RiskCheckService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final BalanceProvider balanceProvider;
    private final RiskLimitPolicy riskLimitPolicy;
    private final Map<String, FrozenRecord> frozenByOrder = new ConcurrentHashMap<>();
    private final Map<String, Object> userLocks = new ConcurrentHashMap<>();
    private final Set<String> processedFills = ConcurrentHashMap.newKeySet();
    private final Map<String, BigDecimal> committedMarginByOrder = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> committedFeeByOrder = new ConcurrentHashMap<>();

    public RiskCheckService() {
        this(new InMemoryBalanceProvider(), new TieredRiskLimitPolicy());
    }

    public RiskCheckService(BalanceProvider balanceProvider, RiskLimitPolicy riskLimitPolicy) {
        this.balanceProvider = Objects.requireNonNull(balanceProvider, "balanceProvider");
        this.riskLimitPolicy = Objects.requireNonNull(riskLimitPolicy, "riskLimitPolicy");
    }

    public RiskResult checkAndFreeze(OrderRequest request) {
        Objects.requireNonNull(request, "request");
        validateRequest(request);
        FrozenRecord existing = frozenByOrder.get(request.orderId());
        if (existing != null) {
            if (!matchesRequest(existing, request)) {
                return RiskResult.reject(RiskErrorCode.INVALID_REQUEST,
                        "orderId already frozen with different attributes");
            }
            return RiskResult.ok(existing);
        }
        Object lock = userLocks.computeIfAbsent(request.userId(), key -> new Object());
        synchronized (lock) {
            existing = frozenByOrder.get(request.orderId());
            if (existing != null) {
                if (!matchesRequest(existing, request)) {
                    return RiskResult.reject(RiskErrorCode.INVALID_REQUEST,
                            "orderId already frozen with different attributes");
                }
                return RiskResult.ok(existing);
            }
            RiskLimitResult limitResult = riskLimitPolicy.evaluate(request);
            if (!limitResult.allowed()) {
                String message = "notional " + limitResult.notional() + " exceeds limit " + limitResult.maxNotional();
                return RiskResult.reject(RiskErrorCode.RISK_LIMIT_EXCEEDED, message);
            }
            BigDecimal required = request.requiredMargin().add(request.requiredFee());
            if (!balanceProvider.tryDebit(request.userId(), required)) {
                return RiskResult.reject(RiskErrorCode.INSUFFICIENT_BALANCE, "insufficient balance");
            }
            FrozenRecord record = new FrozenRecord(
                    request.orderId(),
                    request.userId(),
                    request.symbol(),
                    request.requiredMargin(),
                    request.requiredFee(),
                    request.timestamp(),
                    FreezeStatus.FROZEN
            );
            frozenByOrder.put(request.orderId(), record);
            return RiskResult.ok(record);
        }
    }

    public Optional<FrozenRecord> releaseFreeze(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        FrozenRecord existing = frozenByOrder.get(orderId);
        if (existing == null) {
            return Optional.empty();
        }
        Object lock = userLocks.computeIfAbsent(existing.userId(), key -> new Object());
        synchronized (lock) {
            existing = frozenByOrder.get(orderId);
            if (existing == null) {
                return Optional.empty();
            }
            if (existing.status() != FreezeStatus.FROZEN) {
                return Optional.of(existing);
            }
            BigDecimal committedMargin = committedMarginByOrder.getOrDefault(orderId, ZERO);
            BigDecimal committedFee = committedFeeByOrder.getOrDefault(orderId, ZERO);
            BigDecimal remainingMargin = existing.frozenMargin().subtract(committedMargin);
            BigDecimal remainingFee = existing.frozenFee().subtract(committedFee);
            if (remainingMargin.compareTo(ZERO) < 0 || remainingFee.compareTo(ZERO) < 0) {
                throw new RiskException(RiskErrorCode.INVALID_REQUEST, "committed amounts exceed frozen amounts");
            }
            BigDecimal amount = remainingMargin.add(remainingFee);
            balanceProvider.credit(existing.userId(), amount);
            FrozenRecord released = existing.withStatus(FreezeStatus.RELEASED);
            frozenByOrder.put(orderId, released);
            committedMarginByOrder.remove(orderId);
            committedFeeByOrder.remove(orderId);
            return Optional.of(released);
        }
    }

    public int commitAfterFill(List<TradeFill> fills) {
        Objects.requireNonNull(fills, "fills");
        int committed = 0;
        for (TradeFill fill : fills) {
            FrozenRecord existing = frozenByOrder.get(fill.orderId());
            if (existing == null) {
                continue;
            }
            if (!processedFills.add(fill.fillId())) {
                continue;
            }
            try {
                Object lock = userLocks.computeIfAbsent(existing.userId(), key -> new Object());
                synchronized (lock) {
                    existing = frozenByOrder.get(fill.orderId());
                    if (existing == null || existing.status() != FreezeStatus.FROZEN) {
                        continue;
                    }
                    BigDecimal committedMargin = committedMarginByOrder.getOrDefault(fill.orderId(), ZERO);
                    BigDecimal committedFee = committedFeeByOrder.getOrDefault(fill.orderId(), ZERO);
                    BigDecimal nextCommittedMargin = committedMargin.add(fill.filledMargin());
                    BigDecimal nextCommittedFee = committedFee.add(fill.filledFee());
                    if (nextCommittedMargin.compareTo(existing.frozenMargin()) > 0
                            || nextCommittedFee.compareTo(existing.frozenFee()) > 0) {
                        throw new RiskException(RiskErrorCode.INVALID_REQUEST,
                                "filled amounts exceed frozen amounts");
                    }
                    committedMarginByOrder.put(fill.orderId(), nextCommittedMargin);
                    committedFeeByOrder.put(fill.orderId(), nextCommittedFee);
                    FreezeStatus status = (nextCommittedMargin.compareTo(existing.frozenMargin()) == 0
                            && nextCommittedFee.compareTo(existing.frozenFee()) == 0)
                            ? FreezeStatus.COMMITTED
                            : FreezeStatus.FROZEN;
                    FrozenRecord updated = existing.withStatus(status);
                    frozenByOrder.put(fill.orderId(), updated);
                    if (status == FreezeStatus.COMMITTED) {
                        committedMarginByOrder.remove(fill.orderId());
                        committedFeeByOrder.remove(fill.orderId());
                    }
                    committed++;
                }
            } catch (RuntimeException ex) {
                processedFills.remove(fill.fillId());
                throw ex;
            }
        }
        return committed;
    }

    public void credit(String userId, BigDecimal amount) {
        balanceProvider.credit(userId, amount);
    }

    public BigDecimal balanceOf(String userId) {
        return balanceProvider.available(userId);
    }

    private void validateRequest(OrderRequest request) {
        if (request.requiredMargin().compareTo(ZERO) < 0 || request.requiredFee().compareTo(ZERO) < 0) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "required amounts must be >= 0");
        }
    }

    private boolean matchesRequest(FrozenRecord record, OrderRequest request) {
        return record.userId().equals(request.userId())
                && record.symbol().equals(request.symbol())
                && record.frozenMargin().compareTo(request.requiredMargin()) == 0
                && record.frozenFee().compareTo(request.requiredFee()) == 0;
    }
}
