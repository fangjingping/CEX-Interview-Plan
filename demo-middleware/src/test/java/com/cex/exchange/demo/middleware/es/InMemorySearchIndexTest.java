package com.cex.exchange.demo.middleware.es;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * InMemorySearchIndexTest 单元测试。
 */
class InMemorySearchIndexTest {

    @Test
    void findsDocumentsByTerm() {
        InMemorySearchIndex index = new InMemorySearchIndex();
        index.index("D1", "btc usdt trade");
        index.index("D2", "eth usdt position");

        List<String> results = index.search("btc");
        assertTrue(results.contains("D1"));
    }
}
