package com.cex.exchange.demo.ledger;

import java.math.BigDecimal;

/**
 * LedgerLine 记录类型。
 */
public record LedgerLine(String accountId, LedgerLineType type, BigDecimal amount) {
}
