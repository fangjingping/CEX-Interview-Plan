package com.cex.exchange.demo.persistence;

import java.util.Optional;

public interface SnapshotStore {
    void save(Snapshot snapshot);

    Optional<Snapshot> load();
}
