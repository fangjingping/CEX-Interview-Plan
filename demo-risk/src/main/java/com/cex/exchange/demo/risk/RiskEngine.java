package com.cex.exchange.demo.risk;

import java.math.BigDecimal;

/**
 * RiskEngine 核心类。
 */
public class RiskEngine {
    private static final BigDecimal MARGIN_CALL_MULTIPLIER = new BigDecimal("1.2");

    public RiskStatus evaluate(Position position) {
        BigDecimal ratio = position.marginRatio();
        BigDecimal maintenance = position.getMaintenanceMarginRate();

        if (ratio.compareTo(maintenance) < 0) {
            return RiskStatus.LIQUIDATE;
        }
        if (ratio.compareTo(maintenance.multiply(MARGIN_CALL_MULTIPLIER)) < 0) {
            return RiskStatus.MARGIN_CALL;
        }
        return RiskStatus.OK;
    }
}
