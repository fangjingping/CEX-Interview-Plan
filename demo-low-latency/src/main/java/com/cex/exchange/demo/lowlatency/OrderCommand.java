package com.cex.exchange.demo.lowlatency;

/**
 * OrderCommand 核心类。
 */
public class OrderCommand {
    private long sequence;
    private String symbol;
    private long priceTicks;
    private long quantityLots;
    private long timestampMillis;

    public void reset(long sequence, String symbol, long priceTicks, long quantityLots, long timestampMillis) {
        this.sequence = sequence;
        this.symbol = symbol;
        this.priceTicks = priceTicks;
        this.quantityLots = quantityLots;
        this.timestampMillis = timestampMillis;
    }

    public long getSequence() {
        return sequence;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getPriceTicks() {
        return priceTicks;
    }

    public long getQuantityLots() {
        return quantityLots;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}
