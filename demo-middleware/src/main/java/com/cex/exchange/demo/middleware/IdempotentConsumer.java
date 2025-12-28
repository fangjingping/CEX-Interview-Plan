package com.cex.exchange.demo.middleware;

import java.util.Objects;

/**
 * IdempotentConsumer 核心类。
 */
public class IdempotentConsumer {
    private final IdempotencyStore store;

    public IdempotentConsumer(IdempotencyStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public boolean handle(Message message, MessageHandler handler) {
        if (!store.markIfAbsent(message.id())) {
            return false;
        }
        handler.handle(message);
        return true;
    }
}
