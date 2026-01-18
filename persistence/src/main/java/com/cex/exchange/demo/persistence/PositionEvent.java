package com.cex.exchange.demo.persistence;

import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

public record PositionEvent(String eventId,
                            PositionKey key,
                            OrderSide side,
                            BigDecimal price,
                            BigDecimal quantity,
                            long timestamp) {
    public PositionEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(quantity, "quantity");
        if (price.compareTo(BigDecimal.ZERO) <= 0 || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_EVENT, "price and quantity must be > 0");
        }
    }
}
