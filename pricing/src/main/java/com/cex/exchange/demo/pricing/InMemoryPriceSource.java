package com.cex.exchange.demo.pricing;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPriceSource implements PriceSource {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final Map<String, IndexPrice> indexPrices = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> lastTrades = new ConcurrentHashMap<>();

    @Override
    public Optional<IndexPrice> getIndexPrice(String symbol) {
        return Optional.ofNullable(indexPrices.get(symbol));
    }

    @Override
    public Optional<BigDecimal> getLastTradePrice(String symbol) {
        return Optional.ofNullable(lastTrades.get(symbol));
    }

    public void updateIndexPrice(IndexPrice indexPrice) {
        Objects.requireNonNull(indexPrice, "indexPrice");
        if (indexPrice.price().compareTo(ZERO) <= 0) {
            throw new PricingException(PricingErrorCode.INVALID_PRICE, "indexPrice must be > 0");
        }
        indexPrices.put(indexPrice.symbol(), indexPrice);
    }

    public void updateLastTradePrice(String symbol, BigDecimal price) {
        if (symbol == null || symbol.isBlank()) {
            throw new PricingException(PricingErrorCode.INVALID_PRICE, "symbol must not be blank");
        }
        Objects.requireNonNull(price, "price");
        if (price.compareTo(ZERO) <= 0) {
            throw new PricingException(PricingErrorCode.INVALID_PRICE, "price must be > 0");
        }
        lastTrades.put(symbol, price);
    }
}
