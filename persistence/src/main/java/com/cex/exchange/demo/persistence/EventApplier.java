package com.cex.exchange.demo.persistence;

public interface EventApplier<T> {
    T apply(T state, Event event);
}
