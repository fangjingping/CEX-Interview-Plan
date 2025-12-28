package com.cex.exchange.demo.settlement;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ReconciliationServiceTest 单元测试。
 */
class ReconciliationServiceTest {

    @Test
    void matchesWhenFeesAligned() {
        List<Trade> trades = List.of(
                new Trade("T1", "BTC-USDT", new BigDecimal("100"), new BigDecimal("1"), new BigDecimal("1")),
                new Trade("T2", "BTC-USDT", new BigDecimal("101"), new BigDecimal("1"), new BigDecimal("2"))
        );
        List<FeeEntry> fees = List.of(
                new FeeEntry("T1", new BigDecimal("1")),
                new FeeEntry("T2", new BigDecimal("2"))
        );

        ReconciliationReport report = new ReconciliationService().reconcileFees(trades, fees);
        assertTrue(report.matched());
        assertEquals(0, report.missingFeeTrades().size());
    }

    @Test
    void flagsMissingFeeTrade() {
        List<Trade> trades = List.of(
                new Trade("T1", "BTC-USDT", new BigDecimal("100"), new BigDecimal("1"), new BigDecimal("1")),
                new Trade("T2", "BTC-USDT", new BigDecimal("101"), new BigDecimal("1"), new BigDecimal("2"))
        );
        List<FeeEntry> fees = List.of(new FeeEntry("T1", new BigDecimal("1")));

        ReconciliationReport report = new ReconciliationService().reconcileFees(trades, fees);
        assertFalse(report.matched());
        assertEquals(List.of("T2"), report.missingFeeTrades());
    }
}
