package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.Objects;

public record FrozenRecord(String orderId,
                           String userId,
                           String symbol,
                           BigDecimal frozenMargin,
                           BigDecimal frozenFee,
                           long timestamp,
                           FreezeStatus status) {
    public FrozenRecord {
        Objects.requireNonNull(frozenMargin, "frozenMargin");
        Objects.requireNonNull(frozenFee, "frozenFee");
        Objects.requireNonNull(status, "status");
    }

    public FrozenRecord withStatus(FreezeStatus nextStatus) {
        return new FrozenRecord(orderId, userId, symbol, frozenMargin, frozenFee, timestamp, nextStatus);
    }
}
