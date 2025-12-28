package com.cex.exchange.demo.risk;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RiskEngineTest 单元测试。
 */
class RiskEngineTest {

    @Test
    void flagsLiquidationWhenBelowMaintenanceMargin() {
        RiskEngine engine = new RiskEngine();
        Position position = new Position(
                new BigDecimal("1"),
                new BigDecimal("100"),
                new BigDecimal("90"),
                new BigDecimal("5"),
                new BigDecimal("0.05")
        );

        assertEquals(RiskStatus.LIQUIDATE, engine.evaluate(position));
    }

    @Test
    void flagsMarginCallWhenNearMaintenanceMargin() {
        RiskEngine engine = new RiskEngine();
        Position position = new Position(
                new BigDecimal("1"),
                new BigDecimal("100"),
                new BigDecimal("95"),
                new BigDecimal("10"),
                new BigDecimal("0.05")
        );

        assertEquals(RiskStatus.MARGIN_CALL, engine.evaluate(position));
    }

    @Test
    void staysOkWhenHealthy() {
        RiskEngine engine = new RiskEngine();
        Position position = new Position(
                new BigDecimal("1"),
                new BigDecimal("100"),
                new BigDecimal("105"),
                new BigDecimal("10"),
                new BigDecimal("0.05")
        );

        assertEquals(RiskStatus.OK, engine.evaluate(position));
    }
}
