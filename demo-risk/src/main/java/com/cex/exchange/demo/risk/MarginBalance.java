package com.cex.exchange.demo.risk;

import java.math.BigDecimal;
import java.util.Objects;

public record MarginBalance(BigDecimal available, BigDecimal frozen, BigDecimal used) {
    public static final MarginBalance ZERO = new MarginBalance(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

    public MarginBalance {
        Objects.requireNonNull(available, "available");
        Objects.requireNonNull(frozen, "frozen");
        Objects.requireNonNull(used, "used");
    }

    public MarginBalance addAvailable(BigDecimal amount) {
        return new MarginBalance(available.add(amount), frozen, used);
    }

    public MarginBalance freeze(BigDecimal amount) {
        return new MarginBalance(available.subtract(amount), frozen.add(amount), used);
    }

    public MarginBalance consume(BigDecimal amount) {
        return new MarginBalance(available, frozen.subtract(amount), used.add(amount));
    }

    public MarginBalance release(BigDecimal amount) {
        return new MarginBalance(available.add(amount), frozen.subtract(amount), used);
    }
}
