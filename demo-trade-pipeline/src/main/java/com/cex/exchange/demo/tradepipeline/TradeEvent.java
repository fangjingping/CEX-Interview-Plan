package com.cex.exchange.demo.tradepipeline;

import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * TradeEvent 核心类。
 */
public class TradeEvent {
    private final String eventId;
    private final String symbol;
    private final OrderSide side;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final String takerUserId;
    private final String makerUserId;
    private final long timestamp;

    public TradeEvent(String eventId,
                      String symbol,
                      OrderSide side,
                      BigDecimal price,
                      BigDecimal quantity,
                      String takerUserId,
                      String makerUserId,
                      long timestamp) {
        this.eventId = requireText(eventId, "eventId");
        this.symbol = requireText(symbol, "symbol");
        this.side = Objects.requireNonNull(side, "side");
        this.price = Objects.requireNonNull(price, "price");
        this.quantity = Objects.requireNonNull(quantity, "quantity");
        this.takerUserId = requireText(takerUserId, "takerUserId");
        this.makerUserId = requireText(makerUserId, "makerUserId");
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getTakerUserId() {
        return takerUserId;
    }

    public String getMakerUserId() {
        return makerUserId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
