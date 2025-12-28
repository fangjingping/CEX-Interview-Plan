package com.cex.exchange.demo.lowlatency;

/**
 * IdleStrategy 接口定义。
 */
public interface IdleStrategy {
    void idle();

    void reset();
}
