package com.cex.exchange.demo.resilience;

/**
 * TokenBucketRateLimiter 核心类。
 */
public class TokenBucketRateLimiter {
    private final long capacity;
    private final double refillTokensPerNanos;
    private long availableTokens;
    private long lastRefillNanos;

    public TokenBucketRateLimiter(long capacity, long refillTokensPerSecond, long startNanos) {
        if (capacity <= 0 || refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException("capacity and refillTokensPerSecond must be > 0");
        }
        this.capacity = capacity;
        this.refillTokensPerNanos = refillTokensPerSecond / 1_000_000_000d;
        this.availableTokens = capacity;
        this.lastRefillNanos = startNanos;
    }

    public synchronized boolean tryAcquire(long nowNanos) {
        refill(nowNanos);
        if (availableTokens <= 0) {
            return false;
        }
        availableTokens -= 1;
        return true;
    }

    private void refill(long nowNanos) {
        long elapsed = nowNanos - lastRefillNanos;
        if (elapsed <= 0) {
            return;
        }
        long tokensToAdd = (long) (elapsed * refillTokensPerNanos);
        if (tokensToAdd > 0) {
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillNanos = nowNanos;
        }
    }
}
