package com.cex.exchange.demo.risk;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskServiceTest {

    @Test
    void freezesConsumesAndReleasesMargin() {
        RiskService service = new RiskService();
        service.deposit("U1", new BigDecimal("100"));

        RiskHold hold = service.freeze("R1", "U1", new BigDecimal("50"));
        assertEquals(RiskHoldStatus.FROZEN, hold.status());
        assertBalance(service.getBalance("U1"), "50", "50", "0");

        service.consume("R1", "F1", new BigDecimal("20"));
        assertBalance(service.getBalance("U1"), "50", "30", "20");

        service.release("R1");
        assertBalance(service.getBalance("U1"), "80", "0", "20");
    }

    @Test
    void commitsWhenFullyConsumed() {
        RiskService service = new RiskService();
        service.deposit("U1", new BigDecimal("100"));

        service.freeze("R2", "U1", new BigDecimal("40"));
        RiskHold hold = service.consume("R2", "F1", new BigDecimal("10"));
        assertEquals(RiskHoldStatus.FROZEN, hold.status());

        hold = service.consume("R2", "F2", new BigDecimal("30"));
        assertEquals(RiskHoldStatus.COMMITTED, hold.status());
        assertBalance(service.getBalance("U1"), "60", "0", "40");
    }

    @Test
    void freezeIsIdempotent() {
        RiskService service = new RiskService();
        service.deposit("U1", new BigDecimal("100"));

        service.freeze("R3", "U1", new BigDecimal("25"));
        service.freeze("R3", "U1", new BigDecimal("25"));

        assertBalance(service.getBalance("U1"), "75", "25", "0");
    }

    @Test
    void rejectsInsufficientAvailable() {
        RiskService service = new RiskService();
        service.deposit("U1", new BigDecimal("10"));

        RiskException ex = assertThrows(RiskException.class,
                () -> service.freeze("R4", "U1", new BigDecimal("20")));
        assertEquals(RiskErrorCode.INSUFFICIENT_AVAILABLE, ex.getErrorCode());
    }

    private void assertBalance(MarginBalance balance, String available, String frozen, String used) {
        assertBigDecimal(available, balance.available());
        assertBigDecimal(frozen, balance.frozen());
        assertBigDecimal(used, balance.used());
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
