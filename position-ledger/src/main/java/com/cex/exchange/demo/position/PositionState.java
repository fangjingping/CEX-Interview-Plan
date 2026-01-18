package com.cex.exchange.demo.position;

import java.math.BigDecimal;
import java.util.Objects;

public record PositionState(PositionKey key,
                            BigDecimal quantity,
                            BigDecimal entryPrice,
                            BigDecimal realizedPnl,
                            long updatedAt) {
    public PositionState {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(quantity, "quantity");
        Objects.requireNonNull(entryPrice, "entryPrice");
        Objects.requireNonNull(realizedPnl, "realizedPnl");
    }

    public static PositionState empty(PositionKey key) {
        return new PositionState(key, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
    }
}
