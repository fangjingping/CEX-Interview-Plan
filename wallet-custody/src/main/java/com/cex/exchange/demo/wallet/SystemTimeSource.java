package com.cex.exchange.demo.wallet;

/**
 * SystemTimeSource 核心类。
 */
public class SystemTimeSource implements TimeSource {
    @Override
    public long nowMillis() {
        return System.currentTimeMillis();
    }
}
