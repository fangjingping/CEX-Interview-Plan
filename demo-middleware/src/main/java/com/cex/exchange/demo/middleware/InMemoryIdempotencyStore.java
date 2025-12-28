package com.cex.exchange.demo.middleware;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryIdempotencyStore 核心类。
 */
public class InMemoryIdempotencyStore implements IdempotencyStore {
    private final Set<String> processed = ConcurrentHashMap.newKeySet();
    private final Set<String> inflight = ConcurrentHashMap.newKeySet();

    @Override
    public synchronized boolean tryStart(String messageId) {
        if (processed.contains(messageId)) {
            return false;
        }
        return inflight.add(messageId);
    }

    @Override
    public synchronized void markSuccess(String messageId) {
        inflight.remove(messageId);
        processed.add(messageId);
    }

    @Override
    public synchronized void release(String messageId) {
        inflight.remove(messageId);
    }
}
