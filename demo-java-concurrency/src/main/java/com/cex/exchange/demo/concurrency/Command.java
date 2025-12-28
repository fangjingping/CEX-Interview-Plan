package com.cex.exchange.demo.concurrency;

/**
 * Command 记录类型。
 */
public record Command(long sequence, String payload, long createdAt) {
}
