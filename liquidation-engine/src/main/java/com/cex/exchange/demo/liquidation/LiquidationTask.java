package com.cex.exchange.demo.liquidation;

import java.math.BigDecimal;
import java.util.Objects;

public record LiquidationTask(String taskId,
                              String userId,
                              String symbol,
                              BigDecimal markPrice,
                              BigDecimal priority,
                              long timestamp) {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public LiquidationTask {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        Objects.requireNonNull(markPrice, "markPrice");
        Objects.requireNonNull(priority, "priority");
        if (priority.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("priority must be >= 0");
        }
    }
}
