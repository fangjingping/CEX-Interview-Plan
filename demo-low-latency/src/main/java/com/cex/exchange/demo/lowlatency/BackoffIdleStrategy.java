package com.cex.exchange.demo.lowlatency;

import java.util.concurrent.locks.LockSupport;

/**
 * BackoffIdleStrategy 核心类。
 */
public class BackoffIdleStrategy implements IdleStrategy {
    private final int spinCount;
    private final int yieldCount;
    private final long parkNanos;
    private int idleCount = 0;

    public BackoffIdleStrategy(int spinCount, int yieldCount, long parkNanos) {
        if (spinCount < 0 || yieldCount < 0 || parkNanos < 0) {
            throw new IllegalArgumentException("spinCount/yieldCount/parkNanos must be >= 0");
        }
        this.spinCount = spinCount;
        this.yieldCount = yieldCount;
        this.parkNanos = parkNanos;
    }

    @Override
    public void idle() {
        if (idleCount < spinCount) {
            idleCount++;
            Thread.onSpinWait();
            return;
        }
        if (idleCount < spinCount + yieldCount) {
            idleCount++;
            Thread.yield();
            return;
        }
        LockSupport.parkNanos(parkNanos);
    }

    @Override
    public void reset() {
        idleCount = 0;
    }
}
