package com.cex.exchange.risk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TieredRiskLimitPolicy implements RiskLimitPolicy {
    private final Map<String, List<RiskLimitTier>> tiersBySymbol;
    private final List<RiskLimitTier> defaultTiers;

    public TieredRiskLimitPolicy() {
        this(Map.of(), List.of(
                new RiskLimitTier(new BigDecimal("5"), new BigDecimal("100000")),
                new RiskLimitTier(new BigDecimal("10"), new BigDecimal("50000")),
                new RiskLimitTier(new BigDecimal("20"), new BigDecimal("20000"))
        ));
    }

    public TieredRiskLimitPolicy(Map<String, List<RiskLimitTier>> tiersBySymbol, List<RiskLimitTier> defaultTiers) {
        Objects.requireNonNull(tiersBySymbol, "tiersBySymbol");
        Objects.requireNonNull(defaultTiers, "defaultTiers");
        if (defaultTiers.isEmpty()) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "default tiers must not be empty");
        }
        this.tiersBySymbol = new ConcurrentHashMap<>();
        for (var entry : tiersBySymbol.entrySet()) {
            this.tiersBySymbol.put(entry.getKey(), normalize(entry.getValue()));
        }
        this.defaultTiers = normalize(defaultTiers);
    }

    @Override
    public RiskLimitResult evaluate(OrderRequest request) {
        Objects.requireNonNull(request, "request");
        List<RiskLimitTier> tiers = tiersBySymbol.getOrDefault(request.symbol(), defaultTiers);
        BigDecimal notional = request.notional();
        BigDecimal maxNotional = tiers.get(tiers.size() - 1).maxNotional();
        for (RiskLimitTier tier : tiers) {
            if (request.leverage().compareTo(tier.maxLeverage()) <= 0) {
                maxNotional = tier.maxNotional();
                break;
            }
        }
        if (notional.compareTo(maxNotional) <= 0) {
            return RiskLimitResult.allowed(maxNotional, notional);
        }
        return RiskLimitResult.rejected(maxNotional, notional);
    }

    private List<RiskLimitTier> normalize(List<RiskLimitTier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new RiskException(RiskErrorCode.INVALID_REQUEST, "tiers must not be empty");
        }
        List<RiskLimitTier> copy = new ArrayList<>(tiers);
        copy.sort(Comparator.comparing(RiskLimitTier::maxLeverage));
        return List.copyOf(copy);
    }
}
