package com.cex.exchange.demo.middleware;

/**
 * IdempotencyStore 接口定义。
 */
public interface IdempotencyStore {
    boolean markIfAbsent(String messageId);
}
