package com.cex.exchange.demo.jvmtuning;

/**
 * GcPauseStats 核心类。
 */
public class GcPauseStats {
    private final int count;
    private final double averageMillis;
    private final double maxMillis;
    private final double p95Millis;
    private final double p99Millis;

    public GcPauseStats(int count, double averageMillis, double maxMillis, double p95Millis, double p99Millis) {
        this.count = count;
        this.averageMillis = averageMillis;
        this.maxMillis = maxMillis;
        this.p95Millis = p95Millis;
        this.p99Millis = p99Millis;
    }

    public int getCount() {
        return count;
    }

    public double getAverageMillis() {
        return averageMillis;
    }

    public double getMaxMillis() {
        return maxMillis;
    }

    public double getP95Millis() {
        return p95Millis;
    }

    public double getP99Millis() {
        return p99Millis;
    }
}
