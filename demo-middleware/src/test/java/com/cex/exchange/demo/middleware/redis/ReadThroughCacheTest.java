package com.cex.exchange.demo.middleware.redis;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ReadThroughCacheTest 单元测试。
 */
class ReadThroughCacheTest {

    @Test
    void loadsOnceWithinTtl() {
        InMemoryCache cache = new InMemoryCache();
        ReadThroughCache readThrough = new ReadThroughCache(cache);
        AtomicInteger loads = new AtomicInteger();

        String first = readThrough.getOrLoad("key", 1_000L, 10L, () -> "v" + loads.incrementAndGet());
        String second = readThrough.getOrLoad("key", 1_000L, 20L, () -> "v" + loads.incrementAndGet());

        assertEquals("v1", first);
        assertEquals("v1", second);
        assertEquals(1, loads.get());
    }
}
