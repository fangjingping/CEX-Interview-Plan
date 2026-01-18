package com.cex.exchange.demo.pricing;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryFundingLedger implements FundingLedger {
    private final List<FundingSettlement> settlements = new CopyOnWriteArrayList<>();

    @Override
    public void record(FundingSettlement settlement) {
        Objects.requireNonNull(settlement, "settlement");
        settlements.add(settlement);
    }

    public List<FundingSettlement> settlements() {
        return List.copyOf(settlements);
    }
}
