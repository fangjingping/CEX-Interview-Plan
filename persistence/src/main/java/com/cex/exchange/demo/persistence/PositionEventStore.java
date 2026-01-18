package com.cex.exchange.demo.persistence;

import com.cex.exchange.demo.position.PositionKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class PositionEventStore {
    private final Map<PositionKey, List<StoredPositionEvent>> events = new ConcurrentHashMap<>();
    private final Map<PositionKey, AtomicLong> sequences = new ConcurrentHashMap<>();
    private final Map<String, StoredPositionEvent> eventIndex = new ConcurrentHashMap<>();
    private final Map<PositionKey, Object> locks = new ConcurrentHashMap<>();

    public StoredPositionEvent append(PositionEvent event) {
        Objects.requireNonNull(event, "event");
        StoredPositionEvent existing = eventIndex.get(event.eventId());
        if (existing != null) {
            return existing;
        }
        Object lock = locks.computeIfAbsent(event.key(), key -> new Object());
        synchronized (lock) {
            existing = eventIndex.get(event.eventId());
            if (existing != null) {
                return existing;
            }
            long version = sequences.computeIfAbsent(event.key(), key -> new AtomicLong(0)).incrementAndGet();
            StoredPositionEvent stored = new StoredPositionEvent(version, event);
            events.computeIfAbsent(event.key(), key -> new ArrayList<>()).add(stored);
            eventIndex.put(event.eventId(), stored);
            return stored;
        }
    }

    public List<StoredPositionEvent> load(PositionKey key, long afterVersion) {
        Objects.requireNonNull(key, "key");
        Object lock = locks.computeIfAbsent(key, k -> new Object());
        synchronized (lock) {
            List<StoredPositionEvent> stored = events.get(key);
            if (stored == null || stored.isEmpty()) {
                return List.of();
            }
            List<StoredPositionEvent> result = new ArrayList<>();
            for (StoredPositionEvent event : stored) {
                if (event.version() > afterVersion) {
                    result.add(event);
                }
            }
            return List.copyOf(result);
        }
    }
}
