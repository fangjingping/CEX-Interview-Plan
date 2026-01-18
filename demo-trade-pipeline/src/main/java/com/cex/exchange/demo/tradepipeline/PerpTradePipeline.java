package com.cex.exchange.demo.tradepipeline;

import com.cex.exchange.demo.ledger.LedgerLine;
import com.cex.exchange.demo.ledger.LedgerLineType;
import com.cex.exchange.demo.ledger.LedgerService;
import com.cex.exchange.demo.liquidation.LiquidationDecision;
import com.cex.exchange.demo.liquidation.LiquidationEngine;
import com.cex.exchange.demo.liquidation.LiquidationRequest;
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
import com.cex.exchange.demo.persistence.PositionEvent;
import com.cex.exchange.demo.persistence.PositionPersistenceService;
import com.cex.exchange.demo.persistence.PositionSnapshot;
import com.cex.exchange.demo.persistence.StoredPositionEvent;
import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionService;
import com.cex.exchange.demo.position.PositionState;
import com.cex.exchange.demo.position.PositionTrade;
import com.cex.exchange.demo.pricing.IndexPrice;
import com.cex.exchange.demo.pricing.MarkPrice;
import com.cex.exchange.demo.pricing.PricingPolicy;
import com.cex.exchange.demo.pricing.PricingService;
import com.cex.exchange.demo.risk.MarginBalance;
import com.cex.exchange.demo.risk.RiskService;
import com.cex.exchange.engine.MatchingEngine;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.OrderType;
import com.cex.exchange.model.Trade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PerpTradePipeline {
    private static final int DEFAULT_PARTITIONS = 2;
    private static final BigDecimal DEFAULT_INITIAL_MARGIN_RATE = new BigDecimal("0.1");
    private static final BigDecimal DEFAULT_MAINTENANCE_MARGIN_RATE = new BigDecimal("0.05");

    private final MatchingEngine matchingEngine;
    private final OutboxStore outboxStore;
    private final OutboxPublisher outboxPublisher;
    private final PartitionedLog log;
    private final IdempotentConsumer consumer;
    private final LedgerService ledgerService;
    private final MarketDataBook marketDataBook;
    private final RiskService riskService;
    private final PositionService positionService;
    private final PricingService pricingService;
    private final LiquidationEngine liquidationEngine;
    private final PositionPersistenceService persistenceService;
    private final TradeEventCodec codec = new TradeEventCodec();
    private final Map<Integer, Integer> offsets = new ConcurrentHashMap<>();
    private final Map<String, IndexPrice> indexPrices = new ConcurrentHashMap<>();
    private final Map<String, LiquidationDecision> liquidationDecisions = new ConcurrentHashMap<>();
    private final int partitionCount;
    private final BigDecimal initialMarginRate;
    private final BigDecimal maintenanceMarginRate;
    private final PricingPolicy pricingPolicy;

    public PerpTradePipeline() {
        this(new MatchingEngine(), new LedgerService(), new MarketDataBook(), new RiskService(),
                new PositionService(), new PricingService(), new LiquidationEngine(), new PositionPersistenceService(),
                DEFAULT_PARTITIONS, DEFAULT_INITIAL_MARGIN_RATE, DEFAULT_MAINTENANCE_MARGIN_RATE,
                new PricingPolicy(new BigDecimal("0.05"), new BigDecimal("0.1"), new BigDecimal("0.02")));
    }

    public PerpTradePipeline(MatchingEngine matchingEngine,
                             LedgerService ledgerService,
                             MarketDataBook marketDataBook,
                             RiskService riskService,
                             PositionService positionService,
                             PricingService pricingService,
                             LiquidationEngine liquidationEngine,
                             PositionPersistenceService persistenceService,
                             int partitionCount,
                             BigDecimal initialMarginRate,
                             BigDecimal maintenanceMarginRate,
                             PricingPolicy pricingPolicy) {
        if (partitionCount <= 0) {
            throw new IllegalArgumentException("partitionCount must be > 0");
        }
        this.matchingEngine = Objects.requireNonNull(matchingEngine, "matchingEngine");
        this.ledgerService = Objects.requireNonNull(ledgerService, "ledgerService");
        this.marketDataBook = Objects.requireNonNull(marketDataBook, "marketDataBook");
        this.riskService = Objects.requireNonNull(riskService, "riskService");
        this.positionService = Objects.requireNonNull(positionService, "positionService");
        this.pricingService = Objects.requireNonNull(pricingService, "pricingService");
        this.liquidationEngine = Objects.requireNonNull(liquidationEngine, "liquidationEngine");
        this.persistenceService = Objects.requireNonNull(persistenceService, "persistenceService");
        this.partitionCount = partitionCount;
        this.initialMarginRate = Objects.requireNonNull(initialMarginRate, "initialMarginRate");
        this.maintenanceMarginRate = Objects.requireNonNull(maintenanceMarginRate, "maintenanceMarginRate");
        this.pricingPolicy = Objects.requireNonNull(pricingPolicy, "pricingPolicy");
        this.outboxStore = new OutboxStore();
        this.log = new PartitionedLog(partitionCount, new HashPartitioner());
        this.outboxPublisher = new OutboxPublisher(outboxStore, log);
        this.consumer = new IdempotentConsumer(new InMemoryIdempotencyStore());
    }

    public void updateIndexPrice(String symbol, BigDecimal price) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(price, "price");
        indexPrices.put(symbol, new IndexPrice(symbol, price, System.currentTimeMillis()));
    }

    public List<Trade> place(Order order) {
        Objects.requireNonNull(order, "order");
        BigDecimal requiredMargin = calculateInitialMargin(order);
        riskService.freeze(holdId(order.getOrderId()), order.getUserId(), requiredMargin);
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

    public RiskService getRiskService() {
        return riskService;
    }

    public PositionService getPositionService() {
        return positionService;
    }

    public PositionPersistenceService getPersistenceService() {
        return persistenceService;
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public LiquidationDecision getLiquidationDecision(String requestId) {
        return liquidationDecisions.get(requestId);
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

        BigDecimal marginUsed = notional.multiply(initialMarginRate);
        riskService.consume(holdId(event.getTakerOrderId()), event.getEventId() + "-" + event.getTakerOrderId(), marginUsed);
        riskService.consume(holdId(event.getMakerOrderId()), event.getEventId() + "-" + event.getMakerOrderId(), marginUsed);

        PositionState takerState = applyPosition(event.getEventId() + "-T", event.getTakerUserId(), event.getSymbol(),
                event.getSide(), event.getPrice(), event.getQuantity(), event.getTimestamp());
        OrderSide makerSide = event.getSide() == OrderSide.BUY ? OrderSide.SELL : OrderSide.BUY;
        PositionState makerState = applyPosition(event.getEventId() + "-M", event.getMakerUserId(), event.getSymbol(),
                makerSide, event.getPrice(), event.getQuantity(), event.getTimestamp());

        IndexPrice indexPrice = indexPrices.getOrDefault(event.getSymbol(),
                new IndexPrice(event.getSymbol(), event.getPrice(), event.getTimestamp()));
        MarkPrice markPrice = pricingService.markPrice(indexPrice, event.getPrice(), pricingPolicy);

        recordLiquidation(event.getEventId(), event.getSymbol(), event.getTakerUserId(), takerState, markPrice);
        recordLiquidation(event.getEventId(), event.getSymbol(), event.getMakerUserId(), makerState, markPrice);
    }

    private PositionState applyPosition(String tradeId,
                                        String userId,
                                        String symbol,
                                        OrderSide side,
                                        BigDecimal price,
                                        BigDecimal quantity,
                                        long timestamp) {
        PositionKey key = new PositionKey(userId, symbol);
        PositionTrade trade = new PositionTrade(tradeId, key, side, price, quantity, timestamp);
        PositionState state = positionService.applyTrade(trade);
        PositionEvent event = new PositionEvent(tradeId, key, side, price, quantity, timestamp);
        StoredPositionEvent stored = persistenceService.record(event);
        persistenceService.saveSnapshot(new PositionSnapshot(key, state, stored.version(), timestamp));
        return state;
    }

    private void recordLiquidation(String eventId, String symbol, String userId, PositionState state, MarkPrice markPrice) {
        MarginBalance balance = riskService.getBalance(userId);
        LiquidationRequest request = new LiquidationRequest(
                "LQ-" + eventId + "-" + userId,
                userId,
                symbol,
                state.quantity(),
                state.entryPrice(),
                markPrice.price(),
                balance.used(),
                maintenanceMarginRate,
                markPrice.timestamp()
        );
        LiquidationDecision decision = liquidationEngine.evaluate(request);
        liquidationDecisions.put(request.requestId(), decision);
    }

    private BigDecimal calculateInitialMargin(Order order) {
        BigDecimal price = order.getPrice();
        if (price == null && order.getType() == OrderType.MARKET) {
            IndexPrice indexPrice = indexPrices.get(order.getSymbol());
            if (indexPrice == null) {
                throw new IllegalArgumentException("index price required for market order");
            }
            price = indexPrice.price();
        }
        if (price == null) {
            throw new IllegalArgumentException("price required for margin calculation");
        }
        return price.multiply(order.getQuantity()).multiply(initialMarginRate);
    }

    private String holdId(String orderId) {
        return "R-" + orderId;
    }
}
