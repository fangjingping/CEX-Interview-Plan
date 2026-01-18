package com.cex.exchange.demo.persistence;

public interface StateCodec<T> {
    String encode(T state);

    T decode(String payload);
}
