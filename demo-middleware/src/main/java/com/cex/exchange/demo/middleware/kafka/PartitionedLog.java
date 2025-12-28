package com.cex.exchange.demo.middleware.kafka;

import com.cex.exchange.demo.middleware.SystemTimeSource;
import com.cex.exchange.demo.middleware.TimeSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PartitionedLog 核心类。
 */
public class PartitionedLog {
    private static final long DEFAULT_RECORD_ID_TTL_MILLIS = 86_400_000L;

    private final int partitionCount;
    private final Partitioner partitioner;
    private final Map<Integer, List<PartitionRecord>> logs = new ConcurrentHashMap<>();
    private final Map<String, Long> recordIdExpiresAt = new ConcurrentHashMap<>();
    private final long recordIdTtlMillis;
    private final TimeSource timeSource;

    public PartitionedLog(int partitionCount, Partitioner partitioner) {
        this(partitionCount, partitioner, DEFAULT_RECORD_ID_TTL_MILLIS, new SystemTimeSource());
    }

    public PartitionedLog(int partitionCount, Partitioner partitioner, long recordIdTtlMillis, TimeSource timeSource) {
        if (partitionCount <= 0) {
            throw new IllegalArgumentException("partitionCount must be > 0");
        }
        if (recordIdTtlMillis <= 0) {
            throw new IllegalArgumentException("recordIdTtlMillis must be > 0");
        }
        this.partitionCount = partitionCount;
        this.partitioner = Objects.requireNonNull(partitioner, "partitioner");
        this.recordIdTtlMillis = recordIdTtlMillis;
        this.timeSource = Objects.requireNonNull(timeSource, "timeSource");
    }

    public PartitionRecord append(String key, String payload) {
        int partition = partitioner.partition(key, partitionCount);
        List<PartitionRecord> log = logs.computeIfAbsent(partition, value -> new ArrayList<>());
        synchronized (log) {
            int offset = log.size();
            PartitionRecord record = new PartitionRecord(partition, offset, key, payload);
            log.add(record);
            return record;
        }
    }

    public boolean appendIfAbsent(String recordId, String key, String payload) {
        Objects.requireNonNull(recordId, "recordId");
        long now = timeSource.nowMillis();
        AtomicBoolean accepted = new AtomicBoolean(false);
        recordIdExpiresAt.compute(recordId, (id, existing) -> {
            if (existing == null || existing <= now) {
                accepted.set(true);
                return now + recordIdTtlMillis;
            }
            return existing;
        });
        if (!accepted.get()) {
            return false;
        }
        append(key, payload);
        cleanupExpiredRecordIds(now);
        return true;
    }

    public List<PartitionRecord> read(int partition, int fromOffset, int limit) {
        if (fromOffset < 0 || limit <= 0) {
            throw new IllegalArgumentException("invalid offset or limit");
        }
        List<PartitionRecord> log = logs.get(partition);
        if (log == null) {
            return List.of();
        }
        synchronized (log) {
            if (fromOffset >= log.size()) {
                return List.of();
            }
            int toIndex = Math.min(log.size(), fromOffset + limit);
            return Collections.unmodifiableList(new ArrayList<>(log.subList(fromOffset, toIndex)));
        }
    }

    public int size(int partition) {
        List<PartitionRecord> log = logs.get(partition);
        if (log == null) {
            return 0;
        }
        synchronized (log) {
            return log.size();
        }
    }

    public int totalRecords() {
        int total = 0;
        for (List<PartitionRecord> log : logs.values()) {
            synchronized (log) {
                total += log.size();
            }
        }
        return total;
    }

    public int partitionCount() {
        return partitionCount;
    }

    public void cleanupExpiredRecordIds() {
        cleanupExpiredRecordIds(timeSource.nowMillis());
    }

    private void cleanupExpiredRecordIds(long nowMillis) {
        for (Map.Entry<String, Long> entry : recordIdExpiresAt.entrySet()) {
            if (entry.getValue() <= nowMillis) {
                recordIdExpiresAt.remove(entry.getKey(), entry.getValue());
            }
        }
    }
}
