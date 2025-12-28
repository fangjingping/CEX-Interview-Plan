package com.cex.exchange.demo.settlement;

import java.math.BigDecimal;
import java.util.List;

/**
 * ReconciliationReport 记录类型。
 */
public record ReconciliationReport(BigDecimal tradeFeeTotal,
                                   BigDecimal ledgerFeeTotal,
                                   List<String> missingFeeTrades,
                                   boolean matched) {
}
