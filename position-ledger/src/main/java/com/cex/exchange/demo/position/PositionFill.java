package com.cex.exchange.demo.position;

import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

public record PositionFill(String fillId,
                           String userId,
                           String symbol,
                           OrderSide side,
                           BigDecimal price,
                           BigDecimal quantity,
                           PositionMode mode,
                           long timestamp) {
    public PositionFill {
        if (fillId == null || fillId.isBlank()) {
            throw new IllegalArgumentException("fillId must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(quantity, "quantity");
        Objects.requireNonNull(mode, "mode");
    }
}
