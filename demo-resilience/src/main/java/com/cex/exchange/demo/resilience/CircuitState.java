package com.cex.exchange.demo.resilience;

/**
 * CircuitState 枚举定义。
 */
public enum CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
