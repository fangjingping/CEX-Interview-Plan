package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceSource {
    Optional<IndexPrice> getIndexPrice(String symbol);

    Optional<BigDecimal> getLastTradePrice(String symbol);
}
