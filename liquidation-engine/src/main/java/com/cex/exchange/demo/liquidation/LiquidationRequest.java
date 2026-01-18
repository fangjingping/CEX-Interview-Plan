package com.cex.exchange.demo.liquidation;

import java.math.BigDecimal;
import java.util.Objects;

public record LiquidationRequest(String requestId,
                                 String userId,
                                 String symbol,
                                 BigDecimal positionQuantity,
                                 BigDecimal entryPrice,
                                 BigDecimal markPrice,
                                 BigDecimal margin,
                                 BigDecimal maintenanceMarginRate,
                                 long timestamp) {
    public LiquidationRequest {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(positionQuantity, "positionQuantity");
        Objects.requireNonNull(entryPrice, "entryPrice");
        Objects.requireNonNull(markPrice, "markPrice");
        Objects.requireNonNull(margin, "margin");
        Objects.requireNonNull(maintenanceMarginRate, "maintenanceMarginRate");
    }
}
