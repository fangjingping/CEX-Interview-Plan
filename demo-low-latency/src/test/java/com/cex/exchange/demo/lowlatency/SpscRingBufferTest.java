package com.cex.exchange.demo.lowlatency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SpscRingBufferTest 单元测试。
 */
class SpscRingBufferTest {

    @Test
    void publishesAndPollsInOrder() {
        SpscRingBuffer buffer = new SpscRingBuffer(4);

        assertTrue(buffer.publish((command, sequence) ->
                command.reset(sequence, "BTC-USDT", 100, 1, 1_000L)));
        assertTrue(buffer.publish((command, sequence) ->
                command.reset(sequence, "BTC-USDT", 101, 2, 1_001L)));

        OrderCommand first = buffer.poll();
        OrderCommand second = buffer.poll();

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(0L, first.getSequence());
        assertEquals(1L, second.getSequence());
    }

    @Test
    void appliesBackpressureWhenFull() {
        SpscRingBuffer buffer = new SpscRingBuffer(2);

        assertTrue(buffer.publish((command, sequence) ->
                command.reset(sequence, "ETH-USDT", 200, 1, 1_000L)));
        assertTrue(buffer.publish((command, sequence) ->
                command.reset(sequence, "ETH-USDT", 201, 1, 1_001L)));
        assertFalse(buffer.publish((command, sequence) ->
                command.reset(sequence, "ETH-USDT", 202, 1, 1_002L)));

        assertNotNull(buffer.poll());
        assertTrue(buffer.publish((command, sequence) ->
                command.reset(sequence, "ETH-USDT", 202, 1, 1_002L)));
    }
}
