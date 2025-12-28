package com.cex.exchange.demo.marketdata;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MarketDataBookTest 单元测试。
 */
class MarketDataBookTest {

    @Test
    void returnsTopLevelsBySide() {
        MarketDataBook book = new MarketDataBook();
        book.update(OrderSide.BID, new BigDecimal("100"), new BigDecimal("1"));
        book.update(OrderSide.BID, new BigDecimal("101"), new BigDecimal("2"));
        book.update(OrderSide.ASK, new BigDecimal("102"), new BigDecimal("1"));
        book.update(OrderSide.ASK, new BigDecimal("103"), new BigDecimal("1"));

        List<PriceLevel> bids = book.topN(OrderSide.BID, 1);
        List<PriceLevel> asks = book.topN(OrderSide.ASK, 1);

        assertEquals(new BigDecimal("101"), bids.get(0).price());
        assertEquals(new BigDecimal("102"), asks.get(0).price());
    }

    @Test
    void removesLevelWhenQuantityDropsToZero() {
        MarketDataBook book = new MarketDataBook();
        book.update(OrderSide.BID, new BigDecimal("100"), new BigDecimal("1"));
        book.update(OrderSide.BID, new BigDecimal("101"), new BigDecimal("2"));
        book.update(OrderSide.BID, new BigDecimal("101"), new BigDecimal("-2"));

        List<PriceLevel> bids = book.topN(OrderSide.BID, 1);
        assertEquals(new BigDecimal("100"), bids.get(0).price());
    }
}
