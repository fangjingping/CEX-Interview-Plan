package com.cex.exchange.demo.middleware.outbox;

import com.cex.exchange.demo.middleware.kafka.PartitionedLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OutboxPublisher 核心类。
 */
public class OutboxPublisher {
    private final OutboxStore store;
    private final PartitionedLog log;

    public OutboxPublisher(OutboxStore store, PartitionedLog log) {
        this.store = Objects.requireNonNull(store, "store");
        this.log = Objects.requireNonNull(log, "log");
    }

    public int publish(int limit) {
        List<OutboxEvent> events = store.pending(limit);
        if (events.isEmpty()) {
            return 0;
        }
        List<String> published = new ArrayList<>();
        for (OutboxEvent event : events) {
            log.appendIfAbsent(event.eventId(), event.key(), event.payload());
            published.add(event.eventId());
        }
        store.markPublished(published);
        return published.size();
    }
}
