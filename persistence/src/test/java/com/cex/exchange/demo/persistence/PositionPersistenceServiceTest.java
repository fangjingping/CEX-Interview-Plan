package com.cex.exchange.demo.persistence;

import com.cex.exchange.demo.position.PositionKey;
import com.cex.exchange.demo.position.PositionState;
import com.cex.exchange.model.OrderSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionPersistenceServiceTest {

    @Test
    void recordsEventsIdempotently() {
        PositionEventStore eventStore = new PositionEventStore();
        PositionSnapshotStore snapshotStore = new PositionSnapshotStore();
        PositionPersistenceService service = new PositionPersistenceService(eventStore, snapshotStore);
        PositionKey key = new PositionKey("U1", "BTC-USDT");
        PositionEvent event = new PositionEvent("E1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), 1L);

        StoredPositionEvent first = service.record(event);
        StoredPositionEvent second = service.record(event);

        assertEquals(first, second);
        assertEquals(1, eventStore.load(key, 0L).size());
    }

    @Test
    void recoversFromSnapshotAndEvents() {
        PositionEventStore eventStore = new PositionEventStore();
        PositionSnapshotStore snapshotStore = new PositionSnapshotStore();
        PositionPersistenceService service = new PositionPersistenceService(eventStore, snapshotStore);
        PositionKey key = new PositionKey("U1", "BTC-USDT");

        service.record(new PositionEvent("E1", key, OrderSide.BUY,
                new BigDecimal("100"), new BigDecimal("1"), 1L));
        PositionState snapshotState = service.recover(key);
        snapshotStore.save(new PositionSnapshot(key, snapshotState, 1L, 2L));

        service.record(new PositionEvent("E2", key, OrderSide.BUY,
                new BigDecimal("110"), new BigDecimal("1"), 3L));
        PositionState recovered = service.recover(key);

        assertBigDecimal("2", recovered.quantity());
        assertBigDecimal("105", recovered.entryPrice());
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
