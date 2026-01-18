package com.cex.exchange.demo.position;

import java.math.BigDecimal;
import java.util.Objects;

public record Position(String userId,
                       String symbol,
                       PositionSide side,
                       BigDecimal quantity,
                       BigDecimal entryPrice,
                       BigDecimal margin,
                       BigDecimal realizedPnl,
                       PositionMode mode) {
    public Position {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(quantity, "quantity");
        Objects.requireNonNull(entryPrice, "entryPrice");
        Objects.requireNonNull(margin, "margin");
        Objects.requireNonNull(realizedPnl, "realizedPnl");
        Objects.requireNonNull(mode, "mode");
    }

    public static Position empty(PositionKey key, PositionMode mode) {
        return new Position(key.userId(), key.symbol(), PositionSide.FLAT,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, mode);
    }
}
