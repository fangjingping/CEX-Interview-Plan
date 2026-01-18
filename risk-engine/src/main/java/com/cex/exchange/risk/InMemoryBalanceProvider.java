package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryBalanceProvider implements BalanceProvider {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    public BigDecimal available(String userId) {
        requireUser(userId);
        return balances.getOrDefault(userId, ZERO);
    }

    @Override
    public boolean tryDebit(String userId, BigDecimal amount) {
        requireUser(userId);
        Objects.requireNonNull(amount, "amount");
        if (amount.compareTo(ZERO) < 0) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "amount must be >= 0");
        }
        Object lock = locks.computeIfAbsent(userId, key -> new Object());
        synchronized (lock) {
            BigDecimal balance = balances.getOrDefault(userId, ZERO);
            if (balance.compareTo(amount) < 0) {
                return false;
            }
            balances.put(userId, balance.subtract(amount));
            return true;
        }
    }

    @Override
    public void credit(String userId, BigDecimal amount) {
        requireUser(userId);
        Objects.requireNonNull(amount, "amount");
        if (amount.compareTo(ZERO) < 0) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "amount must be >= 0");
        }
        if (amount.compareTo(ZERO) == 0) {
            return;
        }
        Object lock = locks.computeIfAbsent(userId, key -> new Object());
        synchronized (lock) {
            balances.put(userId, balances.getOrDefault(userId, ZERO).add(amount));
        }
    }

    private void requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
    }
}
