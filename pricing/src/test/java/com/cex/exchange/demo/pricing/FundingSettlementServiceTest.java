package com.cex.exchange.demo.pricing;

import com.cex.exchange.demo.position.PositionSide;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FundingSettlementServiceTest {

    @Test
    void settlementConservesFunding() {
        PricingPolicy policy = new PricingPolicy(new BigDecimal("0.2"), new BigDecimal("0.5"), new BigDecimal("0.05"));
        InMemoryPriceSource priceSource = new InMemoryPriceSource();
        priceSource.updateIndexPrice(new IndexPrice("BTC-USDT", new BigDecimal("100"), 1L));
        priceSource.updateLastTradePrice("BTC-USDT", new BigDecimal("110"));

        MarkPriceCalculator markCalculator = new MarkPriceCalculator(priceSource, policy);
        FundingRateCalculator rateCalculator = new FundingRateCalculator(policy);
        InMemoryPositionProvider positionProvider = new InMemoryPositionProvider();
        positionProvider.upsert(new PositionSnapshot("U1", "BTC-USDT", PositionSide.LONG, new BigDecimal("1")));
        positionProvider.upsert(new PositionSnapshot("U2", "BTC-USDT", PositionSide.SHORT, new BigDecimal("1")));

        InMemoryFundingLedger ledger = new InMemoryFundingLedger();
        FundingSettlementService service = new FundingSettlementService(positionProvider, markCalculator,
                rateCalculator, ledger);

        FundingSettlement settlement = service.settle("BTC-USDT", 10L);

        BigDecimal total = settlement.transfers().stream()
                .map(FundingTransfer::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertBigDecimal("0", total);
    }

    @Test
    void fundingRateIsCapped() {
        PricingPolicy policy = new PricingPolicy(new BigDecimal("0.2"), new BigDecimal("0.5"), new BigDecimal("0.01"));
        FundingRateCalculator calculator = new FundingRateCalculator(policy);
        MarkPrice markPrice = new MarkPrice("ETH-USDT", new BigDecimal("200"), 2L,
                new BigDecimal("0.05"), new BigDecimal("200"));

        FundingRate rate = calculator.calculate(markPrice);

        assertBigDecimal("0.01", rate.rate());
    }

    @Test
    void settlementIsIdempotentForSameTimestamp() {
        PricingPolicy policy = new PricingPolicy(new BigDecimal("0.2"), new BigDecimal("0.5"), new BigDecimal("0.05"));
        InMemoryPriceSource priceSource = new InMemoryPriceSource();
        priceSource.updateIndexPrice(new IndexPrice("BTC-USDT", new BigDecimal("100"), 1L));
        priceSource.updateLastTradePrice("BTC-USDT", new BigDecimal("105"));

        MarkPriceCalculator markCalculator = new MarkPriceCalculator(priceSource, policy);
        FundingRateCalculator rateCalculator = new FundingRateCalculator(policy);
        InMemoryPositionProvider positionProvider = new InMemoryPositionProvider();
        positionProvider.upsert(new PositionSnapshot("U1", "BTC-USDT", PositionSide.LONG, new BigDecimal("2")));
        positionProvider.upsert(new PositionSnapshot("U2", "BTC-USDT", PositionSide.SHORT, new BigDecimal("2")));

        InMemoryFundingLedger ledger = new InMemoryFundingLedger();
        FundingSettlementService service = new FundingSettlementService(positionProvider, markCalculator,
                rateCalculator, ledger);

        FundingSettlement first = service.settle("BTC-USDT", 20L);
        FundingSettlement second = service.settle("BTC-USDT", 20L);

        assertEquals(first, second);
        assertEquals(1, ledger.settlements().size());
    }

    private void assertBigDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
