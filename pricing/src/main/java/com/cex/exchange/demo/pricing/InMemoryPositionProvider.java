package com.cex.exchange.demo.pricing;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPositionProvider implements PositionProvider {
    private final Map<String, Map<String, PositionSnapshot>> positionsBySymbol = new ConcurrentHashMap<>();

    @Override
    public List<PositionSnapshot> openPositions(String symbol) {
        Map<String, PositionSnapshot> positions = positionsBySymbol.get(symbol);
        if (positions == null) {
            return List.of();
        }
        return List.copyOf(positions.values());
    }

    public void upsert(PositionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        positionsBySymbol
                .computeIfAbsent(snapshot.symbol(), key -> new ConcurrentHashMap<>())
                .put(snapshot.userId(), snapshot);
    }
}
