package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.Objects;

public record RiskLimitResult(boolean allowed, BigDecimal maxNotional, BigDecimal notional) {
    public RiskLimitResult {
        Objects.requireNonNull(maxNotional, "maxNotional");
        Objects.requireNonNull(notional, "notional");
    }

    public static RiskLimitResult allowed(BigDecimal maxNotional, BigDecimal notional) {
        return new RiskLimitResult(true, maxNotional, notional);
    }

    public static RiskLimitResult rejected(BigDecimal maxNotional, BigDecimal notional) {
        return new RiskLimitResult(false, maxNotional, notional);
    }
}
