package com.cex.exchange.book;

import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * OrderBook 核心类。
 */
public class OrderBook {
    private final String symbol;
    private final NavigableMap<BigDecimal, Deque<Order>> bids;
    private final NavigableMap<BigDecimal, Deque<Order>> asks;

    public OrderBook(String symbol) {
        this.symbol = Objects.requireNonNull(symbol, "symbol");
        this.bids = new TreeMap<>();
        this.asks = new TreeMap<>();
    }

    public String getSymbol() {
        return symbol;
    }

    public NavigableMap<BigDecimal, Deque<Order>> getBids() {
        return bids;
    }

    public NavigableMap<BigDecimal, Deque<Order>> getAsks() {
        return asks;
    }

    public Optional<BigDecimal> bestBid() {
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.lastKey());
    }

    public Optional<BigDecimal> bestAsk() {
        return asks.isEmpty() ? Optional.empty() : Optional.of(asks.firstKey());
    }

    public void add(Order order) {
        Objects.requireNonNull(order, "order");
        if (!symbol.equals(order.getSymbol())) {
            throw new IllegalArgumentException("order symbol mismatch");
        }
        NavigableMap<BigDecimal, Deque<Order>> bookSide =
                order.getSide() == OrderSide.BUY ? bids : asks;
        BigDecimal price = order.getPrice();
        if (price == null) {
            throw new IllegalArgumentException("limit order price required");
        }
        bookSide.computeIfAbsent(price, key -> new ArrayDeque<>()).addLast(order);
    }
}
