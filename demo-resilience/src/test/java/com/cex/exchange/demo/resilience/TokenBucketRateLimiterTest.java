package com.cex.exchange.demo.resilience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TokenBucketRateLimiterTest 单元测试。
 */
class TokenBucketRateLimiterTest {

    @Test
    void refillsOverTime() {
        long start = 0L;
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(2, 2, start);

        assertTrue(limiter.tryAcquire(start));
        assertTrue(limiter.tryAcquire(start));
        assertFalse(limiter.tryAcquire(start));

        long halfSecond = 500_000_000L;
        assertTrue(limiter.tryAcquire(halfSecond));
    }
}
