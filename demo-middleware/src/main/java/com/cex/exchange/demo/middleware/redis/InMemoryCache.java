package com.cex.exchange.demo.middleware.redis;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryCache 核心类。
 */
public class InMemoryCache {
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    public void put(String key, String value, long ttlMillis, long nowMillis) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis must be > 0");
        }
        store.put(key, new CacheEntry(value, nowMillis + ttlMillis));
    }

    public Optional<String> get(String key, long nowMillis) {
        Objects.requireNonNull(key, "key");
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAtMillis() <= nowMillis) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }
}
