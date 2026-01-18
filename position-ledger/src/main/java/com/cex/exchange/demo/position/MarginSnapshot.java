package com.cex.exchange.demo.position;

import java.math.BigDecimal;
import java.util.Objects;

public record MarginSnapshot(BigDecimal equity,
                             BigDecimal available,
                             BigDecimal used,
                             BigDecimal imr,
                             BigDecimal mmr,
                             BigDecimal liquidationPrice,
                             BigDecimal bankruptcyPrice) {
    public MarginSnapshot {
        Objects.requireNonNull(equity, "equity");
        Objects.requireNonNull(available, "available");
        Objects.requireNonNull(used, "used");
        Objects.requireNonNull(imr, "imr");
        Objects.requireNonNull(mmr, "mmr");
        Objects.requireNonNull(liquidationPrice, "liquidationPrice");
        Objects.requireNonNull(bankruptcyPrice, "bankruptcyPrice");
    }
}
