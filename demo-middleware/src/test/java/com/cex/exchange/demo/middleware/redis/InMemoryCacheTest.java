package com.cex.exchange.demo.middleware.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * InMemoryCacheTest 单元测试。
 */
class InMemoryCacheTest {

    @Test
    void expiresAfterTtl() {
        InMemoryCache cache = new InMemoryCache();
        long now = 1_000L;
        cache.put("key", "value", 500L, now);

        assertTrue(cache.get("key", now + 100L).isPresent());
        assertFalse(cache.get("key", now + 600L).isPresent());
    }
}
