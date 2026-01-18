package com.cex.exchange.demo.pricing;

import com.cex.exchange.demo.position.PositionSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FundingSettlementService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int AMOUNT_SCALE = 8;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final PositionProvider positionProvider;
    private final MarkPriceCalculator markPriceCalculator;
    private final FundingRateCalculator fundingRateCalculator;
    private final FundingLedger ledger;
    private final Map<String, FundingSettlement> settled = new ConcurrentHashMap<>();

    public FundingSettlementService(PositionProvider positionProvider,
                                    MarkPriceCalculator markPriceCalculator,
                                    FundingRateCalculator fundingRateCalculator,
                                    FundingLedger ledger) {
        this.positionProvider = Objects.requireNonNull(positionProvider, "positionProvider");
        this.markPriceCalculator = Objects.requireNonNull(markPriceCalculator, "markPriceCalculator");
        this.fundingRateCalculator = Objects.requireNonNull(fundingRateCalculator, "fundingRateCalculator");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
    }

    public FundingSettlement settle(String symbol, long timestamp) {
        if (symbol == null || symbol.isBlank()) {
            throw new PricingException(PricingErrorCode.INVALID_PRICE, "symbol must not be blank");
        }
        String key = symbol + "|" + timestamp;
        return settled.computeIfAbsent(key, ignore -> {
            MarkPrice markPrice = markPriceCalculator.calculate(symbol)
                    .orElseThrow(() -> new PricingException(PricingErrorCode.PRICE_NOT_AVAILABLE,
                            "index price unavailable"));
            FundingRate rate = fundingRateCalculator.calculate(markPrice);
            List<FundingTransfer> transfers = buildTransfers(symbol, markPrice, rate, timestamp);
            FundingSettlement settlement = new FundingSettlement(symbol, markPrice.price(), rate, transfers, timestamp);
            ledger.record(settlement);
            return settlement;
        });
    }

    private List<FundingTransfer> buildTransfers(String symbol,
                                                 MarkPrice markPrice,
                                                 FundingRate rate,
                                                 long timestamp) {
        List<PositionSnapshot> snapshots = positionProvider.openPositions(symbol);
        List<FundingTransfer> transfers = new ArrayList<>(snapshots.size());
        for (PositionSnapshot snapshot : snapshots) {
            BigDecimal qty = snapshot.quantity();
            if (qty == null || qty.compareTo(ZERO) <= 0) {
                throw new PricingException(PricingErrorCode.INVALID_POSITION,
                        "quantity must be > 0 for " + snapshot.userId());
            }
            if (snapshot.side() == PositionSide.FLAT) {
                continue;
            }
            BigDecimal notional = markPrice.price().multiply(qty);
            BigDecimal amount = notional.multiply(rate.rate()).setScale(AMOUNT_SCALE, ROUNDING);
            if (snapshot.side() == PositionSide.LONG) {
                amount = amount.negate();
            }
            transfers.add(new FundingTransfer(snapshot.userId(), symbol, amount,
                    rate.rate(), markPrice.price(), timestamp));
        }
        return transfers;
    }
}
