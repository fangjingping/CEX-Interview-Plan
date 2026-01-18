package com.cex.exchange.demo.liquidation;

import com.cex.exchange.demo.position.PositionClose;
import com.cex.exchange.demo.position.PositionService;
import com.cex.exchange.demo.position.PositionSide;
import com.cex.exchange.model.OrderSide;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LiquidationService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final PositionService positionService;

    public LiquidationService(PositionService positionService) {
        this.positionService = Objects.requireNonNull(positionService, "positionService");
    }

    public LiquidationResult liquidate(LiquidationTask task) {
        validateTask(task);
        Optional<PositionClose> closed = positionService.closePosition(
                task.userId(), task.symbol(), task.markPrice(), task.timestamp());
        if (closed.isEmpty()) {
            return new LiquidationResult(task.taskId(), task.userId(), task.symbol(),
                    LiquidationStatus.NO_POSITION, List.of());
        }
        PositionClose close = closed.get();
        LiquidationTrade trade = toTrade(task, close);
        return new LiquidationResult(task.taskId(), task.userId(), task.symbol(),
                LiquidationStatus.EXECUTED, List.of(trade));
    }

    private LiquidationTrade toTrade(LiquidationTask task, PositionClose close) {
        OrderSide side = close.side() == PositionSide.LONG ? OrderSide.SELL : OrderSide.BUY;
        return new LiquidationTrade("LQ-" + task.taskId(),
                close.userId(),
                close.symbol(),
                side,
                close.closePrice(),
                close.quantity(),
                task.timestamp());
    }

    private void validateTask(LiquidationTask task) {
        if (task == null) {
            throw new LiquidationException(LiquidationErrorCode.INVALID_TASK, "task must not be null");
        }
        if (task.markPrice().compareTo(ZERO) <= 0) {
            throw new LiquidationException(LiquidationErrorCode.INVALID_TASK, "markPrice must be > 0");
        }
        if (task.userId().isBlank() || task.symbol().isBlank()) {
            throw new LiquidationException(LiquidationErrorCode.INVALID_TASK, "userId/symbol must not be blank");
        }
    }
}
