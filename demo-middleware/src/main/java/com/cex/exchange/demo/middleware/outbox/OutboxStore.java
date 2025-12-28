package com.cex.exchange.demo.middleware.outbox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OutboxStore 核心类。
 */
public class OutboxStore {
    private final Map<String, OutboxRecord> records = new LinkedHashMap<>();

    public synchronized void add(OutboxEvent event) {
        Objects.requireNonNull(event, "event");
        records.putIfAbsent(event.eventId(), new OutboxRecord(event));
    }

    public synchronized List<OutboxEvent> pending(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be > 0");
        }
        List<OutboxEvent> result = new ArrayList<>();
        for (OutboxRecord record : records.values()) {
            if (!record.published) {
                result.add(record.event);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    public synchronized void markPublished(List<String> eventIds) {
        for (String eventId : eventIds) {
            OutboxRecord record = records.get(eventId);
            if (record != null) {
                record.published = true;
            }
        }
    }

    private static final class OutboxRecord {
        private final OutboxEvent event;
        private boolean published;

        private OutboxRecord(OutboxEvent event) {
            this.event = event;
        }
    }
}
