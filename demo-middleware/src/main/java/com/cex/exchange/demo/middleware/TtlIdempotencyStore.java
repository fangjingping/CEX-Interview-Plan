package com.cex.exchange.demo.middleware;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TtlIdempotencyStore 核心类。
 */
public class TtlIdempotencyStore implements IdempotencyStore {
    private final long ttlMillis;
    private final TimeSource timeSource;
    private final Map<String, Long> expiresAtById = new ConcurrentHashMap<>();

    public TtlIdempotencyStore(long ttlMillis, TimeSource timeSource) {
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis must be > 0");
        }
        this.ttlMillis = ttlMillis;
        this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
    }

    @Override
    public boolean markIfAbsent(String messageId) {
        Objects.requireNonNull(messageId, "messageId");
        long now = timeSource.nowMillis();
        long expiresAt = now + ttlMillis;
        AtomicBoolean inserted = new AtomicBoolean(false);
        expiresAtById.compute(messageId, (id, existing) -> {
            if (existing == null || existing <= now) {
                inserted.set(true);
                return expiresAt;
            }
            return existing;
        });
        return inserted.get();
    }

    public void cleanupExpired() {
        long now = timeSource.nowMillis();
        for (Map.Entry<String, Long> entry : expiresAtById.entrySet()) {
            if (entry.getValue() <= now) {
                expiresAtById.remove(entry.getKey(), entry.getValue());
            }
        }
    }
}
