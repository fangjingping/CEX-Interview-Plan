package com.cex.exchange.demo.middleware;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryIdempotencyStore 核心类。
 */
public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    @Override
    public boolean markIfAbsent(String messageId) {
        return processed.add(messageId);
    }
}
