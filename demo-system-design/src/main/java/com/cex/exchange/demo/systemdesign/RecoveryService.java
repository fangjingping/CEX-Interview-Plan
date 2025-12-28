package com.cex.exchange.demo.systemdesign;

import java.util.List;

/**
 * RecoveryService 核心类。
 */
public class RecoveryService {
    private final EventLog eventLog;
    private final SnapshotStore snapshotStore;

    public RecoveryService(EventLog eventLog, SnapshotStore snapshotStore) {
        this.eventLog = eventLog;
        this.snapshotStore = snapshotStore;
    }

    public RecoveredState recover(String initialState, StateApplier applier) {
        long lastSequence = 0;
        String state = initialState;
        Snapshot snapshot = snapshotStore.latest().orElse(null);
        if (snapshot != null) {
            lastSequence = snapshot.sequence();
            state = snapshot.state();
        }

        List<StoredEvent> events = eventLog.readFrom(lastSequence);
        for (StoredEvent stored : events) {
            state = applier.apply(state, stored.event());
            lastSequence = stored.sequence();
        }
        return new RecoveredState(state, lastSequence);
    }
}
