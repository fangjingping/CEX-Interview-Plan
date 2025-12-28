package com.cex.exchange.demo.systemdesign;

/**
 * Snapshot 记录类型。
 */
public record Snapshot(long sequence, String state) {
}
