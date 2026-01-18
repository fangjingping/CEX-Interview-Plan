package com.cex.exchange.demo.position;

import java.math.BigDecimal;
import java.util.Objects;

public record PositionClose(String userId,
                            String symbol,
                            PositionSide side,
                            BigDecimal quantity,
                            BigDecimal closePrice,
                            BigDecimal realizedPnlDelta,
                            long timestamp) {
    public PositionClose {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(quantity, "quantity");
        Objects.requireNonNull(closePrice, "closePrice");
        Objects.requireNonNull(realizedPnlDelta, "realizedPnlDelta");
    }
}
