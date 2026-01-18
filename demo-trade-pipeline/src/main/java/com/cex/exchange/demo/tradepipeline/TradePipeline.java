package com.cex.exchange.demo.tradepipeline;

import com.cex.exchange.demo.ledger.LedgerLine;
import com.cex.exchange.demo.ledger.LedgerLineType;
import com.cex.exchange.demo.ledger.LedgerService;
import com.cex.exchange.demo.marketdata.MarketDataBook;
import com.cex.exchange.demo.middleware.IdempotentConsumer;
import com.cex.exchange.demo.middleware.InMemoryIdempotencyStore;
import com.cex.exchange.demo.middleware.Message;
import com.cex.exchange.demo.middleware.kafka.HashPartitioner;
import com.cex.exchange.demo.middleware.kafka.PartitionRecord;
import com.cex.exchange.demo.middleware.kafka.PartitionedLog;
import com.cex.exchange.demo.middleware.outbox.OutboxEvent;
import com.cex.exchange.demo.middleware.outbox.OutboxPublisher;
import com.cex.exchange.demo.middleware.outbox.OutboxStore;
import com.cex.exchange.engine.MatchingEngine;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.Trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TradePipeline 核心类。
 */
public class TradePipeline {
    private static final int DEFAULT_PARTITIONS = 2;

    private final MatchingEngine matchingEngine;
    private final OutboxStore outboxStore;
    private final OutboxPublisher outboxPublisher;
    private final PartitionedLog log;
    private final IdempotentConsumer consumer;
    private final LedgerService ledgerService;
    private final MarketDataBook marketDataBook;
    private final TradeEventCodec codec = new TradeEventCodec();
    private final Map<Integer, Integer> offsets = new ConcurrentHashMap<>();
    private final int partitionCount;

    public TradePipeline() {
        this(new MatchingEngine(), new LedgerService(), new MarketDataBook(), DEFAULT_PARTITIONS);
    }

    public TradePipeline(MatchingEngine matchingEngine,
                         LedgerService ledgerService,
                         MarketDataBook marketDataBook,
                         int partitionCount) {
        if (partitionCount <= 0) {
            throw new IllegalArgumentException("partitionCount must be > 0");
        }
        this.matchingEngine = Objects.requireNonNull(matchingEngine, "matchingEngine");
        this.ledgerService = Objects.requireNonNull(ledgerService, "ledgerService");
        this.marketDataBook = Objects.requireNonNull(marketDataBook, "marketDataBook");
        this.outboxStore = new OutboxStore();
        this.log = new PartitionedLog(partitionCount, new HashPartitioner());
        this.outboxPublisher = new OutboxPublisher(outboxStore, log);
        this.consumer = new IdempotentConsumer(new InMemoryIdempotencyStore());
        this.partitionCount = partitionCount;
    }

    public List<Trade> place(Order order) {
        Objects.requireNonNull(order, "order");
        List<Trade> trades = matchingEngine.place(order);
        for (Trade trade : trades) {
            TradeEvent event = new TradeEvent(
                    "T-" + trade.getTradeId(),
                    trade.getSymbol(),
                    order.getSide(),
                    trade.getPrice(),
                    trade.getQuantity(),
                    trade.getTakerOrderId(),
                    trade.getMakerOrderId(),
                    trade.getTakerUserId(),
                    trade.getMakerUserId(),
                    trade.getTimestamp()
            );
            outboxStore.add(new OutboxEvent(event.getEventId(), trade.getSymbol(), codec.encode(event)));
        }
        return trades;
    }

    public int publishOutbox(int limit) {
        return outboxPublisher.publish(limit);
    }

    public int consumeTrades(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be > 0");
        }
        int remaining = limit;
        int processed = 0;
        for (int partition = 0; partition < partitionCount; partition++) {
            int offset = offsets.getOrDefault(partition, 0);
            List<PartitionRecord> records = log.read(partition, offset, remaining);
            for (PartitionRecord record : records) {
                TradeEvent event = codec.decode(record.payload());
                Message message = new Message(event.getEventId(), record.payload(), event.getTimestamp());
                consumer.handle(message, msg -> applyTrade(event));
                offset++;
                processed++;
                remaining--;
                if (remaining <= 0) {
                    break;
                }
            }
            offsets.put(partition, offset);
            if (remaining <= 0) {
                break;
            }
        }
        return processed;
    }

    public LedgerService getLedgerService() {
        return ledgerService;
    }

    public MarketDataBook getMarketDataBook() {
        return marketDataBook;
    }

    private void applyTrade(TradeEvent event) {
        BigDecimal notional = event.getPrice().multiply(event.getQuantity());
        List<LedgerLine> lines = List.of(
                new LedgerLine(event.getTakerUserId(), LedgerLineType.DEBIT, notional),
                new LedgerLine(event.getMakerUserId(), LedgerLineType.CREDIT, notional)
        );
        ledgerService.post("L-" + event.getEventId(), lines);
        com.cex.exchange.demo.marketdata.OrderSide bookSide =
                event.getSide() == OrderSide.BUY
                        ? com.cex.exchange.demo.marketdata.OrderSide.BID
                        : com.cex.exchange.demo.marketdata.OrderSide.ASK;
        marketDataBook.update(bookSide, event.getPrice(), event.getQuantity());
    }
}
