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
    private boolean halfOpenTrialUsed = false;

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
                halfOpenTrialUsed = true;
                return true;
            }
            return false;
        }
        if (state == CircuitState.HALF_OPEN) {
            if (halfOpenTrialUsed) {
                return false;
            }
            halfOpenTrialUsed = true;
            return true;
        }
        return true;
    }

    public synchronized void recordSuccess() {
        failureCount = 0;
        state = CircuitState.CLOSED;
        halfOpenTrialUsed = false;
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
        halfOpenTrialUsed = false;
    }
}
