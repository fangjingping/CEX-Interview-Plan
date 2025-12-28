package com.cex.exchange.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Order 核心类。
 */
public class Order {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final String orderId;
    private final String userId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final long timestamp;

    private BigDecimal remainingQuantity;

    public Order(String orderId,
                 String userId,
                 String symbol,
                 OrderSide side,
                 OrderType type,
                 BigDecimal price,
                 BigDecimal quantity,
                 long timestamp) {
        this.orderId = requireText(orderId, "orderId");
        this.userId = requireText(userId, "userId");
        this.symbol = requireText(symbol, "symbol");
        this.side = Objects.requireNonNull(side, "side");
        this.type = Objects.requireNonNull(type, "type");
        this.quantity = requirePositive(quantity, "quantity");
        if (type == OrderType.LIMIT) {
            this.price = requirePositive(price, "price");
        } else {
            this.price = price;
        }
        this.timestamp = timestamp;
        this.remainingQuantity = this.quantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public boolean isFilled() {
        return remainingQuantity.compareTo(ZERO) == 0;
    }

    public void reduce(BigDecimal quantity) {
        requirePositive(quantity, "fillQuantity");
        if (quantity.compareTo(remainingQuantity) > 0) {
            throw new IllegalArgumentException("fillQuantity exceeds remainingQuantity");
        }
        remainingQuantity = remainingQuantity.subtract(quantity);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static BigDecimal requirePositive(BigDecimal value, String field) {
        if (value == null || value.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
