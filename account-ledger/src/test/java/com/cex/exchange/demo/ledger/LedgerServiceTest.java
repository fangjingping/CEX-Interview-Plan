package com.cex.exchange.demo.ledger;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * LedgerServiceTest 单元测试。
 */
class LedgerServiceTest {

    @Test
    void acceptsBalancedEntry() {
        LedgerService service = new LedgerService();
        service.post("E1", List.of(
                new LedgerLine("A1", LedgerLineType.DEBIT, new BigDecimal("10")),
                new LedgerLine("A2", LedgerLineType.CREDIT, new BigDecimal("10"))
        ));

        assertEquals(1, service.getEntries().size());
    }

    @Test
    void rejectsUnbalancedEntry() {
        LedgerService service = new LedgerService();
        assertThrows(IllegalArgumentException.class, () -> service.post("E2", List.of(
                new LedgerLine("A1", LedgerLineType.DEBIT, new BigDecimal("10")),
                new LedgerLine("A2", LedgerLineType.CREDIT, new BigDecimal("9"))
        )));
    }

    @Test
    void ignoresDuplicateEntryId() {
        LedgerService service = new LedgerService();
        service.post("E3", List.of(
                new LedgerLine("A1", LedgerLineType.DEBIT, new BigDecimal("5")),
                new LedgerLine("A2", LedgerLineType.CREDIT, new BigDecimal("5"))
        ));
        service.post("E3", List.of(
                new LedgerLine("A1", LedgerLineType.DEBIT, new BigDecimal("5")),
                new LedgerLine("A2", LedgerLineType.CREDIT, new BigDecimal("5"))
        ));

        assertEquals(1, service.getEntries().size());
    }
}
