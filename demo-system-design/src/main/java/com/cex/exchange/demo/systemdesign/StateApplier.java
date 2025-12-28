package com.cex.exchange.demo.systemdesign;

/**
 * StateApplier 接口定义。
 */
public interface StateApplier {
    String apply(String currentState, DomainEvent event);
}
