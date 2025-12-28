package com.cex.exchange.demo.lowlatency;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LowLatencyProcessorTest 单元测试。
 */
class LowLatencyProcessorTest {

    @Test
    void processesCommandsInOrder() throws InterruptedException {
        List<Long> sequences = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);
        LowLatencyProcessor processor = new LowLatencyProcessor(8, command -> {
            sequences.add(command.getSequence());
            latch.countDown();
        });

        processor.start();
        for (int i = 0; i < 3; i++) {
            boolean published = processor.publish((command, sequence) ->
                    command.reset(sequence, "SOL-USDT", 300 + sequence, 1, 1_000L + sequence));
            assertTrue(published);
        }

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        processor.close();

        assertEquals(List.of(0L, 1L, 2L), sequences);
    }
}
