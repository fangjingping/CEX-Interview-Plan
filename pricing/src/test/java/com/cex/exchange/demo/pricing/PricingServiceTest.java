package com.cex.exchange.demo.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingServiceTest {

    @Test
    void clampsPremiumAndMarkDeviation() {
        PricingService service = new PricingService();
        PricingPolicy policy = new PricingPolicy(new BigDecimal("0.05"), new BigDecimal("0.1"), new BigDecimal("0.02"));
        IndexPrice index = new IndexPrice("BTC-USDT", new BigDecimal("100"), 1L);

        MarkPrice mark = service.markPrice(index, new BigDecimal("120"), policy);

        assertBigDecimal("108", mark.price());
        assertBigDecimal("0.05", mark.premiumRate());
    }

    @Test
    void fundingRateIsCapped() {
        PricingService service = new PricingService();
        PricingPolicy policy = new PricingPolicy(new BigDecimal("0.1"), new BigDecimal("0.2"), new BigDecimal("0.02"));
        MarkPrice mark = new MarkPrice("BTC-USDT", new BigDecimal("100"), 1L, new BigDecimal("0.05"),
                new BigDecimal("100"));

        FundingRate rate = service.fundingRate(mark, policy);

        assertBigDecimal("0.02", rate.rate());
    }

    @Test
    void rejectsInvalidPrice() {
        PricingService service = new PricingService();
        PricingPolicy policy = new PricingPolicy(new BigDecimal("0.05"), new BigDecimal("0.1"), new BigDecimal("0.02"));
        IndexPrice index = new IndexPrice("BTC-USDT", new BigDecimal("0"), 1L);

        PricingException ex = assertThrows(PricingException.class,
                () -> service.markPrice(index, new BigDecimal("100"), policy));
        assertEquals(PricingErrorCode.INVALID_PRICE, ex.getErrorCode());
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
