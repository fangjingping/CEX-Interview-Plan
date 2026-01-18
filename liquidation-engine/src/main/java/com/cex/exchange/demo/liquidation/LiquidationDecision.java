package com.cex.exchange.demo.liquidation;

import com.cex.exchange.demo.risk.RiskStatus;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public record LiquidationDecision(String requestId,
                                  RiskStatus status,
                                  BigDecimal marginRatio,
                                  Optional<LiquidationOrder> order) {
    public LiquidationDecision {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(marginRatio, "marginRatio");
        Objects.requireNonNull(order, "order");
    }
}
