package com.cex.exchange.demo.systemdesign;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * InMemorySnapshotStore 核心类。
 */
public class InMemorySnapshotStore implements SnapshotStore {
    private final AtomicReference<Snapshot> latest = new AtomicReference<>();

    @Override
    public Optional<Snapshot> latest() {
        return Optional.ofNullable(latest.get());
    }

    @Override
    public void save(Snapshot snapshot) {
        latest.set(snapshot);
    }
}
