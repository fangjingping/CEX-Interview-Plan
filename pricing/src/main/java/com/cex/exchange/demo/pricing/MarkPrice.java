package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Objects;

public record MarkPrice(String symbol,
                        BigDecimal price,
                        long timestamp,
                        BigDecimal premiumRate,
                        BigDecimal indexPrice) {
    public MarkPrice {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(premiumRate, "premiumRate");
        Objects.requireNonNull(indexPrice, "indexPrice");
    }
}
