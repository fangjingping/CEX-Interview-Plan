package com.cex.exchange.demo.tradepipeline;

import com.cex.exchange.demo.marketdata.MarketDataBook;
import com.cex.exchange.demo.marketdata.PriceLevel;
import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionState;
import com.cex.exchange.demo.risk.MarginBalance;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.OrderType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerpTradePipelineTest {

    @Test
    void runsPerpTradeFlowEndToEnd() {
        PerpTradePipeline pipeline = new PerpTradePipeline();
        pipeline.updateIndexPrice("BTC-USDT", new BigDecimal("100"));
        pipeline.getRiskService().deposit("U1", new BigDecimal("1000"));
        pipeline.getRiskService().deposit("U2", new BigDecimal("1000"));

        Order sell = new Order("S1", "U1", "BTC-USDT", OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("100"), new BigDecimal("1"), 1L);
        pipeline.place(sell);
        MarginBalance makerBalance = pipeline.getRiskService().getBalance("U1");
        assertBigDecimal("10", makerBalance.frozen());

        Order buy = new Order("B1", "U2", "BTC-USDT", OrderSide.BUY, OrderType.LIMIT,
                new BigDecimal("100"), new BigDecimal("1"), 2L);
        pipeline.place(buy);
        pipeline.publishOutbox(10);
        pipeline.consumeTrades(10);

        assertEquals(1, pipeline.getLedgerService().getEntries().size());

        PositionState makerPosition = pipeline.getPositionService()
                .getPosition(new PositionKey("U1", "BTC-USDT"))
                .orElseThrow();
        PositionState takerPosition = pipeline.getPositionService()
                .getPosition(new PositionKey("U2", "BTC-USDT"))
                .orElseThrow();

        assertBigDecimal("-1", makerPosition.quantity());
        assertBigDecimal("1", takerPosition.quantity());

        MarketDataBook book = pipeline.getMarketDataBook();
        List<PriceLevel> bids = book.topN(com.cex.exchange.demo.marketdata.OrderSide.BID, 1);
        assertEquals(1, bids.size());
        assertBigDecimal("100", bids.get(0).price());

        MarginBalance takerBalance = pipeline.getRiskService().getBalance("U2");
        assertBigDecimal("0", takerBalance.frozen());
        assertBigDecimal("10", takerBalance.used());

        PositionState recoveredMaker = pipeline.getPersistenceService()
                .recover(new PositionKey("U1", "BTC-USDT"));
        PositionState recoveredTaker = pipeline.getPersistenceService()
                .recover(new PositionKey("U2", "BTC-USDT"));
        assertEquals(makerPosition, recoveredMaker);
        assertEquals(takerPosition, recoveredTaker);
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
