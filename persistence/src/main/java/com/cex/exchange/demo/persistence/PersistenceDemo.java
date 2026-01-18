package com.cex.exchange.demo.persistence;

import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionState;
import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;

public class PersistenceDemo {
    public static void main(String[] args) {
        PositionPersistenceService service = new PositionPersistenceService();
        PositionKey key = new PositionKey("U1", "BTC-USDT");

        service.record(new PositionEvent("E1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), System.currentTimeMillis()));
        PositionState recovered = service.recover(key);
        service.saveSnapshot(new PositionSnapshot(key, recovered, 1L, System.currentTimeMillis()));

        System.out.println("Recovered position: " + recovered);
    }
}
