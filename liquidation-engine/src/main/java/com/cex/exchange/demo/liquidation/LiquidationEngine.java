package com.cex.exchange.demo.liquidation;

import com.cex.exchange.demo.risk.Position;
import com.cex.exchange.demo.risk.RiskEngine;
import com.cex.exchange.demo.risk.RiskStatus;
import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LiquidationEngine {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final RiskEngine riskEngine;
    private final Map<String, LiquidationDecision> decisions = new ConcurrentHashMap<>();
    private final Map<String, LiquidationRequest> requests = new ConcurrentHashMap<>();

    public LiquidationEngine() {
        this(new RiskEngine());
    }

    public LiquidationEngine(RiskEngine riskEngine) {
        this.riskEngine = Objects.requireNonNull(riskEngine, "riskEngine");
    }

    public LiquidationDecision evaluate(LiquidationRequest request) {
        Objects.requireNonNull(request, "request");
        validateRequest(request);
        LiquidationRequest existing = requests.putIfAbsent(request.requestId(), request);
        if (existing != null && !existing.equals(request)) {
            throw new LiquidationException(LiquidationErrorCode.REQUEST_MISMATCH, "request already exists");
        }
        return decisions.computeIfAbsent(request.requestId(), key -> computeDecision(request));
    }

    private LiquidationDecision computeDecision(LiquidationRequest request) {
        if (request.positionQuantity().compareTo(ZERO) == 0) {
            return new LiquidationDecision(request.requestId(), RiskStatus.OK, ZERO, Optional.empty());
        }
        Position position = new Position(
                request.positionQuantity(),
                request.entryPrice(),
                request.markPrice(),
                request.margin(),
                request.maintenanceMarginRate());
        RiskStatus status = riskEngine.evaluate(position);
        BigDecimal marginRatio = position.marginRatio();
        if (status != RiskStatus.LIQUIDATE) {
            return new LiquidationDecision(request.requestId(), status, marginRatio, Optional.empty());
        }
        OrderSide side = request.positionQuantity().signum() > 0 ? OrderSide.SELL : OrderSide.BUY;
        LiquidationOrder order = new LiquidationOrder(
                "LQ-" + request.requestId(),
                request.userId(),
                request.symbol(),
                side,
                request.markPrice(),
                request.positionQuantity().abs(),
                request.timestamp()
        );
        return new LiquidationDecision(request.requestId(), status, marginRatio, Optional.of(order));
    }

    private void validateRequest(LiquidationRequest request) {
        if (request.entryPrice().compareTo(ZERO) <= 0
                || request.markPrice().compareTo(ZERO) <= 0
                || request.maintenanceMarginRate().compareTo(ZERO) <= 0
                || request.margin().compareTo(ZERO) < 0) {
            throw new LiquidationException(LiquidationErrorCode.INVALID_REQUEST, "invalid request values");
        }
    }
}
