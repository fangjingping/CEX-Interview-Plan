package com.cex.exchange.demo.concurrency;

/**
 * CommandHandler 接口定义。
 */
public interface CommandHandler {
    void handle(Command command);
}
