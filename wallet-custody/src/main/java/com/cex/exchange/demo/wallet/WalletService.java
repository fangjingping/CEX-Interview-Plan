package com.cex.exchange.demo.wallet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WalletService 核心类。
 */
public class WalletService {
    private final Map<WalletKey, Balance> balances = new ConcurrentHashMap<>();
    private final Set<String> processedDeposits = ConcurrentHashMap.newKeySet();
    private final Map<String, Withdrawal> withdrawals = new ConcurrentHashMap<>();

    public void deposit(String userId, String asset, BigDecimal amount, String txId) {
        requireId(txId, "txId");
        if (!processedDeposits.add(txId)) {
            return;
        }
        Balance balance = balances.computeIfAbsent(new WalletKey(userId, asset), key -> new Balance());
        balance.credit(amount);
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
}
