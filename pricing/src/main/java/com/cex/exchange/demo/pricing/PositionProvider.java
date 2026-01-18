package com.cex.exchange.demo.pricing;

import java.util.List;

public interface PositionProvider {
    List<PositionSnapshot> openPositions(String symbol);
}
