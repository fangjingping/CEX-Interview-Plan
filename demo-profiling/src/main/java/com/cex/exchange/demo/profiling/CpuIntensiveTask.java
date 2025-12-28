package com.cex.exchange.demo.profiling;

/**
 * CpuIntensiveTask 核心类。
 */
public class CpuIntensiveTask {
    public long compute(long seed, int iterations) {
        long value = seed;
        for (int i = 0; i < iterations; i++) {
            value = value * 31 + i;
        }
        return value;
    }
}
