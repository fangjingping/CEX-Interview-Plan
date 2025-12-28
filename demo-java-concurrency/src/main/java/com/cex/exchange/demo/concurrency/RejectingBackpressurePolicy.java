package com.cex.exchange.demo.concurrency;

/**
 * RejectingBackpressurePolicy 核心类。
 */
public class RejectingBackpressurePolicy implements BackpressurePolicy {
    @Override
    public boolean allow(int queueSize, int capacity) {
        return queueSize < capacity;
    }
}
