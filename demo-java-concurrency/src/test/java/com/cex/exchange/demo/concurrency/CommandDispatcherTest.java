package com.cex.exchange.demo.concurrency;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CommandDispatcherTest 单元测试。
 */
class CommandDispatcherTest {

    @Test
    void rejectsWhenQueueAtCapacityBeforeStart() {
        CommandDispatcher dispatcher = new CommandDispatcher(
                1,
                new RejectingBackpressurePolicy(),
                command -> {
                });

        assertTrue(dispatcher.submit(new Command(1L, "A", 1L)));
        assertFalse(dispatcher.submit(new Command(2L, "B", 2L)));

        dispatcher.close();
    }

    @Test
    void processesInFifoOrder() throws InterruptedException {
        List<Long> processed = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);
        CommandDispatcher dispatcher = new CommandDispatcher(
                8,
                new RejectingBackpressurePolicy(),
                command -> {
                    processed.add(command.sequence());
                    latch.countDown();
                });
        dispatcher.start();

        dispatcher.submit(new Command(1L, "A", 1L));
        dispatcher.submit(new Command(2L, "B", 2L));
        dispatcher.submit(new Command(3L, "C", 3L));

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(List.of(1L, 2L, 3L), processed);

        dispatcher.close();
    }
}
