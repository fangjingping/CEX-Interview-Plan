package com.cex.exchange.demo.tradepipeline;

import com.cex.exchange.demo.ledger.LedgerEntry;
import com.cex.exchange.demo.ledger.LedgerLine;
import com.cex.exchange.demo.ledger.LedgerLineType;
import com.cex.exchange.demo.ledger.LedgerService;
import com.cex.exchange.demo.liquidation.LiquidationResult;
import com.cex.exchange.demo.liquidation.LiquidationScheduler;
import com.cex.exchange.demo.liquidation.LiquidationService;
import com.cex.exchange.demo.liquidation.LiquidationStatus;
import com.cex.exchange.demo.liquidation.LiquidationTask;
import com.cex.exchange.demo.liquidation.LiquidationWorker;
import com.cex.exchange.demo.marketdata.MarketDataBook;
import com.cex.exchange.demo.marketdata.PriceLevel;
import com.cex.exchange.demo.middleware.IdempotentConsumer;
import com.cex.exchange.demo.middleware.InMemoryIdempotencyStore;
import com.cex.exchange.demo.middleware.Message;
import com.cex.exchange.demo.persistence.Event;
import com.cex.exchange.demo.persistence.EventApplier;
import com.cex.exchange.demo.persistence.EventLogReadResult;
import com.cex.exchange.demo.persistence.FileEventLog;
import com.cex.exchange.demo.persistence.FileSnapshotStore;
import com.cex.exchange.demo.persistence.RecoveryResult;
import com.cex.exchange.demo.persistence.RecoveryService;
import com.cex.exchange.demo.persistence.Snapshot;
import com.cex.exchange.demo.persistence.StateCodec;
import com.cex.exchange.demo.position.Position;
import com.cex.exchange.demo.position.PositionFill;
import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionMode;
import com.cex.exchange.demo.position.PositionService;
import com.cex.exchange.demo.position.PositionSide;
import com.cex.exchange.engine.MatchingEngine;
import com.cex.exchange.engine.TimeSource;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.OrderType;
import com.cex.exchange.model.Trade;
import com.cex.exchange.risk.FreezeStatus;
import com.cex.exchange.risk.OrderRequest;
import com.cex.exchange.risk.RiskCheckService;
import com.cex.exchange.risk.RiskErrorCode;
import com.cex.exchange.risk.RiskResult;
import com.cex.exchange.risk.TradeFill;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndToEndPerpIntegrationTest {
    private static final BigDecimal IMR = new BigDecimal("0.1");
    private static final BigDecimal FEE_RATE = new BigDecimal("0.001");
    private static final BigDecimal LEVERAGE = new BigDecimal("10");

    @TempDir
    Path tempDir;

    @Test
    void runsEndToEndPerpFlow() {
        String symbol = "BTC-USDT";
        MatchingEngine engine = new MatchingEngine(new IncrementalTimeSource(1000L));
        RiskCheckService riskService = new RiskCheckService();
        LedgerService ledgerService = new LedgerService();
        PositionService positionService = new PositionService();
        MarketDataBook marketDataBook = new MarketDataBook();
        IdempotentConsumer consumer = new IdempotentConsumer(new InMemoryIdempotencyStore());
        FileEventLog eventLog = new FileEventLog(tempDir.resolve("events.log"));
        FileSnapshotStore snapshotStore = new FileSnapshotStore(tempDir.resolve("snapshot.dat"));

        riskService.credit("U-MAKER-1", new BigDecimal("1000"));
        riskService.credit("U-MAKER-2", new BigDecimal("1000"));
        riskService.credit("U-TAKER", new BigDecimal("1000"));

        Order makerOne = new Order("O-M1", "U-MAKER-1", symbol, OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("100"), new BigDecimal("1"), 1L);
        Order makerTwo = new Order("O-M2", "U-MAKER-2", symbol, OrderSide.SELL, OrderType.LIMIT,
                new BigDecimal("101"), new BigDecimal("1"), 2L);
        Order taker = new Order("O-T1", "U-TAKER", symbol, OrderSide.BUY, OrderType.LIMIT,
                new BigDecimal("102"), new BigDecimal("2"), 3L);

        RiskResult makerOneRisk = riskService.checkAndFreeze(toRiskRequest(makerOne));
        RiskResult makerTwoRisk = riskService.checkAndFreeze(toRiskRequest(makerTwo));
        RiskResult takerRisk = riskService.checkAndFreeze(toRiskRequest(taker));

        assertTrue(makerOneRisk.isOk());
        assertTrue(makerTwoRisk.isOk());
        assertTrue(takerRisk.isOk());
        assertEquals(FreezeStatus.FROZEN, makerOneRisk.getFrozenRecord().status());
        assertEquals(FreezeStatus.FROZEN, makerTwoRisk.getFrozenRecord().status());
        assertEquals(FreezeStatus.FROZEN, takerRisk.getFrozenRecord().status());

        RiskResult insufficient = riskService.checkAndFreeze(new OrderRequest(
                "O-LOW", "U-LOW", symbol, new BigDecimal("4000"), BigDecimal.ZERO, LEVERAGE, 4L));
        assertEquals(RiskErrorCode.INSUFFICIENT_BALANCE, insufficient.getCode());

        engine.place(makerOne);
        engine.place(makerTwo);
        List<Trade> trades = engine.place(taker);
        assertEquals(2, trades.size());

        riskService.commitAfterFill(List.of(
                toFill(makerOne, trades.get(0).getTimestamp()),
                toFill(makerTwo, trades.get(1).getTimestamp()),
                toFill(taker, trades.get(1).getTimestamp())
        ));

        List<String> expectedEventIds = new ArrayList<>();
        for (Trade trade : trades) {
            TradeEvent event = toTradeEvent(trade, taker.getSide());
            expectedEventIds.add(event.getEventId());
            Message message = new Message(event.getEventId(), event.getEventId(), event.getTimestamp());
            assertTrue(consumer.handle(message,
                    msg -> applyTrade(event, ledgerService, positionService, marketDataBook, eventLog)));
            assertFalse(consumer.handle(message,
                    msg -> applyTrade(event, ledgerService, positionService, marketDataBook, eventLog)));
        }

        EventLogReadResult logResult = eventLog.readAll();
        assertEquals(expectedEventIds, logResult.events().stream().map(Event::eventId).toList());
        assertEquals(0, logResult.skippedLines());

        assertEquals(trades.size(), ledgerService.getEntries().size());
        assertBigDecimal("0", ledgerNet(ledgerService));

        Position takerPosition = positionService.getPositionDetail(new PositionKey("U-TAKER", symbol)).orElseThrow();
        Position makerOnePosition = positionService.getPositionDetail(new PositionKey("U-MAKER-1", symbol)).orElseThrow();
        Position makerTwoPosition = positionService.getPositionDetail(new PositionKey("U-MAKER-2", symbol)).orElseThrow();

        assertEquals(PositionSide.LONG, takerPosition.side());
        assertBigDecimal("2", takerPosition.quantity());
        assertEquals(PositionSide.SHORT, makerOnePosition.side());
        assertBigDecimal("1", makerOnePosition.quantity());
        assertEquals(PositionSide.SHORT, makerTwoPosition.side());
        assertBigDecimal("1", makerTwoPosition.quantity());

        List<PriceLevel> bids = marketDataBook.topN(com.cex.exchange.demo.marketdata.OrderSide.BID, 2);
        assertEquals(2, bids.size());
        assertBigDecimal("101", bids.get(0).price());
        assertBigDecimal("100", bids.get(1).price());

        StateCodec<CounterState> codec = new CounterStateCodec();
        EventApplier<CounterState> applier = (state, event) -> state.add(1);
        RecoveryService<CounterState> recovery = new RecoveryService<>(eventLog, snapshotStore, codec, applier,
                () -> new CounterState(0));

        Event firstEvent = logResult.events().get(0);
        snapshotStore.save(new Snapshot("S1", firstEvent.eventId(), firstEvent.timestamp(), firstEvent.timestamp(),
                codec.encode(new CounterState(1))));

        RecoveryResult<CounterState> recovered = recovery.recover();
        assertEquals(trades.size(), recovered.state().value());
        assertEquals(trades.size() - 1, recovered.appliedEvents());
        assertEquals(0, recovered.skippedLines());

        marketDataBook.update(com.cex.exchange.demo.marketdata.OrderSide.ASK,
                new BigDecimal("70"), new BigDecimal("1"));
        LiquidationScheduler scheduler = new LiquidationScheduler(10);
        LiquidationService liquidationService = new LiquidationService(positionService);
        LiquidationWorker worker = new LiquidationWorker(scheduler, liquidationService);
        LiquidationTask task = new LiquidationTask("LQ-1", "U-TAKER", symbol,
                new BigDecimal("70"), new BigDecimal("10"), 2000L);
        assertTrue(scheduler.enqueue(task));

        LiquidationResult result = worker.processNext().orElseThrow();
        assertEquals(LiquidationStatus.EXECUTED, result.status());

        Position afterLiquidation = positionService.getPositionDetail(new PositionKey("U-TAKER", symbol)).orElseThrow();
        assertEquals(PositionSide.FLAT, afterLiquidation.side());
        assertBigDecimal("0", afterLiquidation.quantity());
    }

    private TradeEvent toTradeEvent(Trade trade, OrderSide takerSide) {
        return new TradeEvent(
                "T-" + trade.getTradeId(),
                trade.getSymbol(),
                takerSide,
                trade.getPrice(),
                trade.getQuantity(),
                trade.getTakerOrderId(),
                trade.getMakerOrderId(),
                trade.getTakerUserId(),
                trade.getMakerUserId(),
                trade.getTimestamp()
        );
    }

    private void applyTrade(TradeEvent event,
                            LedgerService ledgerService,
                            PositionService positionService,
                            MarketDataBook marketDataBook,
                            FileEventLog eventLog) {
        BigDecimal notional = event.getPrice().multiply(event.getQuantity());
        List<LedgerLine> lines = List.of(
                new LedgerLine(event.getTakerUserId(), LedgerLineType.DEBIT, notional),
                new LedgerLine(event.getMakerUserId(), LedgerLineType.CREDIT, notional)
        );
        ledgerService.post("L-" + event.getEventId(), lines);

        PositionFill takerFill = new PositionFill("F-" + event.getEventId() + "-T",
                event.getTakerUserId(),
                event.getSymbol(),
                event.getSide(),
                event.getPrice(),
                event.getQuantity(),
                PositionMode.CROSS,
                event.getTimestamp());
        positionService.applyFill(takerFill);

        OrderSide makerSide = event.getSide() == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
        PositionFill makerFill = new PositionFill("F-" + event.getEventId() + "-M",
                event.getMakerUserId(),
                event.getSymbol(),
                makerSide,
                event.getPrice(),
                event.getQuantity(),
                PositionMode.CROSS,
                event.getTimestamp());
        positionService.applyFill(makerFill);

        com.cex.exchange.demo.marketdata.OrderSide bookSide =
                event.getSide() == OrderSide.BUY
                        ? com.cex.exchange.demo.marketdata.OrderSide.BID
                        : com.cex.exchange.demo.marketdata.OrderSide.ASK;
        marketDataBook.update(bookSide, event.getPrice(), event.getQuantity());

        String payload = String.join(",",
                event.getSymbol(),
                event.getPrice().toPlainString(),
                event.getQuantity().toPlainString());
        eventLog.append(new Event(event.getEventId(), "TRADE", event.getTimestamp(), payload));
    }

    private OrderRequest toRiskRequest(Order order) {
        BigDecimal notional = order.getPrice().multiply(order.getQuantity());
        BigDecimal margin = notional.multiply(IMR);
        BigDecimal fee = notional.multiply(FEE_RATE);
        return new OrderRequest(order.getOrderId(), order.getUserId(), order.getSymbol(),
                margin, fee, LEVERAGE, order.getTimestamp());
    }

    private TradeFill toFill(Order order, long timestamp) {
        BigDecimal notional = order.getPrice().multiply(order.getQuantity());
        BigDecimal margin = notional.multiply(IMR);
        BigDecimal fee = notional.multiply(FEE_RATE);
        String fillId = "FILL-" + order.getOrderId() + "-" + timestamp;
        return new TradeFill(fillId, order.getOrderId(), margin, fee, timestamp);
    }

    private BigDecimal ledgerNet(LedgerService ledgerService) {
        BigDecimal net = BigDecimal.ZERO;
        for (LedgerEntry entry : ledgerService.getEntries()) {
            for (LedgerLine line : entry.lines()) {
                net = line.type() == LedgerLineType.DEBIT
                        ? net.add(line.amount())
                        : net.subtract(line.amount());
            }
        }
        return net;
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static final class IncrementalTimeSource implements TimeSource {
        private final AtomicLong now;

        private IncrementalTimeSource(long start) {
            this.now = new AtomicLong(start);
        }

        @Override
        public long nowMillis() {
            return now.getAndIncrement();
        }
    }

    private record CounterState(long value) {
        CounterState add(long delta) {
            return new CounterState(value + delta);
        }
    }

    private static final class CounterStateCodec implements StateCodec<CounterState> {
        @Override
        public String encode(CounterState state) {
            return Long.toString(state.value());
        }

        @Override
        public CounterState decode(String payload) {
            return new CounterState(Long.parseLong(payload));
        }
    }
}
