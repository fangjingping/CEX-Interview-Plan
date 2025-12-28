package com.cex.exchange.demo.systemdesign;

/**
 * StoredEvent 记录类型。
 */
public record StoredEvent(long sequence, DomainEvent event) {
}
