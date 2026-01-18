package com.cex.exchange.demo.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class RecoveryService<T> {
    private final EventLog eventLog;
    private final SnapshotStore snapshotStore;
    private final StateCodec<T> stateCodec;
    private final EventApplier<T> applier;
    private final Supplier<T> emptyStateSupplier;

    public RecoveryService(EventLog eventLog,
                           SnapshotStore snapshotStore,
                           StateCodec<T> stateCodec,
                           EventApplier<T> applier,
                           Supplier<T> emptyStateSupplier) {
        this.eventLog = Objects.requireNonNull(eventLog, "eventLog");
        this.snapshotStore = Objects.requireNonNull(snapshotStore, "snapshotStore");
        this.stateCodec = Objects.requireNonNull(stateCodec, "stateCodec");
        this.applier = Objects.requireNonNull(applier, "applier");
        this.emptyStateSupplier = Objects.requireNonNull(emptyStateSupplier, "emptyStateSupplier");
    }

    public RecoveryResult<T> recover() {
        Optional<Snapshot> snapshot = snapshotStore.load();
        T baseState;
        String lastEventId = "";
        long lastEventTimestamp = 0L;
        if (snapshot.isPresent()) {
            Snapshot loaded = snapshot.get();
            baseState = decodeState(loaded.payload());
            lastEventId = loaded.lastEventId();
            lastEventTimestamp = loaded.lastEventTimestamp();
        } else {
            baseState = emptyStateSupplier.get();
        }

        EventLogReadResult readResult = eventLog.readAll();
        List<Event> events = readResult.events();
        RecoveryAccumulator<T> accumulator;
        if (!lastEventId.isBlank()) {
            accumulator = applyAfterAnchor(baseState, events, lastEventId);
            if (!accumulator.anchorFound()) {
                accumulator = applyAfterTimestamp(baseState, events, lastEventTimestamp);
            }
        } else {
            accumulator = applyAfterTimestamp(baseState, events, lastEventTimestamp);
        }
        return new RecoveryResult<>(accumulator.state(), accumulator.applied(), accumulator.skipped(),
                readResult.skippedLines());
    }

    private T decodeState(String payload) {
        try {
            return stateCodec.decode(payload);
        } catch (RuntimeException ex) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_STATE, "failed to decode state", ex);
        }
    }

    private T applyEvent(T state, Event event) {
        try {
            return applier.apply(state, event);
        } catch (RuntimeException ex) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_EVENT, "failed to apply event", ex);
        }
    }

    private RecoveryAccumulator<T> applyAfterAnchor(T baseState, List<Event> events, String anchorEventId) {
        T state = baseState;
        Set<String> seen = new HashSet<>();
        int applied = 0;
        int skipped = 0;
        boolean anchorFound = false;
        for (Event event : events) {
            if (!anchorFound) {
                if (event.eventId().equals(anchorEventId)) {
                    anchorFound = true;
                }
                continue;
            }
            if (!seen.add(event.eventId())) {
                skipped++;
                continue;
            }
            state = applyEvent(state, event);
            applied++;
        }
        return new RecoveryAccumulator<>(state, applied, skipped, anchorFound);
    }

    private RecoveryAccumulator<T> applyAfterTimestamp(T baseState, List<Event> events, long lastEventTimestamp) {
        T state = baseState;
        Set<String> seen = new HashSet<>();
        int applied = 0;
        int skipped = 0;
        for (Event event : events) {
            if (event.timestamp() <= lastEventTimestamp) {
                continue;
            }
            if (!seen.add(event.eventId())) {
                skipped++;
                continue;
            }
            state = applyEvent(state, event);
            applied++;
        }
        return new RecoveryAccumulator<>(state, applied, skipped, true);
    }

    private record RecoveryAccumulator<T>(T state, int applied, int skipped, boolean anchorFound) {
    }
}
