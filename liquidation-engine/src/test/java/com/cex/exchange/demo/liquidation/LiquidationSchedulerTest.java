package com.cex.exchange.demo.liquidation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquidationSchedulerTest {

    @Test
    void prioritizesHigherPriorityFirst() {
        LiquidationScheduler scheduler = new LiquidationScheduler(10);
        LiquidationTask low = new LiquidationTask("T1", "U1", "BTC-USDT",
                new BigDecimal("90"), new BigDecimal("1"), 1L);
        LiquidationTask high = new LiquidationTask("T2", "U2", "BTC-USDT",
                new BigDecimal("90"), new BigDecimal("5"), 2L);

        assertTrue(scheduler.enqueue(low));
        assertTrue(scheduler.enqueue(high));

        LiquidationTask first = scheduler.poll().orElseThrow();
        LiquidationTask second = scheduler.poll().orElseThrow();

        assertEquals("T2", first.taskId());
        assertEquals("T1", second.taskId());
        scheduler.complete(first);
        scheduler.complete(second);
    }

    @Test
    void dropsWhenQueueIsFull() {
        LiquidationScheduler scheduler = new LiquidationScheduler(1);
        LiquidationTask first = new LiquidationTask("T3", "U3", "BTC-USDT",
                new BigDecimal("80"), new BigDecimal("3"), 3L);
        LiquidationTask second = new LiquidationTask("T4", "U4", "BTC-USDT",
                new BigDecimal("80"), new BigDecimal("4"), 4L);

        assertTrue(scheduler.enqueue(first));
        assertFalse(scheduler.enqueue(second));
        assertEquals(1, scheduler.droppedCount());
        scheduler.complete(first);
    }

    @Test
    void rejectsDuplicateUserSymbolTasks() {
        LiquidationScheduler scheduler = new LiquidationScheduler(10);
        LiquidationTask first = new LiquidationTask("T5", "U5", "ETH-USDT",
                new BigDecimal("120"), new BigDecimal("2"), 5L);
        LiquidationTask duplicate = new LiquidationTask("T6", "U5", "ETH-USDT",
                new BigDecimal("115"), new BigDecimal("6"), 6L);

        assertTrue(scheduler.enqueue(first));
        assertFalse(scheduler.enqueue(duplicate));
        LiquidationTask polled = scheduler.poll().orElseThrow();
        scheduler.complete(polled);
    }
}
