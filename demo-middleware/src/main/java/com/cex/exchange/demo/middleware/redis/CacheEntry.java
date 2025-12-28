package com.cex.exchange.demo.middleware.redis;

/**
 * CacheEntry 记录类型。
 */
public record CacheEntry(String value, long expiresAtMillis) {
}
