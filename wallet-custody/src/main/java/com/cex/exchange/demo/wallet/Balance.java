package com.cex.exchange.demo.wallet;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Balance 核心类。
 */
public class Balance {
    private BigDecimal available;
    private BigDecimal frozen;

    public Balance() {
        this.available = BigDecimal.ZERO;
        this.frozen = BigDecimal.ZERO;
    }

    public synchronized BigDecimal getAvailable() {
        return available;
    }

    public synchronized BigDecimal getFrozen() {
        return frozen;
    }

    public synchronized void credit(BigDecimal amount) {
        available = available.add(requirePositive(amount));
    }

    public synchronized void freeze(BigDecimal amount) {
        BigDecimal value = requirePositive(amount);
        if (available.compareTo(value) < 0) {
            throw new IllegalArgumentException("insufficient available balance");
        }
        available = available.subtract(value);
        frozen = frozen.add(value);
    }

    public synchronized void unfreeze(BigDecimal amount) {
        BigDecimal value = requirePositive(amount);
        if (frozen.compareTo(value) < 0) {
            throw new IllegalArgumentException("insufficient frozen balance");
        }
        frozen = frozen.subtract(value);
        available = available.add(value);
    }

    public synchronized void debitFrozen(BigDecimal amount) {
        BigDecimal value = requirePositive(amount);
        if (frozen.compareTo(value) < 0) {
            throw new IllegalArgumentException("insufficient frozen balance");
        }
        frozen = frozen.subtract(value);
    }

    private BigDecimal requirePositive(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        return amount;
    }
}
