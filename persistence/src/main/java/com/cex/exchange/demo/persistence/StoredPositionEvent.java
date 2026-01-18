package com.cex.exchange.demo.persistence;

import java.util.Objects;

public record StoredPositionEvent(long version, PositionEvent event) {
    public StoredPositionEvent {
        Objects.requireNonNull(event, "event");
        if (version <= 0) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_EVENT, "version must be > 0");
        }
    }
}
