package com.cex.exchange.demo.resilience;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ConfigStore 核心类。
 */
public class ConfigStore {
    private final AtomicReference<ConfigSnapshot> current =
            new AtomicReference<>(new ConfigSnapshot(0, Map.of()));

    public ConfigSnapshot get() {
        return current.get();
    }

    public ConfigSnapshot update(Map<String, String> values) {
        Map<String, String> copy = new HashMap<>(values);
        ConfigSnapshot next = new ConfigSnapshot(current.get().version() + 1, Map.copyOf(copy));
        current.set(next);
        return next;
    }
}
