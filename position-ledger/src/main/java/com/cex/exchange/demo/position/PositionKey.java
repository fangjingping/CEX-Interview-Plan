package com.cex.exchange.demo.position;

public record PositionKey(String userId, String symbol) {
    public PositionKey {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
    }
}
