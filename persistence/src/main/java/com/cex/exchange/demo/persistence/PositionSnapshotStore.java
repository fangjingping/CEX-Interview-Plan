package com.cex.exchange.demo.persistence;

import com.cex.exchange.demo.position.PositionKey;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PositionSnapshotStore {
    private final Map<PositionKey, PositionSnapshot> snapshots = new ConcurrentHashMap<>();

    public void save(PositionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        snapshots.compute(snapshot.key(), (key, existing) -> {
            if (existing == null || snapshot.version() > existing.version()) {
                return snapshot;
            }
            return existing;
        });
    }

    public Optional<PositionSnapshot> latest(PositionKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(snapshots.get(key));
    }
}
