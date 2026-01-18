package com.cex.exchange.demo.persistence;

import java.util.Objects;

public record RecoveryResult<T>(T state, int appliedEvents, int skippedEvents, int skippedLines) {
    public RecoveryResult {
        Objects.requireNonNull(state, "state");
        if (appliedEvents < 0 || skippedEvents < 0 || skippedLines < 0) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_STATE, "counts must be >= 0");
        }
    }
}
