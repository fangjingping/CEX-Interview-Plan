package com.cex.exchange.demo.settlement;

import java.math.BigDecimal;

/**
 * FeeEntry 记录类型。
 */
public record FeeEntry(String tradeId, BigDecimal amount) {
}
