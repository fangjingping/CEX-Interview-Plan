package com.cex.exchange.demo.middleware;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TtlIdempotencyStore 核心类。
 */
public class TtlIdempotencyStore implements IdempotencyStore {
    private enum State {
        IN_FLIGHT,
        PROCESSED
    }

    private final long ttlMillis;
    private final TimeSource timeSource;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    public TtlIdempotencyStore(long ttlMillis, TimeSource timeSource) {
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis must be > 0");
        }
        this.ttlMillis = ttlMillis;
        this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
    }

    @Override
    public boolean tryStart(String messageId) {
        Objects.requireNonNull(messageId, "messageId");
        long now = timeSource.nowMillis();
        AtomicBoolean inserted = new AtomicBoolean(false);
        entries.compute(messageId, (id, existing) -> {
            if (existing == null || existing.expiresAtMillis <= now) {
                inserted.set(true);
                return new Entry(State.IN_FLIGHT, now + ttlMillis);
            }
            return existing;
        });
        return inserted.get();
    }

    @Override
    public void markSuccess(String messageId) {
        Objects.requireNonNull(messageId, "messageId");
        long now = timeSource.nowMillis();
        entries.computeIfPresent(messageId, (id, existing) -> new Entry(State.PROCESSED, now + ttlMillis));
        cleanupExpired();
    }

    @Override
    public void release(String messageId) {
        entries.remove(messageId);
    }

    public void cleanupExpired() {
        long now = timeSource.nowMillis();
        for (Map.Entry<String, Entry> entry : entries.entrySet()) {
            if (entry.getValue().expiresAtMillis <= now) {
                entries.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    private static final class Entry {
        private final State state;
        private final long expiresAtMillis;

        private Entry(State state, long expiresAtMillis) {
            this.state = state;
            this.expiresAtMillis = expiresAtMillis;
        }
    }
}
