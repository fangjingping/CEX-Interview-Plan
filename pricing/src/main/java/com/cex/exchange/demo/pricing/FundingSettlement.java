package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record FundingSettlement(String symbol,
                                BigDecimal markPrice,
                                FundingRate rate,
                                List<FundingTransfer> transfers,
                                long timestamp) {
    public FundingSettlement {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(markPrice, "markPrice");
        Objects.requireNonNull(rate, "rate");
        Objects.requireNonNull(transfers, "transfers");
        transfers = List.copyOf(transfers);
    }
}
