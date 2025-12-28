package com.cex.exchange.demo.middleware.outbox;

/**
 * OutboxEvent 记录类型。
 */
public record OutboxEvent(String eventId, String key, String payload) {
}
