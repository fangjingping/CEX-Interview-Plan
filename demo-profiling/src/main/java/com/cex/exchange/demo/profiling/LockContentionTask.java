package com.cex.exchange.demo.profiling;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LockContentionTask 核心类。
 */
public class LockContentionTask {
    public long run(int threads, int iterations) {
        if (threads <= 0 || iterations <= 0) {
            throw new IllegalArgumentException("threads and iterations must be > 0");
        }
        Object lock = new Object();
        AtomicLong counter = new AtomicLong();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    synchronized (lock) {
                        counter.incrementAndGet();
                    }
                }
                latch.countDown();
            });
        }
        await(latch);
        executor.shutdownNow();
        return counter.get();
    }

    private void await(CountDownLatch latch) {
        Objects.requireNonNull(latch, "latch");
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
