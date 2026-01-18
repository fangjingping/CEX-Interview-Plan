package com.cex.exchange.demo.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileRecoveryServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void recoversFromSnapshotAndLog() {
        Path logPath = tempDir.resolve("events.log");
        Path snapshotPath = tempDir.resolve("snapshot.dat");
        FileEventLog eventLog = new FileEventLog(logPath);
        FileSnapshotStore snapshotStore = new FileSnapshotStore(snapshotPath);

        StateCodec<CounterState> codec = new StateCodec<>() {
            @Override
            public String encode(CounterState state) {
                return Long.toString(state.value());
            }

            @Override
            public CounterState decode(String payload) {
                return new CounterState(Long.parseLong(payload));
            }
        };
        EventApplier<CounterState> applier = (state, event) ->
                state.add(Long.parseLong(event.payload()));
        RecoveryService<CounterState> recovery = new RecoveryService<>(eventLog, snapshotStore, codec, applier,
                () -> new CounterState(0L));

        eventLog.append(new Event("E1", "DELTA", 1L, "5"));
        eventLog.append(new Event("E2", "DELTA", 2L, "-2"));

        CounterState expected = new CounterState(3L);
        snapshotStore.save(new Snapshot("S1", "E2", 2L, 3L, codec.encode(expected)));

        RecoveryResult<CounterState> result = recovery.recover();

        assertEquals(3L, result.state().value());
        assertEquals(0, result.appliedEvents());
        assertEquals(0, result.skippedLines());
    }

    @Test
    void skipsCorruptedTailLine() throws IOException {
        Path logPath = tempDir.resolve("events.log");
        Path snapshotPath = tempDir.resolve("snapshot.dat");
        FileEventLog eventLog = new FileEventLog(logPath);
        FileSnapshotStore snapshotStore = new FileSnapshotStore(snapshotPath);

        StateCodec<CounterState> codec = new StateCodec<>() {
            @Override
            public String encode(CounterState state) {
                return Long.toString(state.value());
            }

            @Override
            public CounterState decode(String payload) {
                return new CounterState(Long.parseLong(payload));
            }
        };
        EventApplier<CounterState> applier = (state, event) ->
                state.add(Long.parseLong(event.payload()));
        RecoveryService<CounterState> recovery = new RecoveryService<>(eventLog, snapshotStore, codec, applier,
                () -> new CounterState(0L));

        eventLog.append(new Event("E3", "DELTA", 4L, "7"));
        Files.writeString(logPath, "corrupted-line" + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);

        RecoveryResult<CounterState> result = recovery.recover();

        assertEquals(1, result.appliedEvents());
        assertEquals(1, result.skippedLines());
        assertEquals(7L, result.state().value());
    }

    @Test
    void deduplicatesEventIds() {
        Path logPath = tempDir.resolve("events.log");
        Path snapshotPath = tempDir.resolve("snapshot.dat");
        FileEventLog eventLog = new FileEventLog(logPath);
        FileSnapshotStore snapshotStore = new FileSnapshotStore(snapshotPath);

        StateCodec<CounterState> codec = new StateCodec<>() {
            @Override
            public String encode(CounterState state) {
                return Long.toString(state.value());
            }

            @Override
            public CounterState decode(String payload) {
                return new CounterState(Long.parseLong(payload));
            }
        };
        EventApplier<CounterState> applier = (state, event) ->
                state.add(Long.parseLong(event.payload()));
        RecoveryService<CounterState> recovery = new RecoveryService<>(eventLog, snapshotStore, codec, applier,
                () -> new CounterState(0L));

        eventLog.append(new Event("E4", "DELTA", 5L, "4"));
        eventLog.append(new Event("E4", "DELTA", 6L, "9"));

        RecoveryResult<CounterState> result = recovery.recover();

        assertEquals(1, result.appliedEvents());
        assertEquals(1, result.skippedEvents());
        assertEquals(4L, result.state().value());
    }

    private record CounterState(long value) {
        CounterState add(long delta) {
            return new CounterState(value + delta);
        }
    }
}
