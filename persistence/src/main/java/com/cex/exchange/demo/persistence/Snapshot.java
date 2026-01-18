package com.cex.exchange.demo.persistence;

import java.util.Objects;

public record Snapshot(String snapshotId, String lastEventId, long lastEventTimestamp, long timestamp, String payload) {
    public Snapshot {
        if (snapshotId == null || snapshotId.isBlank()) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_SNAPSHOT, "snapshotId must not be blank");
        }
        if (snapshotId.contains("|")) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_SNAPSHOT, "snapshotId must not contain '|'");
        }
        Objects.requireNonNull(lastEventId, "lastEventId");
        if (lastEventId.contains("|")) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_SNAPSHOT, "lastEventId must not contain '|'");
        }
        if (lastEventTimestamp > 0 && lastEventId.isBlank()) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_SNAPSHOT, "lastEventId must not be blank");
        }
        if (lastEventTimestamp < 0 || timestamp < 0) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_SNAPSHOT, "timestamps must be >= 0");
        }
        Objects.requireNonNull(payload, "payload");
    }
}
