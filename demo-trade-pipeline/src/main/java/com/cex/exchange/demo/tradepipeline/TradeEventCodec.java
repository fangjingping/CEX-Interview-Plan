package com.cex.exchange.demo.tradepipeline;

import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * TradeEventCodec 核心类。
 */
public class TradeEventCodec {
    private static final String SEP = "|";
    private static final int FIELD_COUNT = 8;

    public String encode(TradeEvent event) {
        Objects.requireNonNull(event, "event");
        return String.join(SEP,
                event.getEventId(),
                event.getSymbol(),
                event.getSide().name(),
                event.getPrice().toPlainString(),
                event.getQuantity().toPlainString(),
                event.getTakerUserId(),
                event.getMakerUserId(),
                String.valueOf(event.getTimestamp()));
    }

    public TradeEvent decode(String payload) {
        Objects.requireNonNull(payload, "payload");
        String[] parts = payload.split("\\|", -1);
        if (parts.length != FIELD_COUNT) {
            throw new IllegalArgumentException("invalid trade event payload");
        }
        return new TradeEvent(
                parts[0],
                parts[1],
                OrderSide.valueOf(parts[2]),
                new BigDecimal(parts[3]),
                new BigDecimal(parts[4]),
                parts[5],
                parts[6],
                Long.parseLong(parts[7])
        );
    }
}
