package com.cex.exchange.demo.liquidation;

import java.util.Comparator;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class LiquidationScheduler {
    private static final int DEFAULT_MAX_QUEUE_SIZE = 1024;
    private static final Comparator<LiquidationTask> TASK_ORDER = Comparator
            .comparing(LiquidationTask::priority)
            .reversed()
            .thenComparingLong(LiquidationTask::timestamp)
            .thenComparing(LiquidationTask::taskId);

    private final Queue<LiquidationTask> queue;
    private final Set<String> scheduledKeys = ConcurrentHashMap.newKeySet();
    private final Semaphore capacity;
    private final AtomicInteger droppedCount = new AtomicInteger();
    private final int maxQueueSize;

    public LiquidationScheduler() {
        this(DEFAULT_MAX_QUEUE_SIZE);
    }

    public LiquidationScheduler(int maxQueueSize) {
        if (maxQueueSize <= 0) {
            throw new LiquidationException(LiquidationErrorCode.INVALID_TASK, "maxQueueSize must be > 0");
        }
        this.maxQueueSize = maxQueueSize;
        this.capacity = new Semaphore(maxQueueSize);
        this.queue = new PriorityBlockingQueue<>(maxQueueSize, TASK_ORDER);
    }

    public boolean enqueue(LiquidationTask task) {
        if (task == null) {
            throw new LiquidationException(LiquidationErrorCode.INVALID_TASK, "task must not be null");
        }
        String key = key(task.userId(), task.symbol());
        if (!scheduledKeys.add(key)) {
            return false;
        }
        if (!capacity.tryAcquire()) {
            scheduledKeys.remove(key);
            droppedCount.incrementAndGet();
            return false;
        }
        try {
            queue.add(task);
        } catch (RuntimeException ex) {
            capacity.release();
            scheduledKeys.remove(key);
            throw ex;
        }
        return true;
    }

    public Optional<LiquidationTask> poll() {
        return Optional.ofNullable(queue.poll());
    }

    public void complete(LiquidationTask task) {
        if (task == null) {
            return;
        }
        if (scheduledKeys.remove(key(task.userId(), task.symbol()))) {
            capacity.release();
        }
    }

    public int droppedCount() {
        return droppedCount.get();
    }

    public int maxQueueSize() {
        return maxQueueSize;
    }

    private String key(String userId, String symbol) {
        return userId + "|" + symbol;
    }
}
