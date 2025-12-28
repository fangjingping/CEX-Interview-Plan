package com.cex.exchange.demo.lowlatency;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LowLatencyProcessor 核心类。
 */
public class LowLatencyProcessor implements AutoCloseable {
    private final SpscRingBuffer buffer;
    private final CommandHandler handler;
    private final IdleStrategy idleStrategy;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;

    public LowLatencyProcessor(int capacity, CommandHandler handler) {
        this(capacity, handler, new SpinWaitIdleStrategy());
    }

    public LowLatencyProcessor(int capacity, CommandHandler handler, IdleStrategy idleStrategy) {
        this.buffer = new SpscRingBuffer(capacity);
        this.handler = Objects.requireNonNull(handler, "handler");
        this.idleStrategy = Objects.requireNonNull(idleStrategy, "idleStrategy");
    }

    public boolean publish(CommandWriter writer) {
        return buffer.publish(writer);
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            worker = new Thread(this::runLoop, "low-latency-processor");
            worker.setDaemon(true);
            worker.start();
        }
    }

    private void runLoop() {
        while (running.get() || !buffer.isEmpty()) {
            OrderCommand command = buffer.poll();
            if (command != null) {
                handler.handle(command);
                idleStrategy.reset();
            } else {
                idleStrategy.idle();
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        if (worker != null) {
            try {
                worker.join(500L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
