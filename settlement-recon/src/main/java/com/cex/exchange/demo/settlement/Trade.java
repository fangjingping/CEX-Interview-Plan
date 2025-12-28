package com.cex.exchange.demo.settlement;

import java.math.BigDecimal;

/**
 * Trade 记录类型。
 */
public record Trade(String tradeId, String symbol, BigDecimal price, BigDecimal quantity, BigDecimal fee) {
}
