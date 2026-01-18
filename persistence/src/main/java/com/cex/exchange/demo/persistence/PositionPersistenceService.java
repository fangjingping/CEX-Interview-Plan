package com.cex.exchange.demo.persistence;

import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionService;
import com.cex.exchange.demo.position.PositionState;
import com.cex.exchange.demo.position.PositionTrade;

import java.util.List;
import java.util.Objects;

public class PositionPersistenceService {
    private final PositionEventStore eventStore;
    private final PositionSnapshotStore snapshotStore;

    public PositionPersistenceService() {
        this(new PositionEventStore(), new PositionSnapshotStore());
    }

    public PositionPersistenceService(PositionEventStore eventStore, PositionSnapshotStore snapshotStore) {
        this.eventStore = Objects.requireNonNull(eventStore, "eventStore");
        this.snapshotStore = Objects.requireNonNull(snapshotStore, "snapshotStore");
    }

    public StoredPositionEvent record(PositionEvent event) {
        return eventStore.append(event);
    }

    public void saveSnapshot(PositionSnapshot snapshot) {
        snapshotStore.save(snapshot);
    }

    public PositionState recover(PositionKey key) {
        PositionSnapshot snapshot = snapshotStore.latest(key).orElse(null);
        PositionState state = snapshot == null ? PositionState.empty(key) : snapshot.state();
        long version = snapshot == null ? 0L : snapshot.version();
        List<StoredPositionEvent> events = eventStore.load(key, version);
        for (StoredPositionEvent stored : events) {
            PositionEvent event = stored.event();
            PositionTrade trade = new PositionTrade(event.eventId(), event.key(), event.side(),
                    event.price(), event.quantity(), event.timestamp());
            state = PositionService.nextState(state, trade);
        }
        return state;
    }
}
