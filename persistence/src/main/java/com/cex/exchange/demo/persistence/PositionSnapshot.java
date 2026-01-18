package com.cex.exchange.demo.persistence;

import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionState;

import java.util.Objects;

public record PositionSnapshot(PositionKey key, PositionState state, long version, long timestamp) {
    public PositionSnapshot {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(state, "state");
        if (version < 0) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_SNAPSHOT, "version must be >= 0");
        }
    }
}
