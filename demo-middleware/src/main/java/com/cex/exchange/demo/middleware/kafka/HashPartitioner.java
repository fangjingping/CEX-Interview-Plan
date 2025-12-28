package com.cex.exchange.demo.middleware.kafka;

import java.util.Objects;

/**
 * HashPartitioner 核心类。
 */
public class HashPartitioner implements Partitioner {
    @Override
    public int partition(String key, int partitionCount) {
        Objects.requireNonNull(key, "key");
        if (partitionCount <= 0) {
            throw new IllegalArgumentException("partitionCount must be > 0");
        }
        return Math.floorMod(key.hashCode(), partitionCount);
    }
}
