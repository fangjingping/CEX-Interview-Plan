package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Objects;

public record FundingRate(String symbol, BigDecimal rate, long timestamp) {
    public FundingRate {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(rate, "rate");
    }
}
