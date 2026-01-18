package com.cex.exchange.demo.position;

import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

public record PositionTrade(String tradeId,
                            PositionKey key,
                            OrderSide side,
                            BigDecimal price,
                            BigDecimal quantity,
                            long timestamp) {
    public PositionTrade {
        if (tradeId == null || tradeId.isBlank()) {
            throw new IllegalArgumentException("tradeId must not be blank");
        }
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(quantity, "quantity");
    }
}
