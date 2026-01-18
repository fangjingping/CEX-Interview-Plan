package com.cex.exchange.demo.pricing;

import java.util.Objects;

public class FundingRateCalculator {
    private final PricingService pricingService;
    private final PricingPolicy policy;

    public FundingRateCalculator(PricingPolicy policy) {
        this(new PricingService(), policy);
    }

    public FundingRateCalculator(PricingService pricingService, PricingPolicy policy) {
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public FundingRate calculate(MarkPrice markPrice) {
        return pricingService.fundingRate(markPrice, policy);
    }
}
