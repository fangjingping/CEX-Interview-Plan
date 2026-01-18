package com.cex.exchange.demo.pricing;

public interface FundingLedger {
    void record(FundingSettlement settlement);
}
