package com.cex.exchange.demo.liquidation;

import com.cex.exchange.demo.risk.RiskStatus;
import com.cex.exchange.model.OrderSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquidationEngineTest {

    @Test
    void createsOrderWhenBelowMaintenance() {
        LiquidationEngine engine = new LiquidationEngine();
        LiquidationRequest request = new LiquidationRequest(
                "R1",
                "U1",
                "BTC-USDT",
                new BigDecimal("1"),
                new BigDecimal("100"),
                new BigDecimal("80"),
                new BigDecimal("5"),
                new BigDecimal("0.05"),
                1L
        );

        LiquidationDecision decision = engine.evaluate(request);

        assertEquals(RiskStatus.LIQUIDATE, decision.status());
        assertTrue(decision.order().isPresent());
        LiquidationOrder order = decision.order().get();
        assertEquals(OrderSide.SELL, order.side());
        assertBigDecimal("1", order.quantity());
    }

    @Test
    void returnsOkForZeroPosition() {
        LiquidationEngine engine = new LiquidationEngine();
        LiquidationRequest request = new LiquidationRequest(
                "R2",
                "U1",
                "BTC-USDT",
                new BigDecimal("0"),
                new BigDecimal("100"),
                new BigDecimal("100"),
                new BigDecimal("5"),
                new BigDecimal("0.05"),
                2L
        );

        LiquidationDecision decision = engine.evaluate(request);

        assertEquals(RiskStatus.OK, decision.status());
        assertTrue(decision.order().isEmpty());
    }

    @Test
    void evaluatesIdempotently() {
        LiquidationEngine engine = new LiquidationEngine();
        LiquidationRequest request = new LiquidationRequest(
                "R3",
                "U1",
                "BTC-USDT",
                new BigDecimal("1"),
                new BigDecimal("100"),
                new BigDecimal("90"),
                new BigDecimal("20"),
                new BigDecimal("0.05"),
                3L
        );

        LiquidationDecision first = engine.evaluate(request);
        LiquidationDecision second = engine.evaluate(request);

        assertEquals(first, second);
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
