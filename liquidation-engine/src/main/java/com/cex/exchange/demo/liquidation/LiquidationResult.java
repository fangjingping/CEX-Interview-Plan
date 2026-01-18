package com.cex.exchange.demo.liquidation;

import java.util.List;
import java.util.Objects;

public record LiquidationResult(String taskId,
                                String userId,
                                String symbol,
                                LiquidationStatus status,
                                List<LiquidationTrade> trades) {
    public LiquidationResult {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(trades, "trades");
        trades = List.copyOf(trades);
    }
}
