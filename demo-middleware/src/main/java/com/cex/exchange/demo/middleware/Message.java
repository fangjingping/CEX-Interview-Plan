package com.cex.exchange.demo.middleware;

/**
 * Message 记录类型。
 */
public record Message(String id, String payload, long createdAt) {
}
