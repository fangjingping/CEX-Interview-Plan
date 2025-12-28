package com.cex.exchange.demo.lowlatency;

/**
 * CommandWriter 接口定义。
 */
@FunctionalInterface
public interface CommandWriter {
    void write(OrderCommand command, long sequence);
}
