package com.cex.exchange.demo.gateway;

/**
 * SignedRequest 记录类型。
 */
public record SignedRequest(String apiKeyId, long timestamp, String nonce, String payload, String signature) {
}
