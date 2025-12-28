package com.cex.exchange.demo.lowlatency;

/**
 * CommandHandler 接口定义。
 */
@FunctionalInterface
public interface CommandHandler {
    void handle(OrderCommand command);
}
