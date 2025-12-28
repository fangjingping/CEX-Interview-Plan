package com.cex.exchange.demo.middleware.outbox;

import com.cex.exchange.demo.middleware.kafka.HashPartitioner;
import com.cex.exchange.demo.middleware.kafka.PartitionedLog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OutboxPublisherTest 单元测试。
 */
class OutboxPublisherTest {

    @Test
    void publishesEachEventOnce() {
        OutboxStore store = new OutboxStore();
        PartitionedLog log = new PartitionedLog(2, new HashPartitioner());
        OutboxPublisher publisher = new OutboxPublisher(store, log);

        store.add(new OutboxEvent("E1", "BTC-USDT", "payload-1"));
        store.add(new OutboxEvent("E1", "BTC-USDT", "payload-1"));

        assertEquals(1, publisher.publish(10));
        assertEquals(0, publisher.publish(10));
        assertEquals(1, log.totalRecords());
    }

    @Test
    void replaysPendingButKeepsIdempotentLog() {
        OutboxStore store = new OutboxStore();
        PartitionedLog log = new PartitionedLog(2, new HashPartitioner());
        OutboxPublisher publisher = new OutboxPublisher(store, log);

        store.add(new OutboxEvent("E1", "BTC-USDT", "payload-1"));
        store.add(new OutboxEvent("E2", "BTC-USDT", "payload-2"));

        assertEquals(1, publisher.publish(1));
        assertEquals(1, publisher.publish(10));
        assertEquals(0, publisher.publish(10));
        assertEquals(2, log.totalRecords());
    }
}
