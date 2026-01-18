package com.cex.exchange.demo.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FileSnapshotStore implements SnapshotStore {
    private static final String DELIMITER = "|";

    private final Path snapshotPath;

    public FileSnapshotStore(Path snapshotPath) {
        this.snapshotPath = Objects.requireNonNull(snapshotPath, "snapshotPath");
        ensurePath();
    }

    @Override
    public void save(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        String payload = Base64.getEncoder().encodeToString(snapshot.payload().getBytes(StandardCharsets.UTF_8));
        String line = String.join(DELIMITER,
                snapshot.snapshotId(),
                snapshot.lastEventId(),
                Long.toString(snapshot.lastEventTimestamp()),
                Long.toString(snapshot.timestamp()),
                payload);
        try {
            Files.writeString(snapshotPath, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            throw new PersistenceException(PersistenceErrorCode.IO_FAILURE, "failed to write snapshot", ex);
        }
    }

    @Override
    public Optional<Snapshot> load() {
        if (!Files.exists(snapshotPath)) {
            return Optional.empty();
        }
        try {
            List<String> lines = Files.readAllLines(snapshotPath, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return Optional.empty();
            }
            String line = lines.get(0);
            if (line == null || line.isBlank()) {
                return Optional.empty();
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length != 5) {
                throw new PersistenceException(PersistenceErrorCode.INVALID_SNAPSHOT, "invalid snapshot format");
            }
            String snapshotId = parts[0];
            String lastEventId = parts[1];
            long lastEventTimestamp = Long.parseLong(parts[2]);
            long timestamp = Long.parseLong(parts[3]);
            String payload = new String(Base64.getDecoder().decode(parts[4]), StandardCharsets.UTF_8);
            return Optional.of(new Snapshot(snapshotId, lastEventId, lastEventTimestamp, timestamp, payload));
        } catch (IOException ex) {
            throw new PersistenceException(PersistenceErrorCode.IO_FAILURE, "failed to read snapshot", ex);
        }
    }

    private void ensurePath() {
        Path parent = snapshotPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ex) {
                throw new PersistenceException(PersistenceErrorCode.INVALID_PATH,
                        "failed to create snapshot directory", ex);
            }
        }
    }
}
