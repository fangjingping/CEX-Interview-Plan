package com.cex.exchange.demo.middleware;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TtlIdempotencyStoreTest 单元测试。
 */
class TtlIdempotencyStoreTest {

    @Test
    void allowsReuseAfterExpiry() {
        ManualTimeSource timeSource = new ManualTimeSource(1_000L);
        TtlIdempotencyStore store = new TtlIdempotencyStore(500L, timeSource);

        assertTrue(store.tryStart("m1"));
        store.markSuccess("m1");
        assertFalse(store.tryStart("m1"));

        timeSource.advance(600L);
        assertTrue(store.tryStart("m1"));
    }

    private static final class ManualTimeSource implements TimeSource {
        private long nowMillis;

        private ManualTimeSource(long nowMillis) {
            this.nowMillis = nowMillis;
        }

        @Override
        public long nowMillis() {
            return nowMillis;
        }

        private void advance(long deltaMillis) {
            nowMillis += deltaMillis;
        }
    }
}
