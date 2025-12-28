package com.cex.exchange.demo.marketdata;

import java.math.BigDecimal;

/**
 * PriceLevel 记录类型。
 */
public record PriceLevel(BigDecimal price, BigDecimal quantity) {
}
