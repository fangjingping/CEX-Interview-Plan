package com.cex.exchange.demo.marketdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

/**
 * MarketDataBook 核心类。
 */
public class MarketDataBook {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final NavigableMap<BigDecimal, BigDecimal> bids = new TreeMap<>();
    private final NavigableMap<BigDecimal, BigDecimal> asks = new TreeMap<>();

    public void update(OrderSide side, BigDecimal price, BigDecimal deltaQuantity) {
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(deltaQuantity, "deltaQuantity");
        NavigableMap<BigDecimal, BigDecimal> bookSide = side == OrderSide.BID ? bids : asks;
        BigDecimal next = bookSide.getOrDefault(price, ZERO).add(deltaQuantity);
        if (next.compareTo(ZERO) <= 0) {
            bookSide.remove(price);
        } else {
            bookSide.put(price, next);
        }
    }

    public List<PriceLevel> topN(OrderSide side, int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("depth must be > 0");
        }
        NavigableMap<BigDecimal, BigDecimal> bookSide = side == OrderSide.BID ? bids.descendingMap() : asks;
        List<PriceLevel> levels = new ArrayList<>();
        for (var entry : bookSide.entrySet()) {
            levels.add(new PriceLevel(entry.getKey(), entry.getValue()));
            if (levels.size() >= depth) {
                break;
            }
        }
        return levels;
    }
}
