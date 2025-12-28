package com.cex.exchange.demo.middleware.kafka;

/**
 * Partitioner 接口定义。
 */
public interface Partitioner {
    int partition(String key, int partitionCount);
}
