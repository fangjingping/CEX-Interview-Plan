package com.cex.exchange.demo.position;

import com.cex.exchange.model.OrderSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionServiceTest {

    @Test
    void opensAndIncreasesPosition() {
        PositionService service = new PositionService();
        PositionKey key = new PositionKey("U1", "BTC-USDT");

        service.applyTrade(new PositionTrade("T1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), 1L));
        PositionState state = service.applyTrade(new PositionTrade("T2", key, OrderSide.BUY,
                new BigDecimal("110"), new BigDecimal("1"), 2L));

        assertBigDecimal("2", state.quantity());
        assertBigDecimal("105", state.entryPrice());
        assertBigDecimal("0", state.realizedPnl());
    }

    @Test
    void reducesAndRealizesPnl() {
        PositionService service = new PositionService();
        PositionKey key = new PositionKey("U1", "BTC-USDT");

        service.applyTrade(new PositionTrade("T1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("2"), 1L));
        PositionState state = service.applyTrade(new PositionTrade("T2", key, OrderSide.SELL,
                new BigDecimal("110"), new BigDecimal("1"), 2L));

        assertBigDecimal("1", state.quantity());
        assertBigDecimal("100", state.entryPrice());
        assertBigDecimal("10", state.realizedPnl());
    }

    @Test
    void flipsPositionWhenOverReducing() {
        PositionService service = new PositionService();
        PositionKey key = new PositionKey("U1", "BTC-USDT");

        service.applyTrade(new PositionTrade("T1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), 1L));
        PositionState state = service.applyTrade(new PositionTrade("T2", key, OrderSide.SELL,
                new BigDecimal("90"), new BigDecimal("2"), 2L));

        assertTrue(state.quantity().signum() < 0);
        assertBigDecimal("-1", state.quantity());
        assertBigDecimal("90", state.entryPrice());
        assertBigDecimal("-10", state.realizedPnl());
    }

    @Test
    void ignoresDuplicateTradeId() {
        PositionService service = new PositionService();
        PositionKey key = new PositionKey("U1", "BTC-USDT");

        PositionState first = service.applyTrade(new PositionTrade("T1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), 1L));
        PositionState second = service.applyTrade(new PositionTrade("T1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), 1L));

        assertEquals(first, second);
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
