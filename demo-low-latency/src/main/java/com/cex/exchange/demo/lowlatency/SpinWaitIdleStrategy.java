package com.cex.exchange.demo.lowlatency;

/**
 * SpinWaitIdleStrategy 核心类。
 */
public class SpinWaitIdleStrategy implements IdleStrategy {
    @Override
    public void idle() {
        Thread.onSpinWait();
    }

    @Override
    public void reset() {
    }
}
