package com.cex.exchange.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Trade 核心类。
 */
public class Trade {
    private final long tradeId;
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final String takerOrderId;
    private final String makerOrderId;
    private final String takerUserId;
    private final String makerUserId;
    private final long timestamp;

    public Trade(long tradeId,
                 String symbol,
                 BigDecimal price,
                 BigDecimal quantity,
                 String takerOrderId,
                 String makerOrderId,
                 String takerUserId,
                 String makerUserId,
                 long timestamp) {
        this.tradeId = tradeId;
        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.price = Objects.requireNonNull(price, "price");
        this.quantity = Objects.requireNonNull(quantity, "quantity");
        this.takerOrderId = Objects.requireNonNull(takerOrderId, "takerOrderId");
        this.makerOrderId = Objects.requireNonNull(makerOrderId, "makerOrderId");
        this.takerUserId = Objects.requireNonNull(takerUserId, "takerUserId");
        this.makerUserId = Objects.requireNonNull(makerUserId, "makerUserId");
        this.timestamp = timestamp;
    }

    public long getTradeId() {
        return tradeId;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getTakerOrderId() {
        return takerOrderId;
    }

    public String getMakerOrderId() {
        return makerOrderId;
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
}
