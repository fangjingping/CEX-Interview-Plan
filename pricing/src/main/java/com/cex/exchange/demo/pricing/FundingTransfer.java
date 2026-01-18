package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Objects;

public record FundingTransfer(String userId,
                              String symbol,
                              BigDecimal amount,
                              BigDecimal fundingRate,
                              BigDecimal markPrice,
                              long timestamp) {
    public FundingTransfer {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(fundingRate, "fundingRate");
        Objects.requireNonNull(markPrice, "markPrice");
    }
}
