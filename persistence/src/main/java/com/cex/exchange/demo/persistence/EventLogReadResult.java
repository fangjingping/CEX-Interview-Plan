package com.cex.exchange.demo.persistence;

import java.util.List;
import java.util.Objects;

public record EventLogReadResult(List<Event> events, int skippedLines) {
    public EventLogReadResult {
        Objects.requireNonNull(events, "events");
        if (skippedLines < 0) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_LOG_LINE, "skippedLines must be >= 0");
        }
        events = List.copyOf(events);
    }
}
