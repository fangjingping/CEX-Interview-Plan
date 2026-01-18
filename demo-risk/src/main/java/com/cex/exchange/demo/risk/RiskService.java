package com.cex.exchange.demo.risk;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RiskService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final Map<String, MarginBalance> balances = new ConcurrentHashMap<>();
    private final Map<String, RiskHold> holds = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> holdFills = new ConcurrentHashMap<>();
    private final Map<String, Object> userLocks = new ConcurrentHashMap<>();

    public void deposit(String userId, BigDecimal amount) {
        String resolvedUserId = requireText(userId, "userId");
        validatePositive(amount, "amount");
        Object lock = userLocks.computeIfAbsent(resolvedUserId, key -> new Object());
        synchronized (lock) {
            MarginBalance balance = balances.getOrDefault(resolvedUserId, MarginBalance.ZERO);
            balances.put(resolvedUserId, balance.addAvailable(amount));
        }
    }

    public MarginBalance getBalance(String userId) {
        String resolvedUserId = requireText(userId, "userId");
        return balances.getOrDefault(resolvedUserId, MarginBalance.ZERO);
    }

    public RiskHold freeze(String requestId, String userId, BigDecimal amount) {
        String resolvedRequestId = requireText(requestId, "requestId");
        String resolvedUserId = requireText(userId, "userId");
        validatePositive(amount, "amount");
        RiskHold existing = holds.get(resolvedRequestId);
        if (existing != null) {
            if (!existing.userId().equals(resolvedUserId) || existing.reserved().compareTo(amount) != 0) {
                throw new RiskException(RiskErrorCode.REQUEST_MISMATCH, "request already exists with different payload");
            }
            return existing;
        }
        Object lock = userLocks.computeIfAbsent(resolvedUserId, key -> new Object());
        synchronized (lock) {
            existing = holds.get(resolvedRequestId);
            if (existing != null) {
                if (!existing.userId().equals(resolvedUserId) || existing.reserved().compareTo(amount) != 0) {
                    throw new RiskException(RiskErrorCode.REQUEST_MISMATCH, "request already exists with different payload");
                }
                return existing;
            }
            MarginBalance balance = balances.getOrDefault(resolvedUserId, MarginBalance.ZERO);
            if (balance.available().compareTo(amount) < 0) {
                throw new RiskException(RiskErrorCode.INSUFFICIENT_AVAILABLE, "insufficient available margin");
            }
            balances.put(resolvedUserId, balance.freeze(amount));
            RiskHold hold = new RiskHold(resolvedRequestId, resolvedUserId, amount, ZERO, RiskHoldStatus.FROZEN);
            holds.put(resolvedRequestId, hold);
            holdFills.put(resolvedRequestId, ConcurrentHashMap.newKeySet());
            return hold;
        }
    }

    public RiskHold consume(String requestId, String fillId, BigDecimal amount) {
        String resolvedRequestId = requireText(requestId, "requestId");
        String resolvedFillId = requireText(fillId, "fillId");
        validatePositive(amount, "amount");
        RiskHold hold = holds.get(resolvedRequestId);
        if (hold == null) {
            throw new RiskException(RiskErrorCode.HOLD_NOT_FOUND, "hold not found");
        }
        Object lock = userLocks.computeIfAbsent(hold.userId(), key -> new Object());
        synchronized (lock) {
            hold = holds.get(resolvedRequestId);
            if (hold == null) {
                throw new RiskException(RiskErrorCode.HOLD_NOT_FOUND, "hold not found");
            }
            if (hold.status() == RiskHoldStatus.RELEASED) {
                throw new RiskException(RiskErrorCode.HOLD_ALREADY_RELEASED, "hold already released");
            }
            Set<String> fills = holdFills.computeIfAbsent(resolvedRequestId, key -> ConcurrentHashMap.newKeySet());
            if (!fills.add(resolvedFillId)) {
                return hold;
            }
            BigDecimal remaining = hold.remaining();
            if (remaining.compareTo(amount) < 0) {
                fills.remove(resolvedFillId);
                throw new RiskException(RiskErrorCode.INSUFFICIENT_RESERVED, "amount exceeds remaining reserve");
            }
            MarginBalance balance = balances.getOrDefault(hold.userId(), MarginBalance.ZERO);
            if (balance.frozen().compareTo(amount) < 0) {
                fills.remove(resolvedFillId);
                throw new RiskException(RiskErrorCode.INSUFFICIENT_RESERVED, "frozen margin is insufficient");
            }
            balances.put(hold.userId(), balance.consume(amount));
            BigDecimal consumed = hold.consumed().add(amount);
            RiskHoldStatus status = consumed.compareTo(hold.reserved()) == 0
                    ? RiskHoldStatus.COMMITTED
                    : hold.status();
            RiskHold updated = new RiskHold(hold.requestId(), hold.userId(), hold.reserved(), consumed, status);
            holds.put(resolvedRequestId, updated);
            return updated;
        }
    }

    public RiskHold release(String requestId) {
        String resolvedRequestId = requireText(requestId, "requestId");
        RiskHold hold = holds.get(resolvedRequestId);
        if (hold == null) {
            throw new RiskException(RiskErrorCode.HOLD_NOT_FOUND, "hold not found");
        }
        Object lock = userLocks.computeIfAbsent(hold.userId(), key -> new Object());
        synchronized (lock) {
            hold = holds.get(resolvedRequestId);
            if (hold == null) {
                throw new RiskException(RiskErrorCode.HOLD_NOT_FOUND, "hold not found");
            }
            if (hold.status() != RiskHoldStatus.FROZEN) {
                return hold;
            }
            BigDecimal remaining = hold.remaining();
            if (remaining.compareTo(ZERO) > 0) {
                MarginBalance balance = balances.getOrDefault(hold.userId(), MarginBalance.ZERO);
                balances.put(hold.userId(), balance.release(remaining));
            }
            RiskHold released = new RiskHold(hold.requestId(), hold.userId(), hold.reserved(), hold.consumed(),
                    RiskHoldStatus.RELEASED);
            holds.put(resolvedRequestId, released);
            return released;
        }
    }

    private void validatePositive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field);
        if (value.compareTo(ZERO) <= 0) {
            throw new RiskException(RiskErrorCode.INVALID_AMOUNT, field + " must be > 0");
        }
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
