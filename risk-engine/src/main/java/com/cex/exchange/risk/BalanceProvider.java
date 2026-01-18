package com.cex.exchange.risk;

import java.math.BigDecimal;

public interface BalanceProvider {
    BigDecimal available(String userId);

    boolean tryDebit(String userId, BigDecimal amount);

    void credit(String userId, BigDecimal amount);
}
