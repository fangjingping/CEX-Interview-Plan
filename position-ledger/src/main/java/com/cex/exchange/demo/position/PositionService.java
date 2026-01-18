package com.cex.exchange.demo.position;

import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PositionService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_IMR = new BigDecimal("0.1");
    private static final BigDecimal DEFAULT_MMR = new BigDecimal("0.05");
    private static final int PRICE_SCALE = 8;
    private static final int QTY_SCALE = 8;
    private static final int AMOUNT_SCALE = 8;
    private static final int RISK_SCALE = 8;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final Map<PositionKey, PositionState> positions = new ConcurrentHashMap<>();
    private final Set<String> processedTrades = ConcurrentHashMap.newKeySet();

    private final Map<PositionKey, Position> positionBook = new ConcurrentHashMap<>();
    private final Set<String> processedFills = ConcurrentHashMap.newKeySet();
    private final BigDecimal imr;
    private final BigDecimal mmr;

    public PositionService() {
        this(DEFAULT_IMR, DEFAULT_MMR);
    }

    public PositionService(BigDecimal imr, BigDecimal mmr) {
        Objects.requireNonNull(imr, "imr");
        Objects.requireNonNull(mmr, "mmr");
        BigDecimal normalizedImr = normalizeRatio(imr);
        BigDecimal normalizedMmr = normalizeRatio(mmr);
        if (normalizedImr.compareTo(ZERO) <= 0 || normalizedMmr.compareTo(ZERO) <= 0
                || normalizedMmr.compareTo(normalizedImr) > 0) {
            throw new PositionException(PositionErrorCode.INVALID_MODE, "invalid margin rates");
        }
        this.imr = normalizedImr;
        this.mmr = normalizedMmr;
    }

    public PositionState applyTrade(PositionTrade trade) {
        Objects.requireNonNull(trade, "trade");
        validateTrade(trade);
        if (!processedTrades.add(trade.tradeId())) {
            return positions.getOrDefault(trade.key(), PositionState.empty(trade.key()));
        }
        try {
            return positions.compute(trade.key(), (key, current) -> nextState(current, trade));
        } catch (RuntimeException ex) {
            processedTrades.remove(trade.tradeId());
            throw ex;
        }
    }

    public Optional<PositionState> getPosition(PositionKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(positions.get(key));
    }

    public Position applyFill(PositionFill fill) {
        Objects.requireNonNull(fill, "fill");
        validateFill(fill);
        PositionKey key = new PositionKey(fill.userId(), fill.symbol());
        if (!processedFills.add(fill.fillId())) {
            return positionBook.getOrDefault(key, Position.empty(key, fill.mode()));
        }
        try {
            return positionBook.compute(key, (posKey, current) -> nextPosition(current, fill, posKey));
        } catch (RuntimeException ex) {
            processedFills.remove(fill.fillId());
            throw ex;
        }
    }

    public Optional<Position> getPositionDetail(PositionKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(positionBook.get(key));
    }

    public Optional<PositionClose> closePosition(String userId, String symbol, BigDecimal markPrice, long timestamp) {
        if (markPrice == null || normalizePrice(markPrice).compareTo(ZERO) <= 0) {
            throw new PositionException(PositionErrorCode.INVALID_MARK_PRICE, "markPrice must be > 0");
        }
        PositionKey key = new PositionKey(userId, symbol);
        PositionClose[] holder = new PositionClose[1];
        positionBook.compute(key, (posKey, current) -> {
            Position existing = current == null ? Position.empty(posKey, PositionMode.CROSS) : current;
            if (existing.side() == PositionSide.FLAT || normalizeQty(existing.quantity()).compareTo(ZERO) == 0) {
                return existing;
            }
            BigDecimal qty = normalizeQty(existing.quantity());
            BigDecimal entry = normalizePrice(existing.entryPrice());
            BigDecimal price = normalizePrice(markPrice);
            BigDecimal pnlDelta = existing.side() == PositionSide.LONG
                    ? price.subtract(entry).multiply(qty)
                    : entry.subtract(price).multiply(qty);
            BigDecimal realized = normalizeAmount(existing.realizedPnl().add(pnlDelta));
            holder[0] = new PositionClose(posKey.userId(), posKey.symbol(), existing.side(), qty, price,
                    normalizeAmount(pnlDelta), timestamp);
            BigDecimal zeroQty = normalizeQty(ZERO);
            BigDecimal zeroPrice = normalizePrice(ZERO);
            BigDecimal zeroAmount = normalizeAmount(ZERO);
            return new Position(posKey.userId(), posKey.symbol(), PositionSide.FLAT, zeroQty, zeroPrice,
                    zeroAmount, realized, existing.mode());
        });
        return Optional.ofNullable(holder[0]);
    }

    /**
     * 假设：单向持仓，保证金 = |qty| * entryPrice * IMR；不考虑手续费/资金费。
     */
    public MarginSnapshot computeRisk(String userId, String symbol, BigDecimal markPrice) {
        if (markPrice == null || normalizePrice(markPrice).compareTo(ZERO) <= 0) {
            throw new PositionException(PositionErrorCode.INVALID_MARK_PRICE, "markPrice must be > 0");
        }
        PositionKey key = new PositionKey(userId, symbol);
        Position position = positionBook.getOrDefault(key, Position.empty(key, PositionMode.CROSS));
        if (position.quantity().compareTo(ZERO) == 0 || position.side() == PositionSide.FLAT) {
            BigDecimal zero = normalizeAmount(ZERO);
            BigDecimal zeroPrice = normalizePrice(ZERO);
            return new MarginSnapshot(zero, zero, zero, imr, mmr, zeroPrice, zeroPrice);
        }
        BigDecimal qty = normalizeQty(position.quantity());
        BigDecimal mark = normalizePrice(markPrice);
        BigDecimal notional = normalizeAmount(mark.multiply(qty));
        BigDecimal unrealized = normalizeAmount(unrealizedPnl(position, mark));
        BigDecimal equity = normalizeAmount(position.margin().add(unrealized));
        BigDecimal used = normalizeAmount(notional.multiply(imr));
        BigDecimal available = normalizeAmount(equity.subtract(used));
        BigDecimal liquidationPrice = liquidationPrice(position, qty);
        BigDecimal bankruptcyPrice = bankruptcyPrice(position, qty);

        return new MarginSnapshot(equity, available, used, imr, mmr, liquidationPrice, bankruptcyPrice);
    }

    public static PositionState nextState(PositionState currentState, PositionTrade trade) {
        PositionState current = currentState == null ? PositionState.empty(trade.key()) : currentState;
        BigDecimal tradeQty = normalizeQty(trade.quantity());
        BigDecimal tradePrice = normalizePrice(trade.price());
        BigDecimal signedQty = trade.side() == OrderSide.BUY ? tradeQty : tradeQty.negate();
        BigDecimal currentQty = normalizeQty(current.quantity());
        BigDecimal currentEntry = normalizePrice(current.entryPrice());
        BigDecimal currentRealized = normalizeAmount(current.realizedPnl());
        BigDecimal newQty = currentQty.add(signedQty);
        BigDecimal newEntryPrice = currentEntry;
        BigDecimal newRealized = currentRealized;

        if (currentQty.signum() == 0) {
            newEntryPrice = tradePrice;
        } else if (currentQty.signum() == signedQty.signum()) {
            BigDecimal total = currentQty.abs().add(signedQty.abs());
            BigDecimal weighted = currentEntry.multiply(currentQty.abs())
                    .add(tradePrice.multiply(signedQty.abs()));
            newEntryPrice = weighted.divide(total, PRICE_SCALE, ROUNDING);
        } else {
            BigDecimal closed = currentQty.abs().min(signedQty.abs());
            BigDecimal pnlPerUnit = currentQty.signum() > 0
                    ? tradePrice.subtract(currentEntry)
                    : currentEntry.subtract(tradePrice);
            newRealized = normalizeAmount(currentRealized.add(pnlPerUnit.multiply(closed)));
            if (newQty.signum() == 0) {
                newEntryPrice = normalizePrice(ZERO);
            } else if (newQty.signum() == signedQty.signum()) {
                newEntryPrice = tradePrice;
            }
        }

        return new PositionState(trade.key(),
                normalizeQty(newQty),
                normalizePrice(newEntryPrice),
                normalizeAmount(newRealized),
                trade.timestamp());
    }

    private Position nextPosition(Position current, PositionFill fill, PositionKey key) {
        Position existing = current == null ? Position.empty(key, fill.mode()) : current;
        BigDecimal fillQty = normalizeQty(fill.quantity());
        BigDecimal fillPrice = normalizePrice(fill.price());
        BigDecimal existingQty = normalizeQty(existing.quantity());
        BigDecimal existingEntry = normalizePrice(existing.entryPrice());
        BigDecimal existingRealized = normalizeAmount(existing.realizedPnl());
        boolean fillIsBuy = fill.side() == OrderSide.BUY;
        PositionSide fillSide = fillIsBuy ? PositionSide.LONG : PositionSide.SHORT;

        if (existing.side() == PositionSide.FLAT || existingQty.compareTo(ZERO) == 0) {
            BigDecimal margin = calcMargin(fillQty, fillPrice);
            return new Position(key.userId(), key.symbol(), fillSide, fillQty,
                    fillPrice, margin, existingRealized, fill.mode());
        }

        if (existing.side() == fillSide) {
            BigDecimal totalQty = existingQty.add(fillQty);
            BigDecimal weighted = existingEntry.multiply(existingQty)
                    .add(fillPrice.multiply(fillQty));
            BigDecimal entryPrice = weighted.divide(totalQty, PRICE_SCALE, ROUNDING);
            BigDecimal margin = calcMargin(totalQty, entryPrice);
            return new Position(key.userId(), key.symbol(), existing.side(), totalQty, entryPrice,
                    margin, existingRealized, existing.mode());
        }

        BigDecimal closedQty = existingQty.min(fillQty);
        BigDecimal pnlPerUnit = existing.side() == PositionSide.LONG
                ? fillPrice.subtract(existingEntry)
                : existingEntry.subtract(fillPrice);
        BigDecimal realized = normalizeAmount(existingRealized.add(pnlPerUnit.multiply(closedQty)));
        BigDecimal remainingQty = existingQty.subtract(closedQty);

        if (remainingQty.compareTo(ZERO) > 0) {
            BigDecimal margin = calcMargin(remainingQty, existingEntry);
            return new Position(key.userId(), key.symbol(), existing.side(), remainingQty, existingEntry,
                    margin, realized, existing.mode());
        }

        if (fillQty.compareTo(existingQty) == 0) {
            BigDecimal zeroQty = normalizeQty(ZERO);
            BigDecimal zeroPrice = normalizePrice(ZERO);
            BigDecimal zeroAmount = normalizeAmount(ZERO);
            return new Position(key.userId(), key.symbol(), PositionSide.FLAT, zeroQty, zeroPrice,
                    zeroAmount, realized, existing.mode());
        }

        BigDecimal flippedQty = fillQty.subtract(existingQty);
        BigDecimal margin = calcMargin(flippedQty, fillPrice);
        return new Position(key.userId(), key.symbol(), fillSide, flippedQty, fillPrice,
                margin, realized, existing.mode());
    }

    private BigDecimal unrealizedPnl(Position position, BigDecimal markPrice) {
        BigDecimal entry = normalizePrice(position.entryPrice());
        if (position.side() == PositionSide.LONG) {
            return markPrice.subtract(entry).multiply(normalizeQty(position.quantity()));
        }
        return entry.subtract(markPrice).multiply(normalizeQty(position.quantity()));
    }

    private BigDecimal liquidationPrice(Position position, BigDecimal qty) {
        if (position.side() == PositionSide.LONG) {
            BigDecimal numerator = normalizePrice(position.entryPrice()).multiply(qty).subtract(position.margin());
            BigDecimal denominator = qty.multiply(BigDecimal.ONE.subtract(mmr));
            return safeDivide(numerator, denominator);
        }
        BigDecimal numerator = position.margin().add(normalizePrice(position.entryPrice()).multiply(qty));
        BigDecimal denominator = qty.multiply(BigDecimal.ONE.add(mmr));
        return safeDivide(numerator, denominator);
    }

    private BigDecimal bankruptcyPrice(Position position, BigDecimal qty) {
        if (position.side() == PositionSide.LONG) {
            return safeDivide(normalizePrice(position.entryPrice()).multiply(qty).subtract(position.margin()), qty);
        }
        return safeDivide(normalizePrice(position.entryPrice()).multiply(qty).add(position.margin()), qty);
    }

    private BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.compareTo(ZERO) == 0) {
            return normalizePrice(ZERO);
        }
        return normalizeRisk(numerator.divide(denominator, RISK_SCALE, ROUNDING));
    }

    private BigDecimal calcMargin(BigDecimal qty, BigDecimal entryPrice) {
        return normalizeAmount(qty.multiply(entryPrice).multiply(imr));
    }

    private void validateTrade(PositionTrade trade) {
        if (trade.side() == null) {
            throw new PositionException(PositionErrorCode.INVALID_SIDE, "side must not be null");
        }
        if (normalizePrice(trade.price()).compareTo(ZERO) <= 0) {
            throw new PositionException(PositionErrorCode.INVALID_PRICE, "price must be > 0");
        }
        if (normalizeQty(trade.quantity()).compareTo(ZERO) <= 0) {
            throw new PositionException(PositionErrorCode.INVALID_QUANTITY, "quantity must be > 0");
        }
    }

    private void validateFill(PositionFill fill) {
        if (fill.side() == null) {
            throw new PositionException(PositionErrorCode.INVALID_SIDE, "side must not be null");
        }
        if (fill.mode() == null) {
            throw new PositionException(PositionErrorCode.INVALID_MODE, "mode must not be null");
        }
        if (normalizePrice(fill.price()).compareTo(ZERO) <= 0) {
            throw new PositionException(PositionErrorCode.INVALID_PRICE, "price must be > 0");
        }
        if (normalizeQty(fill.quantity()).compareTo(ZERO) <= 0) {
            throw new PositionException(PositionErrorCode.INVALID_QUANTITY, "quantity must be > 0");
        }
    }

    private static BigDecimal normalizePrice(BigDecimal value) {
        return value.setScale(PRICE_SCALE, ROUNDING);
    }

    private static BigDecimal normalizeQty(BigDecimal value) {
        return value.setScale(QTY_SCALE, ROUNDING);
    }

    private static BigDecimal normalizeAmount(BigDecimal value) {
        return value.setScale(AMOUNT_SCALE, ROUNDING);
    }

    private static BigDecimal normalizeRisk(BigDecimal value) {
        return value.setScale(RISK_SCALE, ROUNDING);
    }

    private static BigDecimal normalizeRatio(BigDecimal value) {
        return value.setScale(RISK_SCALE, ROUNDING);
    }
}
