package com.cex.exchange.demo.resilience;

/**
 * ServiceInstance 记录类型。
 */
public record ServiceInstance(String serviceName, String instanceId, String host, int port) {
}
