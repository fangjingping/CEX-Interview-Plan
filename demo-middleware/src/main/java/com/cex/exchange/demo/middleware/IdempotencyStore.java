package com.cex.exchange.demo.middleware;

/**
 * IdempotencyStore 接口定义。
 */
public interface IdempotencyStore {
    boolean tryStart(String messageId);

    void markSuccess(String messageId);

    void release(String messageId);
}
