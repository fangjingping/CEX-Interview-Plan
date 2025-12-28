package com.cex.exchange.demo.resilience;

/**
 * CircuitBreaker 核心类。
 */
public class CircuitBreaker {
    private final int failureThreshold;
    private final long openDurationMillis;
    private CircuitState state = CircuitState.CLOSED;
    private int failureCount = 0;
    private long openedAtMillis = 0;

    public CircuitBreaker(int failureThreshold, long openDurationMillis) {
        if (failureThreshold <= 0 || openDurationMillis <= 0) {
            throw new IllegalArgumentException("failureThreshold and openDurationMillis must be > 0");
        }
        this.failureThreshold = failureThreshold;
        this.openDurationMillis = openDurationMillis;
    }

    public synchronized boolean allowRequest(long nowMillis) {
        if (state == CircuitState.OPEN) {
            if (nowMillis - openedAtMillis >= openDurationMillis) {
                state = CircuitState.HALF_OPEN;
                return true;
            }
            return false;
        }
        return true;
    }

    public synchronized void recordSuccess() {
        failureCount = 0;
        state = CircuitState.CLOSED;
    }

    public synchronized void recordFailure(long nowMillis) {
        if (state == CircuitState.HALF_OPEN) {
            open(nowMillis);
            return;
        }
        failureCount += 1;
        if (failureCount >= failureThreshold) {
            open(nowMillis);
        }
    }

    public synchronized CircuitState getState() {
        return state;
    }

    private void open(long nowMillis) {
        state = CircuitState.OPEN;
        openedAtMillis = nowMillis;
    }
}
