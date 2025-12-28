package com.cex.exchange.demo.middleware.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PartitionedLog 核心类。
 */
public class PartitionedLog {
    private final int partitionCount;
    private final Partitioner partitioner;
    private final Map<Integer, List<PartitionRecord>> logs = new ConcurrentHashMap<>();

    public PartitionedLog(int partitionCount, Partitioner partitioner) {
        if (partitionCount <= 0) {
            throw new IllegalArgumentException("partitionCount must be > 0");
        }
        this.partitionCount = partitionCount;
        this.partitioner = Objects.requireNonNull(partitioner, "partitioner");
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
}
