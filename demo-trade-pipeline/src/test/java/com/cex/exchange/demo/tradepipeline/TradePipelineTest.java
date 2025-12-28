package com.cex.exchange.demo.tradepipeline;

import com.cex.exchange.demo.marketdata.MarketDataBook;
import com.cex.exchange.demo.marketdata.PriceLevel;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.OrderType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TradePipelineTest 单元测试。
 */
class TradePipelineTest {

    @Test
    void processesTradeEndToEnd() {
        TradePipeline pipeline = new TradePipeline();
        Order sell = new Order("S1", "U1", "BTC-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("100"), new BigDecimal("5"), 1L);
        pipeline.place(sell);
        pipeline.publishOutbox(10);
        assertEquals(0, pipeline.consumeTrades(10));
        assertEquals(0, pipeline.getLedgerService().getEntries().size());

        Order buy = new Order("B1", "U2", "BTC-USDT", OrderSide.BUY, OrderType.LIMIT,
                new BigDecimal("105"), new BigDecimal("5"), 2L);
        pipeline.place(buy);
        pipeline.publishOutbox(10);
        assertEquals(1, pipeline.consumeTrades(10));
        assertEquals(1, pipeline.getLedgerService().getEntries().size());

        MarketDataBook book = pipeline.getMarketDataBook();
        List<PriceLevel> bids = book.topN(com.cex.exchange.demo.marketdata.OrderSide.BID, 1);
        assertEquals(1, bids.size());
        assertTrue(bids.get(0).price().compareTo(new BigDecimal("100")) == 0);
    }
}
