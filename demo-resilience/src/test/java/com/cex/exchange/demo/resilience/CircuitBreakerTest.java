package com.cex.exchange.demo.resilience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CircuitBreakerTest 单元测试。
 */
class CircuitBreakerTest {

    @Test
    void opensAfterFailuresAndHalfOpens() {
        CircuitBreaker breaker = new CircuitBreaker(2, 1_000L);
        long now = 10_000L;

        assertTrue(breaker.allowRequest(now));
        breaker.recordFailure(now);
        breaker.recordFailure(now);

        assertEquals(CircuitState.OPEN, breaker.getState());
        assertFalse(breaker.allowRequest(now));

        long later = now + 1_000L;
        assertTrue(breaker.allowRequest(later));
        breaker.recordSuccess();
        assertEquals(CircuitState.CLOSED, breaker.getState());
    }

    @Test
    void halfOpenAllowsSingleTrial() {
        CircuitBreaker breaker = new CircuitBreaker(1, 1_000L);
        long now = 5_000L;

        breaker.recordFailure(now);
        assertEquals(CircuitState.OPEN, breaker.getState());

        long later = now + 1_000L;
        assertTrue(breaker.allowRequest(later));
        assertFalse(breaker.allowRequest(later));

        breaker.recordFailure(later);
        assertEquals(CircuitState.OPEN, breaker.getState());
    }
}
