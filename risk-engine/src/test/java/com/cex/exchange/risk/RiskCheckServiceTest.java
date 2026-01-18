package com.cex.exchange.risk;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskCheckServiceTest {

    @Test
    void freezesForValidOrder() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100"));
        OrderRequest request = new OrderRequest(
                "O1",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                1L
        );

        RiskResult result = service.checkAndFreeze(request);

        assertTrue(result.isOk());
        FrozenRecord record = result.getFrozenRecord();
        assertNotNull(record);
        assertEquals("O1", record.orderId());
        assertEquals("U1", record.userId());
        assertEquals("BTC-USDT", record.symbol());
        assertBigDecimal("10", record.frozenMargin());
        assertBigDecimal("1", record.frozenFee());
        assertEquals(FreezeStatus.FROZEN, record.status());
        assertBigDecimal("89", service.balanceOf("U1"));
    }

    @Test
    void rejectsWhenBalanceInsufficient() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("5"));
        OrderRequest request = new OrderRequest(
                "O2",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                2L
        );

        RiskResult result = service.checkAndFreeze(request);

        assertEquals(false, result.isOk());
        assertEquals(RiskErrorCode.INSUFFICIENT_BALANCE, result.getCode());
    }

    @Test
    void rejectsWhenRiskLimitExceeded() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100000"));
        OrderRequest request = new OrderRequest(
                "O3",
                "U1",
                "BTC-USDT",
                new BigDecimal("10000"),
                new BigDecimal("0"),
                new BigDecimal("10"),
                3L
        );

        RiskResult result = service.checkAndFreeze(request);

        assertEquals(false, result.isOk());
        assertEquals(RiskErrorCode.RISK_LIMIT_EXCEEDED, result.getCode());
    }

    @Test
    void returnsSameRecordForDuplicateOrderId() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100"));
        OrderRequest request = new OrderRequest(
                "O4",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                4L
        );

        RiskResult first = service.checkAndFreeze(request);
        RiskResult second = service.checkAndFreeze(request);

        assertEquals(first.getFrozenRecord(), second.getFrozenRecord());
        assertBigDecimal("89", service.balanceOf("U1"));
    }

    @Test
    void releasesFrozenFunds() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100"));
        OrderRequest request = new OrderRequest(
                "O5",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                5L
        );

        service.checkAndFreeze(request);
        Optional<FrozenRecord> released = service.releaseFreeze("O5");

        assertTrue(released.isPresent());
        assertEquals(FreezeStatus.RELEASED, released.get().status());
        assertBigDecimal("100", service.balanceOf("U1"));
    }

    @Test
    void commitsFrozenFunds() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100"));
        OrderRequest request = new OrderRequest(
                "O6",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                6L
        );

        service.checkAndFreeze(request);
        int committed = service.commitAfterFill(List.of(new TradeFill(
                "F1",
                "O6",
                new BigDecimal("10"),
                new BigDecimal("1"),
                7L
        )));

        assertEquals(1, committed);
        Optional<FrozenRecord> record = service.releaseFreeze("O6");
        assertTrue(record.isPresent());
        assertEquals(FreezeStatus.COMMITTED, record.get().status());
        assertBigDecimal("89", service.balanceOf("U1"));
    }

    @Test
    void rejectsDuplicateOrderIdWithDifferentAttributes() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100"));
        OrderRequest original = new OrderRequest(
                "O6",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                6L
        );
        OrderRequest conflicting = new OrderRequest(
                "O6",
                "U1",
                "BTC-USDT",
                new BigDecimal("12"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                6L
        );

        RiskResult first = service.checkAndFreeze(original);
        RiskResult second = service.checkAndFreeze(conflicting);

        assertTrue(first.isOk());
        assertEquals(RiskErrorCode.INVALID_REQUEST, second.getCode());
    }

    @Test
    void commitsPartialFillAndLeavesRemainingFrozen() {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100"));
        OrderRequest request = new OrderRequest(
                "O9",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                9L
        );

        service.checkAndFreeze(request);
        int committed = service.commitAfterFill(List.of(new TradeFill(
                "F-P1",
                "O9",
                new BigDecimal("4"),
                new BigDecimal("0.4"),
                10L
        )));

        assertEquals(1, committed);
        Optional<FrozenRecord> released = service.releaseFreeze("O9");
        assertTrue(released.isPresent());
        assertEquals(FreezeStatus.RELEASED, released.get().status());
        assertBigDecimal("95.6", service.balanceOf("U1"));
    }

    @Test
    void concurrentFreezeIsIdempotent() throws ExecutionException, InterruptedException {
        RiskCheckService service = new RiskCheckService();
        service.credit("U1", new BigDecimal("100"));
        OrderRequest request = new OrderRequest(
                "O7",
                "U1",
                "BTC-USDT",
                new BigDecimal("10"),
                new BigDecimal("1"),
                new BigDecimal("5"),
                8L
        );

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<RiskResult>> tasks = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                tasks.add(() -> service.checkAndFreeze(request));
            }
            List<Future<RiskResult>> results = executor.invokeAll(tasks);
            FrozenRecord expected = results.get(0).get().getFrozenRecord();
            for (Future<RiskResult> future : results) {
                assertEquals(expected, future.get().getFrozenRecord());
            }
        } finally {
            executor.shutdownNow();
        }

        assertBigDecimal("89", service.balanceOf("U1"));
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
