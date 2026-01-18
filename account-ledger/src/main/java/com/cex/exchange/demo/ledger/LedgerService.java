package com.cex.exchange.demo.ledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * LedgerService 核心类。
 */
public class LedgerService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final Set<String> processedEntries = ConcurrentHashMap.newKeySet();
    private final List<LedgerEntry> entries = new CopyOnWriteArrayList<>();

    public void post(String entryId, List<LedgerLine> lines) {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(lines, "lines");
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines must not be empty");
        }
        BigDecimal debitTotal = ZERO;
        BigDecimal creditTotal = ZERO;
        for (LedgerLine line : lines) {
            if (line.amount() == null || line.amount().signum() <= 0) {
                throw new IllegalArgumentException("amount must be positive");
            }
            if (line.type() == LedgerLineType.DEBIT) {
                debitTotal = debitTotal.add(line.amount());
            } else {
                creditTotal = creditTotal.add(line.amount());
            }
        }
        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new IllegalArgumentException("debit and credit not balanced");
        }
        if (!processedEntries.add(entryId)) {
            return;
        }
        try {
            entries.add(new LedgerEntry(entryId, List.copyOf(lines), Instant.now()));
        } catch (RuntimeException ex) {
            processedEntries.remove(entryId);
            throw ex;
        }
    }

    public List<LedgerEntry> getEntries() {
        return List.copyOf(entries);
    }
}
