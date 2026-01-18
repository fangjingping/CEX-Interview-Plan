package com.cex.exchange.risk;

public interface RiskLimitPolicy {
    RiskLimitResult evaluate(OrderRequest request);
}
