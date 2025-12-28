package com.cex.exchange.demo.systemdesign;

import java.util.Optional;

/**
 * SnapshotStore 接口定义。
 */
public interface SnapshotStore {
    Optional<Snapshot> latest();

    void save(Snapshot snapshot);
}
