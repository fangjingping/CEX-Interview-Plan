package com.cex.exchange.demo.lowlatency;

import java.util.Objects;

/**
 * SpscRingBuffer 核心类。
 */
public class SpscRingBuffer {
    private final OrderCommand[] buffer;
    private final int capacity;
    private final int mask;
    private volatile long head;
    private volatile long tail;

    public SpscRingBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        if ((capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException("capacity must be power of two");
        }
        this.capacity = capacity;
        this.mask = capacity - 1;
        this.buffer = new OrderCommand[capacity];
        for (int i = 0; i < capacity; i++) {
            buffer[i] = new OrderCommand();
        }
    }

    public boolean publish(CommandWriter writer) {
        Objects.requireNonNull(writer, "writer");
        long currentTail = tail;
        if (currentTail - head >= capacity) {
            return false;
        }
        int index = (int) (currentTail & mask);
        OrderCommand entry = buffer[index];
        writer.write(entry, currentTail);
        tail = currentTail + 1;
        return true;
    }

    public OrderCommand poll() {
        long currentHead = head;
        if (currentHead >= tail) {
            return null;
        }
        int index = (int) (currentHead & mask);
        OrderCommand entry = buffer[index];
        head = currentHead + 1;
        return entry;
    }

    public int size() {
        return (int) (tail - head);
    }

    public boolean isEmpty() {
        return tail == head;
    }

    public int capacity() {
        return capacity;
    }
}
