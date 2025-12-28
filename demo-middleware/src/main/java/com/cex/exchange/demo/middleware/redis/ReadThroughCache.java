package com.cex.exchange.demo.middleware.redis;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * ReadThroughCache 核心类。
 */
public class ReadThroughCache {
    private final InMemoryCache cache;
    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    public ReadThroughCache(InMemoryCache cache) {
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    public String getOrLoad(String key, long ttlMillis, long nowMillis, Supplier<String> loader) {
        Objects.requireNonNull(loader, "loader");
        Optional<String> cached = cache.get(key, nowMillis);
        if (cached.isPresent()) {
            return cached.get();
        }
        Object lock = locks.computeIfAbsent(key, value -> new Object());
        try {
            synchronized (lock) {
                Optional<String> secondCheck = cache.get(key, nowMillis);
                if (secondCheck.isPresent()) {
                    return secondCheck.get();
                }
                String loaded = loader.get();
                cache.put(key, loaded, ttlMillis, nowMillis);
                return loaded;
            }
        } finally {
            locks.remove(key, lock);
        }
    }
}
