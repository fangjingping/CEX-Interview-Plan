package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class PricingService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final int RATE_SCALE = 8;

    public MarkPrice markPrice(IndexPrice indexPrice, BigDecimal lastTradePrice, PricingPolicy policy) {
        Objects.requireNonNull(indexPrice, "indexPrice");
        Objects.requireNonNull(lastTradePrice, "lastTradePrice");
        Objects.requireNonNull(policy, "policy");
        validatePositive(indexPrice.price(), "indexPrice");
        validatePositive(lastTradePrice, "lastTradePrice");
        validatePolicy(policy);

        BigDecimal premiumRate = lastTradePrice.subtract(indexPrice.price())
                .divide(indexPrice.price(), RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal clampedPremium = clamp(premiumRate, policy.maxPremiumRate());
        BigDecimal rawMark = indexPrice.price().multiply(ONE.add(clampedPremium));
        BigDecimal lower = lastTradePrice.multiply(ONE.subtract(policy.maxMarkDeviation()));
        BigDecimal upper = lastTradePrice.multiply(ONE.add(policy.maxMarkDeviation()));
        BigDecimal mark = clamp(rawMark, lower, upper);

        return new MarkPrice(indexPrice.symbol(), mark, indexPrice.timestamp(), clampedPremium, indexPrice.price());
    }

    public FundingRate fundingRate(MarkPrice markPrice, PricingPolicy policy) {
        Objects.requireNonNull(markPrice, "markPrice");
        Objects.requireNonNull(policy, "policy");
        validatePolicy(policy);
        BigDecimal clamped = clamp(markPrice.premiumRate(), policy.fundingRateCap());
        return new FundingRate(markPrice.symbol(), clamped, markPrice.timestamp());
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal bound) {
        if (bound.compareTo(ZERO) < 0) {
            throw new PricingException(PricingErrorCode.INVALID_POLICY, "bound must be >= 0");
        }
        BigDecimal min = bound.negate();
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(bound) > 0) {
            return bound;
        }
        return value;
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        if (value.compareTo(max) > 0) {
            return max;
        }
        return value;
    }

    private void validatePositive(BigDecimal value, String field) {
        if (value.compareTo(ZERO) <= 0) {
            throw new PricingException(PricingErrorCode.INVALID_PRICE, field + " must be > 0");
        }
    }

    private void validatePolicy(PricingPolicy policy) {
        if (policy.maxPremiumRate().compareTo(ZERO) < 0
                || policy.maxMarkDeviation().compareTo(ZERO) < 0
                || policy.fundingRateCap().compareTo(ZERO) < 0) {
            throw new PricingException(PricingErrorCode.INVALID_POLICY, "policy values must be >= 0");
        }
    }
}
