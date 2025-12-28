package com.cex.exchange.demo.middleware.es;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BufferedSearchIndexTest 单元测试。
 */
class BufferedSearchIndexTest {

    @Test
    void requiresRefreshToSearch() {
        BufferedSearchIndex index = new BufferedSearchIndex();
        index.index("D1", "btc usdt");

        assertTrue(index.search("btc").isEmpty());
        assertEquals(1, index.pendingCount());

        index.refresh();
        assertEquals(0, index.pendingCount());
        assertEquals(1, index.search("btc").size());
    }
}
