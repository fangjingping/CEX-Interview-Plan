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
        if (!store.tryStart(message.id())) {
            return false;
        }
        try {
            handler.handle(message);
            store.markSuccess(message.id());
            return true;
        } catch (RuntimeException ex) {
            store.release(message.id());
            throw ex;
        }
    }
}
