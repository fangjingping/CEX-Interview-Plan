package com.cex.exchange.demo.concurrency;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CommandDispatcher 核心类。
 */
public class CommandDispatcher implements AutoCloseable {
    private final int capacity;
    private final BlockingQueue<Command> queue;
    private final BackpressurePolicy policy;
    private final CommandHandler handler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService worker;

    public CommandDispatcher(int capacity, BackpressurePolicy policy, CommandHandler handler) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.capacity = capacity;
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.policy = Objects.requireNonNull(policy, "policy");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    public boolean submit(Command command) {
        if (!policy.allow(queue.size(), capacity)) {
            return false;
        }
        return queue.offer(command);
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            worker = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "command-dispatcher");
                thread.setDaemon(true);
                return thread;
            });
            worker.submit(this::runLoop);
        }
    }

    private void runLoop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                Command command = queue.poll(50, TimeUnit.MILLISECONDS);
                if (command != null) {
                    handler.handle(command);
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        if (worker != null) {
            worker.shutdownNow();
        }
    }
}
