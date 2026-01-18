package com.cex.exchange.demo.liquidation;

import java.math.BigDecimal;

public class LiquidationDemo {
    public static void main(String[] args) {
        LiquidationEngine engine = new LiquidationEngine();
        LiquidationRequest request = new LiquidationRequest(
                "R1",
                "U1",
                "BTC-USDT",
                new BigDecimal("1"),
                new BigDecimal("100"),
                new BigDecimal("80"),
                new BigDecimal("5"),
                new BigDecimal("0.05"),
                System.currentTimeMillis()
        );

        LiquidationDecision decision = engine.evaluate(request);
        System.out.println("Decision: " + decision.status() + ", order: " + decision.order());
    }
}
