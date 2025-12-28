package com.cex.exchange.demo.systemdesign;

/**
 * RecoveredState 记录类型。
 */
public record RecoveredState(String state, long lastSequence) {
}
