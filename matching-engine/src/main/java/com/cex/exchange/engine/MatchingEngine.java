package com.cex.exchange.engine;

import com.cex.exchange.book.OrderBook;
import com.cex.exchange.model.Order;
import com.cex.exchange.model.OrderSide;
import com.cex.exchange.model.OrderType;
import com.cex.exchange.model.Trade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MatchingEngine 核心类。
 */
public class MatchingEngine {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final Map<String, OrderBook> books;
    private final Map<String, Object> bookLocks;
    private final AtomicLong tradeSequence;
    private final TimeSource timeSource;

    public MatchingEngine() {
        this(new SystemTimeSource());
    }

    public MatchingEngine(TimeSource timeSource) {
        this.books = new ConcurrentHashMap<>();
        this.bookLocks = new ConcurrentHashMap<>();
        this.tradeSequence = new AtomicLong(1);
        this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
    }

    public List<Trade> place(Order order) {
        Objects.requireNonNull(order, "order");
        Object lock = bookLocks.computeIfAbsent(order.getSymbol(), key -> new Object());
        synchronized (lock) {
            OrderBook book = books.computeIfAbsent(order.getSymbol(), OrderBook::new);
            return match(order, book);
        }
    }

    public Optional<OrderBook> getOrderBook(String symbol) {
        return Optional.ofNullable(books.get(symbol));
    }

    private List<Trade> match(Order taker, OrderBook book) {
        List<Trade> trades = new ArrayList<>();
        NavigableMap<BigDecimal, Deque<Order>> opposite =
                taker.getSide() == OrderSide.BUY ? book.getAsks() : book.getBids();
        while (taker.getRemainingQuantity().compareTo(ZERO) > 0 && !opposite.isEmpty()) {
            BigDecimal bestPrice = bestPrice(opposite, taker.getSide());
            if (taker.getType() == OrderType.LIMIT && !isCrossed(taker, bestPrice)) {
                break;
            }
            Deque<Order> level = opposite.get(bestPrice);
            while (taker.getRemainingQuantity().compareTo(ZERO) > 0 && level != null && !level.isEmpty()) {
                Order maker = level.peekFirst();
                BigDecimal fillQuantity = min(taker.getRemainingQuantity(), maker.getRemainingQuantity());
                taker.reduce(fillQuantity);
                maker.reduce(fillQuantity);
                trades.add(new Trade(
                        tradeSequence.getAndIncrement(),
                        taker.getSymbol(),
                        bestPrice,
                        fillQuantity,
                        taker.getOrderId(),
                        maker.getOrderId(),
                        taker.getUserId(),
                        maker.getUserId(),
                        timeSource.nowMillis()
                ));
                if (maker.isFilled()) {
                    level.removeFirst();
                }
            }
            if (level == null || level.isEmpty()) {
                opposite.remove(bestPrice);
            }
        }
        if (taker.getRemainingQuantity().compareTo(ZERO) > 0 && taker.getType() == OrderType.LIMIT) {
            book.add(taker);
        }
        return trades;
    }

    private boolean isCrossed(Order taker, BigDecimal bestPrice) {
        if (taker.getSide() == OrderSide.BUY) {
            return taker.getPrice().compareTo(bestPrice) >= 0;
        }
        return taker.getPrice().compareTo(bestPrice) <= 0;
    }

    private BigDecimal bestPrice(NavigableMap<BigDecimal, Deque<Order>> opposite, OrderSide takerSide) {
        return takerSide == OrderSide.BUY ? opposite.firstKey() : opposite.lastKey();
    }

    private BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) <= 0 ? left : right;
    }
}
