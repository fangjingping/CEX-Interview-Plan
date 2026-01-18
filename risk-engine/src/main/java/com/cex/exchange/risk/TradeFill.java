package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.Objects;

public record TradeFill(String fillId, String orderId, BigDecimal filledMargin, BigDecimal filledFee, long timestamp) {
    public TradeFill {
        if (fillId == null || fillId.isBlank()) {
            throw new IllegalArgumentException("fillId must not be blank");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        Objects.requireNonNull(filledMargin, "filledMargin");
        Objects.requireNonNull(filledFee, "filledFee");
        if (filledMargin.compareTo(BigDecimal.ZERO) < 0 || filledFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "filled amounts must be >= 0");
        }
    }
}
