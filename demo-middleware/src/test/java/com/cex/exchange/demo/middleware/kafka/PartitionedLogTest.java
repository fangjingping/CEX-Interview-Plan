package com.cex.exchange.demo.middleware.kafka;

import com.cex.exchange.demo.middleware.TimeSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void allowsReuseAfterRecordIdExpires() {
        ManualTimeSource timeSource = new ManualTimeSource(1_000L);
        PartitionedLog log = new PartitionedLog(1, new HashPartitioner(), 500L, timeSource);

        assertTrue(log.appendIfAbsent("R1", "BTC-USDT", "A"));
        assertTrue(log.appendIfAbsent("R2", "BTC-USDT", "B"));
        assertEquals(2, log.totalRecords());
        assertFalse(log.appendIfAbsent("R1", "BTC-USDT", "A"));

        timeSource.advance(600L);
        assertTrue(log.appendIfAbsent("R1", "BTC-USDT", "C"));
    }

    private static final class ManualTimeSource implements TimeSource {
        private long nowMillis;

        private ManualTimeSource(long nowMillis) {
            this.nowMillis = nowMillis;
        }

        @Override
        public long nowMillis() {
            return nowMillis;
        }

        private void advance(long deltaMillis) {
            nowMillis += deltaMillis;
        }
    }
}
