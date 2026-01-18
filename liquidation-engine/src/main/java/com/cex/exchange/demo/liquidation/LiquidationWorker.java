package com.cex.exchange.demo.liquidation;

import java.util.Objects;
import java.util.Optional;

public class LiquidationWorker {
    private final LiquidationScheduler scheduler;
    private final LiquidationService service;

    public LiquidationWorker(LiquidationScheduler scheduler, LiquidationService service) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.service = Objects.requireNonNull(service, "service");
    }

    public Optional<LiquidationResult> processNext() {
        Optional<LiquidationTask> task = scheduler.poll();
        if (task.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(service.liquidate(task.get()));
        } finally {
            scheduler.complete(task.get());
        }
    }
}
