package com.cex.exchange.demo.risk;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Position 核心类。
 */
public class Position {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int RATIO_SCALE = 8;

    private final BigDecimal quantity;
    private final BigDecimal entryPrice;
    private final BigDecimal markPrice;
    private final BigDecimal margin;
    private final BigDecimal maintenanceMarginRate;

    public Position(BigDecimal quantity,
                    BigDecimal entryPrice,
                    BigDecimal markPrice,
                    BigDecimal margin,
                    BigDecimal maintenanceMarginRate) {
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.markPrice = markPrice;
        this.margin = margin;
        this.maintenanceMarginRate = maintenanceMarginRate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public BigDecimal getMarkPrice() {
        return markPrice;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public BigDecimal getMaintenanceMarginRate() {
        return maintenanceMarginRate;
    }

    public BigDecimal notional() {
        return markPrice.multiply(quantity.abs());
    }

    public BigDecimal unrealizedPnl() {
        return markPrice.subtract(entryPrice).multiply(quantity);
    }

    public BigDecimal equity() {
        return margin.add(unrealizedPnl());
    }

    public BigDecimal marginRatio() {
        BigDecimal notional = notional();
        if (notional.compareTo(ZERO) == 0) {
            return ZERO;
        }
        return equity().divide(notional, RATIO_SCALE, RoundingMode.HALF_UP);
    }
}
