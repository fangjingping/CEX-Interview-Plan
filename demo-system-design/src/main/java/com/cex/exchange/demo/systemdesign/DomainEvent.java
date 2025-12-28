package com.cex.exchange.demo.systemdesign;

/**
 * DomainEvent 记录类型。
 */
public record DomainEvent(String type, String payload, long timestamp) {
}
