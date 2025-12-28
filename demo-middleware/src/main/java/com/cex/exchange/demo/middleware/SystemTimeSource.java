package com.cex.exchange.demo.middleware;

/**
 * SystemTimeSource 核心类。
 */
public class SystemTimeSource implements TimeSource {
    @Override
    public long nowMillis() {
        return System.currentTimeMillis();
    }
}
