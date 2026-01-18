package com.cex.exchange.demo.risk;

import java.math.BigDecimal;
import java.util.Objects;

public record RiskHold(String requestId,
                       String userId,
                       BigDecimal reserved,
                       BigDecimal consumed,
                       RiskHoldStatus status) {
    public RiskHold {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        Objects.requireNonNull(reserved, "reserved");
        Objects.requireNonNull(consumed, "consumed");
        Objects.requireNonNull(status, "status");
    }

    public BigDecimal remaining() {
        return reserved.subtract(consumed);
    }
}
