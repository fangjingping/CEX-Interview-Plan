package com.cex.exchange.demo.persistence;

import java.util.Objects;

public record Event(String eventId, String type, long timestamp, String payload) {
    public Event {
        if (eventId == null || eventId.isBlank()) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_EVENT, "eventId must not be blank");
        }
        if (type == null || type.isBlank()) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_EVENT, "type must not be blank");
        }
        if (eventId.contains("|") || type.contains("|")) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_EVENT, "eventId/type must not contain '|'");
        }
        if (timestamp <= 0) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_EVENT, "timestamp must be > 0");
        }
        Objects.requireNonNull(payload, "payload");
    }
}
