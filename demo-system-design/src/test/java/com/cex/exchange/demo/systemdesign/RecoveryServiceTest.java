package com.cex.exchange.demo.systemdesign;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RecoveryServiceTest 单元测试。
 */
class RecoveryServiceTest {

    @Test
    void recoversFromSnapshotAndReplaysInOrder() {
        EventLog eventLog = new InMemoryEventLog();
        SnapshotStore snapshotStore = new InMemorySnapshotStore();

        eventLog.append(new DomainEvent("ORDER", "A", 1L));
        eventLog.append(new DomainEvent("ORDER", "B", 2L));
        snapshotStore.save(new Snapshot(2L, "S2"));
        eventLog.append(new DomainEvent("ORDER", "C", 3L));

        RecoveryService recoveryService = new RecoveryService(eventLog, snapshotStore);
        RecoveredState recovered = recoveryService.recover("INIT", (state, event) -> state + event.payload());

        assertEquals("S2C", recovered.state());
        assertEquals(3L, recovered.lastSequence());
    }
}
