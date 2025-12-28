package com.cex.exchange.demo.middleware;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IdempotentConsumerTest 单元测试。
 */
class IdempotentConsumerTest {

    @Test
    void processesEachMessageOnce() {
        IdempotentConsumer consumer = new IdempotentConsumer(new InMemoryIdempotencyStore());
        AtomicInteger handled = new AtomicInteger();

        Message message = new Message("m1", "payload", 1L);
        assertTrue(consumer.handle(message, msg -> handled.incrementAndGet()));
        assertFalse(consumer.handle(message, msg -> handled.incrementAndGet()));

        assertEquals(1, handled.get());
    }

    @Test
    void processesDistinctMessages() {
        IdempotentConsumer consumer = new IdempotentConsumer(new InMemoryIdempotencyStore());
        AtomicInteger handled = new AtomicInteger();

        assertTrue(consumer.handle(new Message("m1", "payload-1", 1L), msg -> handled.incrementAndGet()));
        assertTrue(consumer.handle(new Message("m2", "payload-2", 2L), msg -> handled.incrementAndGet()));

        assertEquals(2, handled.get());
    }

    @Test
    void releasesWhenHandlerFails() {
        IdempotentConsumer consumer = new IdempotentConsumer(new InMemoryIdempotencyStore());
        AtomicInteger handled = new AtomicInteger();

        Message message = new Message("m3", "payload-3", 3L);
        try {
            consumer.handle(message, msg -> {
                handled.incrementAndGet();
                throw new IllegalStateException("fail");
            });
        } catch (IllegalStateException ignored) {
        }

        assertTrue(consumer.handle(message, msg -> handled.incrementAndGet()));
        assertEquals(2, handled.get());
    }
}
