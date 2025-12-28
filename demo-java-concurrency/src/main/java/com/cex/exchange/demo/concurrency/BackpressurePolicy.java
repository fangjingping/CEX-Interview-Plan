package com.cex.exchange.demo.concurrency;

/**
 * BackpressurePolicy 接口定义。
 */
public interface BackpressurePolicy {
    boolean allow(int queueSize, int capacity);
}
