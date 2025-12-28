package com.cex.exchange.demo.resilience;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ConfigStoreTest 单元测试。
 */
class ConfigStoreTest {

    @Test
    void updatesVersionedConfig() {
        ConfigStore store = new ConfigStore();
        ConfigSnapshot first = store.get();
        ConfigSnapshot updated = store.update(Map.of("limit", "1000"));

        assertEquals(0, first.version());
        assertEquals(1, updated.version());
        assertEquals("1000", updated.values().get("limit"));
    }
}
