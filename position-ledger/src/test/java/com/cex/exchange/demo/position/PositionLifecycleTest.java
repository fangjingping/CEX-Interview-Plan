package com.cex.exchange.demo.position;

import com.cex.exchange.model.OrderSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PositionLifecycleTest {

    @Test
    void opensAndIncreasesPosition() {
        PositionService service = new PositionService();
        PositionFill open = new PositionFill("F1", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), PositionMode.CROSS, 1L);
        PositionFill add = new PositionFill("F2", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("110"), new BigDecimal("1"), PositionMode.CROSS, 2L);

        Position first = service.applyFill(open);
        Position second = service.applyFill(add);

        assertEquals(PositionSide.LONG, second.side());
        assertBigDecimal("2", second.quantity());
        assertBigDecimal("105", second.entryPrice());
        assertBigDecimal("21", second.margin());
        assertBigDecimal("0", second.realizedPnl());
        assertEquals(PositionMode.CROSS, first.mode());
    }

    @Test
    void reducesAndClosesPosition() {
        PositionService service = new PositionService();
        PositionFill open = new PositionFill("F3", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("2"), PositionMode.CROSS, 1L);
        PositionFill reduce = new PositionFill("F4", "U1", "BTC-USDT", OrderSide.SELL,
                new BigDecimal("110"), new BigDecimal("1"), PositionMode.CROSS, 2L);
        PositionFill close = new PositionFill("F5", "U1", "BTC-USDT", OrderSide.SELL,
                new BigDecimal("90"), new BigDecimal("1"), PositionMode.CROSS, 3L);

        service.applyFill(open);
        Position afterReduce = service.applyFill(reduce);
        Position afterClose = service.applyFill(close);

        assertEquals(PositionSide.LONG, afterReduce.side());
        assertBigDecimal("1", afterReduce.quantity());
        assertBigDecimal("100", afterReduce.entryPrice());
        assertBigDecimal("10", afterReduce.realizedPnl());

        assertEquals(PositionSide.FLAT, afterClose.side());
        assertBigDecimal("0", afterClose.quantity());
        assertBigDecimal("0", afterClose.margin());
        assertBigDecimal("0", afterClose.realizedPnl());
    }

    @Test
    void computesRiskSnapshot() {
        PositionService service = new PositionService();
        PositionFill open = new PositionFill("F6", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), PositionMode.CROSS, 1L);
        service.applyFill(open);

        MarginSnapshot snapshot = service.computeRisk("U1", "BTC-USDT", new BigDecimal("90"));

        assertBigDecimal("0", snapshot.equity());
        assertBigDecimal("-9", snapshot.available());
        assertBigDecimal("9", snapshot.used());
        assertBigDecimal("94.73684211", snapshot.liquidationPrice());
        assertBigDecimal("90", snapshot.bankruptcyPrice());
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        PositionService service = new PositionService();
        PositionFill zero = new PositionFill("F7", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("0"), PositionMode.CROSS, 1L);
        PositionFill negative = new PositionFill("F8", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("-1"), PositionMode.CROSS, 1L);

        assertThrows(PositionException.class, () -> service.applyFill(zero));
        assertThrows(PositionException.class, () -> service.applyFill(negative));
    }

    @Test
    void handlesExtremePriceValues() {
        PositionService service = new PositionService();
        PositionFill open = new PositionFill("F9", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), PositionMode.CROSS, 1L);
        service.applyFill(open);

        MarginSnapshot snapshot = service.computeRisk("U1", "BTC-USDT", new BigDecimal("1000000000"));

        assertBigDecimal("999999910", snapshot.equity());
        assertBigDecimal("899999910", snapshot.available());
        assertBigDecimal("100000000", snapshot.used());
        assertBigDecimal("94.73684211", snapshot.liquidationPrice());
        assertBigDecimal("90", snapshot.bankruptcyPrice());
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
