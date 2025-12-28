package com.cex.exchange.demo.ledger;

import java.time.Instant;
import java.util.List;

/**
 * LedgerEntry 记录类型。
 */
public record LedgerEntry(String entryId, List<LedgerLine> lines, Instant timestamp) {
}
