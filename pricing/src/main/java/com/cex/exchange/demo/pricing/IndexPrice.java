package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Objects;

public record IndexPrice(String symbol, BigDecimal price, long timestamp) {
    public IndexPrice {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(price, "price");
    }
}
