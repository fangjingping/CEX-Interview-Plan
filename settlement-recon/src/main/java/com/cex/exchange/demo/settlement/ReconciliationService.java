package com.cex.exchange.demo.settlement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReconciliationService 核心类。
 */
public class ReconciliationService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public ReconciliationReport reconcileFees(List<Trade> trades, List<FeeEntry> feeEntries) {
        Map<String, BigDecimal> feeByTradeId = new HashMap<>();
        for (FeeEntry entry : feeEntries) {
            feeByTradeId.merge(entry.tradeId(), entry.amount(), BigDecimal::add);
        }

        BigDecimal tradeFeeTotal = ZERO;
        BigDecimal ledgerFeeTotal = ZERO;
        List<String> missing = new ArrayList<>();
        for (Trade trade : trades) {
            tradeFeeTotal = tradeFeeTotal.add(trade.fee());
            if (!feeByTradeId.containsKey(trade.tradeId())) {
                missing.add(trade.tradeId());
            }
        }
        for (FeeEntry entry : feeEntries) {
            ledgerFeeTotal = ledgerFeeTotal.add(entry.amount());
        }

        boolean matched = missing.isEmpty() && tradeFeeTotal.compareTo(ledgerFeeTotal) == 0;
        return new ReconciliationReport(tradeFeeTotal, ledgerFeeTotal, List.copyOf(missing), matched);
    }
}
