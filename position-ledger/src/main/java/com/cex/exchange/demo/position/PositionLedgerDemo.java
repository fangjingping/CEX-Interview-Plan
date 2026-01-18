package com.cex.exchange.demo.position;

import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;

public class PositionLedgerDemo {
    public static void main(String[] args) {
        PositionService service = new PositionService();
        PositionKey key = new PositionKey("U1", "BTC-USDT");

        service.applyTrade(new PositionTrade("T1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("2"), System.currentTimeMillis()));
        service.applyTrade(new PositionTrade("T2", key, OrderSide.SELL,
                new BigDecimal("110"), new BigDecimal("1"), System.currentTimeMillis()));

        service.getPosition(key).ifPresent(position -> System.out.println("Position: " + position));
    }
}
