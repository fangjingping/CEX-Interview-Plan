package com.cex.exchange.demo.pricing;

import com.cex.exchange.demo.position.PositionSide;

import java.math.BigDecimal;
import java.util.Objects;

public record PositionSnapshot(String userId,
                               String symbol,
                               PositionSide side,
                               BigDecimal quantity) {
    public PositionSnapshot {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(quantity, "quantity");
    }
}
