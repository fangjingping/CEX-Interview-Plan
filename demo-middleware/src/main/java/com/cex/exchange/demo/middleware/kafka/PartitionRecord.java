package com.cex.exchange.demo.middleware.kafka;

/**
 * PartitionRecord 记录类型。
 */
public record PartitionRecord(int partition, int offset, String key, String payload) {
}
