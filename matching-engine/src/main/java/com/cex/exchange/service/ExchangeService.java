package com.cex.exchange.service;

import com.cex.exchange.engine.MatchingEngine;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.OrderType;
import com.cex.exchange.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ExchangeService 核心类。
 */
public class ExchangeService {
    private final MatchingEngine engine;
    private final AtomicLong orderSequence;

    public ExchangeService() {
        this.engine = new MatchingEngine();
        this.orderSequence = new AtomicLong(1);
    }

    public List<Trade> placeLimit(String userId,
                                  String symbol,
                                  OrderSide side,
                                  BigDecimal price,
                                  BigDecimal quantity) {
        Order order = new Order(
                nextOrderId(),
                userId,
                symbol,
                side,
                OrderType.LIMIT,
                price,
                quantity,
                Instant.now().toEpochMilli()
        );
        return engine.place(order);
    }

    public List<Trade> placeMarket(String userId,
                                   String symbol,
                                   OrderSide side,
                                   BigDecimal quantity) {
        Order order = new Order(
                nextOrderId(),
                userId,
                symbol,
                side,
                OrderType.MARKET,
                null,
                quantity,
                Instant.now().toEpochMilli()
        );
        return engine.place(order);
    }

    public MatchingEngine getEngine() {
        return engine;
    }

    private String nextOrderId() {
        return "O" + orderSequence.getAndIncrement();
    }
}
