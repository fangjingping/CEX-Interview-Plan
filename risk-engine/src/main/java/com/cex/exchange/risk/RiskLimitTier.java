package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.Objects;

public record RiskLimitTier(BigDecimal maxLeverage, BigDecimal maxNotional) {
    public RiskLimitTier {
        Objects.requireNonNull(maxLeverage, "maxLeverage");
        Objects.requireNonNull(maxNotional, "maxNotional");
        if (maxLeverage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "maxLeverage must be > 0");
        }
        if (maxNotional.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "maxNotional must be > 0");
        }
    }
}
