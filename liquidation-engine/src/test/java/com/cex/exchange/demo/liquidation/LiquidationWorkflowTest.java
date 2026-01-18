package com.cex.exchange.demo.liquidation;

import com.cex.exchange.demo.position.Position;
import com.cex.exchange.demo.position.PositionFill;
import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionMode;
import com.cex.exchange.demo.position.PositionService;
import com.cex.exchange.demo.position.PositionSide;
import com.cex.exchange.model.OrderSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquidationWorkflowTest {

    @Test
    void enqueuesAndProcessesLiquidation() {
        PositionService positionService = new PositionService();
        positionService.applyFill(new PositionFill("F1", "U1", "BTC-USDT", OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), PositionMode.CROSS, 1L));

        LiquidationScheduler scheduler = new LiquidationScheduler();
        LiquidationService service = new LiquidationService(positionService);
        LiquidationWorker worker = new LiquidationWorker(scheduler, service);

        LiquidationTask task = new LiquidationTask("T1", "U1", "BTC-USDT",
                new BigDecimal("80"), new BigDecimal("10"), 2L);
        assertTrue(scheduler.enqueue(task));

        Optional<LiquidationResult> result = worker.processNext();

        assertTrue(result.isPresent());
        assertEquals(LiquidationStatus.EXECUTED, result.get().status());
        assertEquals(1, result.get().trades().size());

        Position position = positionService.getPositionDetail(new PositionKey("U1", "BTC-USDT")).orElseThrow();
        assertEquals(PositionSide.FLAT, position.side());
        assertBigDecimal("0", position.quantity());
    }

    @Test
    void repeatedTasksDoNotDoubleLiquidate() {
        PositionService positionService = new PositionService();
        positionService.applyFill(new PositionFill("F2", "U2", "ETH-USDT", OrderSide.BUY,
                new BigDecimal("200"), new BigDecimal("1"), PositionMode.CROSS, 1L));

        LiquidationScheduler scheduler = new LiquidationScheduler();
        LiquidationService service = new LiquidationService(positionService);
        LiquidationWorker worker = new LiquidationWorker(scheduler, service);

        LiquidationTask first = new LiquidationTask("T2", "U2", "ETH-USDT",
                new BigDecimal("150"), new BigDecimal("9"), 2L);
        LiquidationTask duplicate = new LiquidationTask("T3", "U2", "ETH-USDT",
                new BigDecimal("150"), new BigDecimal("8"), 3L);

        assertTrue(scheduler.enqueue(first));
        assertFalse(scheduler.enqueue(duplicate));

        LiquidationResult firstResult = worker.processNext().orElseThrow();
        assertEquals(LiquidationStatus.EXECUTED, firstResult.status());
        assertTrue(worker.processNext().isEmpty());

        LiquidationTask retry = new LiquidationTask("T4", "U2", "ETH-USDT",
                new BigDecimal("140"), new BigDecimal("7"), 4L);
        assertTrue(scheduler.enqueue(retry));

        LiquidationResult secondResult = worker.processNext().orElseThrow();
        assertEquals(LiquidationStatus.NO_POSITION, secondResult.status());
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
