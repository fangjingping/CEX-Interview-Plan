package com.cex.exchange.demo.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class FileEventLog implements EventLog {
    private static final String DELIMITER = "|";

    private final Path logPath;

    public FileEventLog(Path logPath) {
        this.logPath = Objects.requireNonNull(logPath, "logPath");
        ensurePath();
    }

    @Override
    public void append(Event event) {
        Objects.requireNonNull(event, "event");
        String encoded = encode(event);
        try {
            Files.writeString(logPath, encoded + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new PersistenceException(PersistenceErrorCode.IO_FAILURE, "failed to append event", ex);
        }
    }

    @Override
    public EventLogReadResult readAll() {
        if (!Files.exists(logPath)) {
            return new EventLogReadResult(List.of(), 0);
        }
        List<Event> events = new ArrayList<>();
        int skipped = 0;
        try {
            List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    skipped++;
                    continue;
                }
                try {
                    events.add(decode(line));
                } catch (RuntimeException ex) {
                    skipped++;
                }
            }
        } catch (IOException ex) {
            throw new PersistenceException(PersistenceErrorCode.IO_FAILURE, "failed to read event log", ex);
        }
        return new EventLogReadResult(events, skipped);
    }

    private void ensurePath() {
        Path parent = logPath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ex) {
                throw new PersistenceException(PersistenceErrorCode.INVALID_PATH,
                        "failed to create log directory", ex);
            }
        }
    }

    private String encode(Event event) {
        String payload = Base64.getEncoder().encodeToString(event.payload().getBytes(StandardCharsets.UTF_8));
        return String.join(DELIMITER, event.eventId(), event.type(), Long.toString(event.timestamp()), payload);
    }

    private Event decode(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4) {
            throw new PersistenceException(PersistenceErrorCode.INVALID_LOG_LINE, "invalid log line");
        }
        long timestamp = Long.parseLong(parts[2]);
        byte[] payloadBytes = Base64.getDecoder().decode(parts[3]);
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        return new Event(parts[0], parts[1], timestamp, payload);
    }
}
