package com.cex.exchange.demo.middleware.kafka;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PartitionedLogTest 单元测试。
 */
class PartitionedLogTest {

    @Test
    void keepsOrderWithinPartition() {
        PartitionedLog log = new PartitionedLog(2, new HashPartitioner());
        PartitionRecord first = log.append("BTC-USDT", "A");
        PartitionRecord second = log.append("BTC-USDT", "B");

        assertEquals(first.partition(), second.partition());
        assertEquals(0, first.offset());
        assertEquals(1, second.offset());

        List<PartitionRecord> records = log.read(first.partition(), 0, 10);
        assertEquals(List.of("A", "B"), records.stream().map(PartitionRecord::payload).toList());
        assertEquals(2, log.totalRecords());
    }

    @Test
    void handlesDifferentKeysAcrossPartitions() {
        PartitionedLog log = new PartitionedLog(2, new HashPartitioner());
        PartitionRecord btc = log.append("BTC-USDT", "BTC");
        PartitionRecord eth = log.append("ETH-USDT", "ETH");

        assertTrue(btc.partition() >= 0);
        assertTrue(eth.partition() >= 0);
    }
}
