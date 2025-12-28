package com.cex.exchange.demo.wallet;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WalletServiceTest 单元测试。
 */
class WalletServiceTest {

    @Test
    void depositIsIdempotentByTxId() {
        WalletService service = new WalletService();
        service.deposit("U1", "USDT", new BigDecimal("100"), "TX1");
        service.deposit("U1", "USDT", new BigDecimal("100"), "TX1");

        Balance balance = service.getBalance("U1", "USDT").orElseThrow();
        assertBigDecimal(new BigDecimal("100"), balance.getAvailable());
        assertBigDecimal(BigDecimal.ZERO, balance.getFrozen());
    }

    @Test
    void withdrawalFreezesAndCancels() {
        WalletService service = new WalletService();
        service.deposit("U1", "USDT", new BigDecimal("100"), "TX2");

        service.requestWithdrawal("U1", "USDT", new BigDecimal("30"), "W1");
        Balance balance = service.getBalance("U1", "USDT").orElseThrow();
        assertBigDecimal(new BigDecimal("70"), balance.getAvailable());
        assertBigDecimal(new BigDecimal("30"), balance.getFrozen());

        service.cancelWithdrawal("W1");
        Balance afterCancel = service.getBalance("U1", "USDT").orElseThrow();
        assertBigDecimal(new BigDecimal("100"), afterCancel.getAvailable());
        assertBigDecimal(BigDecimal.ZERO, afterCancel.getFrozen());
    }

    @Test
    void withdrawalConfirmConsumesFrozen() {
        WalletService service = new WalletService();
        service.deposit("U1", "USDT", new BigDecimal("100"), "TX3");

        service.requestWithdrawal("U1", "USDT", new BigDecimal("40"), "W2");
        service.confirmWithdrawal("W2");

        Balance balance = service.getBalance("U1", "USDT").orElseThrow();
        assertBigDecimal(new BigDecimal("60"), balance.getAvailable());
        assertBigDecimal(BigDecimal.ZERO, balance.getFrozen());
        assertEquals(WithdrawalStatus.CONFIRMED, service.getWithdrawal("W2").orElseThrow().status());
    }

    private void assertBigDecimal(BigDecimal expected, BigDecimal actual) {
        assertTrue(actual.compareTo(expected) == 0,
                () -> "Expected " + expected + " but got " + actual);
    }
}
