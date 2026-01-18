package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Objects;

public record PricingPolicy(BigDecimal maxPremiumRate,
                            BigDecimal maxMarkDeviation,
                            BigDecimal fundingRateCap) {
    public PricingPolicy {
        Objects.requireNonNull(maxPremiumRate, "maxPremiumRate");
        Objects.requireNonNull(maxMarkDeviation, "maxMarkDeviation");
        Objects.requireNonNull(fundingRateCap, "fundingRateCap");
    }
}
