package com.cex.exchange.demo.wallet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WalletService 核心类。
 */
public class WalletService {
    private static final long DEFAULT_DEPOSIT_TTL_MILLIS = 86_400_000L;

    private final Map<WalletKey, Balance> balances = new ConcurrentHashMap<>();
    private final Map<String, Long> depositExpiresAt = new ConcurrentHashMap<>();
    private final Map<String, Withdrawal> withdrawals = new ConcurrentHashMap<>();
    private final TimeSource timeSource;
    private final long depositTtlMillis;

    public WalletService() {
        this(new SystemTimeSource(), DEFAULT_DEPOSIT_TTL_MILLIS);
    }

    public WalletService(TimeSource timeSource, long depositTtlMillis) {
        this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
        if (depositTtlMillis <= 0) {
            throw new IllegalArgumentException("depositTtlMillis must be > 0");
        }
        this.depositTtlMillis = depositTtlMillis;
    }

    public void deposit(String userId, String asset, BigDecimal amount, String txId) {
        requireId(txId, "txId");
        long now = timeSource.nowMillis();
        if (!markDeposit(txId, now)) {
            return;
        }
        Balance balance = balances.computeIfAbsent(new WalletKey(userId, asset), key -> new Balance());
        balance.credit(amount);
        cleanupExpiredDeposits(now);
    }

    public String requestWithdrawal(String userId, String asset, BigDecimal amount, String withdrawalId) {
        requireId(withdrawalId, "withdrawalId");
        return withdrawals.compute(withdrawalId, (id, existing) -> {
            if (existing != null) {
                return existing;
            }
            Balance balance = balances.computeIfAbsent(new WalletKey(userId, asset), key -> new Balance());
            balance.freeze(amount);
            return new Withdrawal(id, userId, asset, amount, WithdrawalStatus.REQUESTED);
        }).withdrawalId();
    }

    public void confirmWithdrawal(String withdrawalId) {
        requireId(withdrawalId, "withdrawalId");
        withdrawals.compute(withdrawalId, (id, existing) -> {
            if (existing == null) {
                throw new IllegalArgumentException("withdrawal not found");
            }
            if (existing.status() == WithdrawalStatus.CONFIRMED) {
                return existing;
            }
            if (existing.status() == WithdrawalStatus.CANCELLED) {
                throw new IllegalStateException("withdrawal already cancelled");
            }
            Balance balance = balances.get(new WalletKey(existing.userId(), existing.asset()));
            if (balance == null) {
                throw new IllegalStateException("balance not found");
            }
            balance.debitFrozen(existing.amount());
            return new Withdrawal(id, existing.userId(), existing.asset(), existing.amount(), WithdrawalStatus.CONFIRMED);
        });
    }

    public void cancelWithdrawal(String withdrawalId) {
        requireId(withdrawalId, "withdrawalId");
        withdrawals.compute(withdrawalId, (id, existing) -> {
            if (existing == null) {
                throw new IllegalArgumentException("withdrawal not found");
            }
            if (existing.status() == WithdrawalStatus.CANCELLED) {
                return existing;
            }
            if (existing.status() == WithdrawalStatus.CONFIRMED) {
                throw new IllegalStateException("withdrawal already confirmed");
            }
            Balance balance = balances.get(new WalletKey(existing.userId(), existing.asset()));
            if (balance == null) {
                throw new IllegalStateException("balance not found");
            }
            balance.unfreeze(existing.amount());
            return new Withdrawal(id, existing.userId(), existing.asset(), existing.amount(), WithdrawalStatus.CANCELLED);
        });
    }

    public Optional<Balance> getBalance(String userId, String asset) {
        return Optional.ofNullable(balances.get(new WalletKey(userId, asset)));
    }

    public Optional<Withdrawal> getWithdrawal(String withdrawalId) {
        return Optional.ofNullable(withdrawals.get(withdrawalId));
    }

    private void requireId(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private boolean markDeposit(String txId, long nowMillis) {
        AtomicBoolean accepted = new AtomicBoolean(false);
        depositExpiresAt.compute(txId, (key, existing) -> {
            if (existing == null || existing <= nowMillis) {
                accepted.set(true);
                return nowMillis + depositTtlMillis;
            }
            return existing;
        });
        return accepted.get();
    }

    private void cleanupExpiredDeposits(long nowMillis) {
        for (Map.Entry<String, Long> entry : depositExpiresAt.entrySet()) {
            if (entry.getValue() <= nowMillis) {
                depositExpiresAt.remove(entry.getKey(), entry.getValue());
            }
        }
    }
}
