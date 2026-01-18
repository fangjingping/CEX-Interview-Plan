package com.cex.exchange.demo.liquidation;

import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

public record LiquidationOrder(String orderId,
                               String userId,
                               String symbol,
                               OrderSide side,
                               BigDecimal price,
                               BigDecimal quantity,
                               long timestamp) {
    public LiquidationOrder {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank");
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
    }
}
