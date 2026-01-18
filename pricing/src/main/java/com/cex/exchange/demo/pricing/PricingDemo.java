package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;

public class PricingDemo {
    public static void main(String[] args) {
        PricingService service = new PricingService();
        PricingPolicy policy = new PricingPolicy(new BigDecimal("0.05"), new BigDecimal("0.1"), new BigDecimal("0.02"));
        IndexPrice index = new IndexPrice("BTC-USDT", new BigDecimal("100"), System.currentTimeMillis());
        MarkPrice mark = service.markPrice(index, new BigDecimal("103"), policy);
        FundingRate fundingRate = service.fundingRate(mark, policy);

        System.out.println("Mark price: " + mark.price() + ", funding rate: " + fundingRate.rate());
    }
}
