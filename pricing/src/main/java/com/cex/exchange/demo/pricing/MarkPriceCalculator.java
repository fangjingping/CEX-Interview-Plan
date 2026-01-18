package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class MarkPriceCalculator {
    private final PriceSource priceSource;
    private final PricingService pricingService;
    private final PricingPolicy policy;

    public MarkPriceCalculator(PriceSource priceSource, PricingPolicy policy) {
        this(priceSource, new PricingService(), policy);
    }

    public MarkPriceCalculator(PriceSource priceSource, PricingService pricingService, PricingPolicy policy) {
        this.priceSource = Objects.requireNonNull(priceSource, "priceSource");
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public Optional<MarkPrice> calculate(String symbol) {
        Optional<IndexPrice> indexPrice = priceSource.getIndexPrice(symbol);
        if (indexPrice.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal lastTrade = priceSource.getLastTradePrice(symbol)
                .orElse(indexPrice.get().price());
        return Optional.of(pricingService.markPrice(indexPrice.get(), lastTrade, policy));
    }
}
