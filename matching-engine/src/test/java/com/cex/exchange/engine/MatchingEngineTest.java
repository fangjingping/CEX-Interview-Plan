package com.cex.exchange.engine;

import com.cex.exchange.book.OrderBook;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.OrderType;
import com.cex.exchange.model.Trade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MatchingEngineTest 单元测试。
 */
class MatchingEngineTest {

    @Test
    void matchesLimitOrderPartially() {
        MatchingEngine engine = new MatchingEngine();
        Order sell = new Order("S1", "U1", "BTC-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("100"), new BigDecimal("10"), 1L);
        engine.place(sell);

        Order buy = new Order("B1", "U2", "BTC-USDT", OrderSide.BUY, OrderType.LIMIT,
                new BigDecimal("105"), new BigDecimal("5"), 2L);
        List<Trade> trades = engine.place(buy);

        assertEquals(1, trades.size());
        Trade trade = trades.get(0);
        assertBigDecimal(new BigDecimal("100"), trade.getPrice());
        assertBigDecimal(new BigDecimal("5"), trade.getQuantity());

        OrderBook book = engine.getOrderBook("BTC-USDT").orElseThrow();
        Deque<Order> asks = book.getAsks().get(new BigDecimal("100"));
        assertNotNull(asks);
        Order remaining = asks.peekFirst();
        assertNotNull(remaining);
        assertBigDecimal(new BigDecimal("5"), remaining.getRemainingQuantity());
    }

    @Test
    void respectsFifoAtSamePrice() {
        MatchingEngine engine = new MatchingEngine();
        Order sell1 = new Order("S1", "U1", "ETH-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("200"), new BigDecimal("3"), 1L);
        Order sell2 = new Order("S2", "U2", "ETH-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("200"), new BigDecimal("4"), 2L);
        engine.place(sell1);
        engine.place(sell2);

        Order buy = new Order("B1", "U3", "ETH-USDT", OrderSide.BUY, OrderType.LIMIT,
                new BigDecimal("210"), new BigDecimal("5"), 3L);
        List<Trade> trades = engine.place(buy);

        assertEquals(2, trades.size());
        assertEquals("S1", trades.get(0).getMakerOrderId());
        assertEquals("S2", trades.get(1).getMakerOrderId());

        OrderBook book = engine.getOrderBook("ETH-USDT").orElseThrow();
        Deque<Order> asks = book.getAsks().get(new BigDecimal("200"));
        assertNotNull(asks);
        Order remaining = asks.peekFirst();
        assertNotNull(remaining);
        assertEquals("S2", remaining.getOrderId());
        assertBigDecimal(new BigDecimal("2"), remaining.getRemainingQuantity());
    }

    @Test
    void marketOrderConsumesBestPrices() {
        MatchingEngine engine = new MatchingEngine();
        engine.place(new Order("S1", "U1", "SOL-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("30"), new BigDecimal("1"), 1L));
        engine.place(new Order("S2", "U2", "SOL-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("31"), new BigDecimal("2"), 2L));

        Order marketBuy = new Order("B1", "U3", "SOL-USDT", OrderSide.BUY, OrderType.MARKET,
                null, new BigDecimal("3"), 3L);
        List<Trade> trades = engine.place(marketBuy);

        assertEquals(2, trades.size());
        assertBigDecimal(new BigDecimal("30"), trades.get(0).getPrice());
        assertBigDecimal(new BigDecimal("1"), trades.get(0).getQuantity());
        assertBigDecimal(new BigDecimal("31"), trades.get(1).getPrice());
        assertBigDecimal(new BigDecimal("2"), trades.get(1).getQuantity());
    }

    @Test
    void nonCrossingLimitOrderAddsToBook() {
        MatchingEngine engine = new MatchingEngine();
        engine.place(new Order("S1", "U1", "ADA-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("2"), new BigDecimal("10"), 1L));

        Order buy = new Order("B1", "U2", "ADA-USDT", OrderSide.BUY, OrderType.LIMIT,
                new BigDecimal("1.5"), new BigDecimal("4"), 2L);
        List<Trade> trades = engine.place(buy);

        assertTrue(trades.isEmpty());
        OrderBook book = engine.getOrderBook("ADA-USDT").orElseThrow();
        Deque<Order> bids = book.getBids().get(new BigDecimal("1.5"));
        assertNotNull(bids);
        assertEquals("B1", bids.peekFirst().getOrderId());
    }

    private void assertBigDecimal(BigDecimal expected, BigDecimal actual) {
        assertTrue(actual.compareTo(expected) == 0,
                () -> "Expected " + expected + " but got " + actual);
    }
}
