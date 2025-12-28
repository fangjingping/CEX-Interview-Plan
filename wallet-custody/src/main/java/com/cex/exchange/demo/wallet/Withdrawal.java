package com.cex.exchange.demo.wallet;

import java.math.BigDecimal;

/**
 * Withdrawal 记录类型。
 */
public record Withdrawal(String withdrawalId,
                         String userId,
                         String asset,
                         BigDecimal amount,
                         WithdrawalStatus status) {
}
