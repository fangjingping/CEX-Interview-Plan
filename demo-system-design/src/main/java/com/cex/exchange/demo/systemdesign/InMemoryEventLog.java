package com.cex.exchange.demo.systemdesign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * InMemoryEventLog 核心类。
 */
public class InMemoryEventLog implements EventLog {
    private final AtomicLong nextSequence = new AtomicLong(1);
    private final List<StoredEvent> events = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public StoredEvent append(DomainEvent event) {
        long sequence = nextSequence.getAndIncrement();
        StoredEvent stored = new StoredEvent(sequence, event);
        lock.writeLock().lock();
        try {
            events.add(stored);
        } finally {
            lock.writeLock().unlock();
        }
        return stored;
    }

    @Override
    public List<StoredEvent> readFrom(long sequenceExclusive) {
        lock.readLock().lock();
        try {
            List<StoredEvent> result = new ArrayList<>();
            for (StoredEvent stored : events) {
                if (stored.sequence() > sequenceExclusive) {
                    result.add(stored);
                }
            }
            return Collections.unmodifiableList(result);
        } finally {
            lock.readLock().unlock();
        }
    }
}
