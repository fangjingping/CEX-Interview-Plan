package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderRequest(String orderId,
                           String userId,
                           String symbol,
                           BigDecimal requiredMargin,
                           BigDecimal requiredFee,
                           BigDecimal leverage,
                           long timestamp) {
    public OrderRequest {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(requiredMargin, "requiredMargin");
        Objects.requireNonNull(requiredFee, "requiredFee");
        Objects.requireNonNull(leverage, "leverage");
        if (leverage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "leverage must be > 0");
        }
    }

    public BigDecimal notional() {
        return requiredMargin.multiply(leverage);
    }
}
