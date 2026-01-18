package com.cex.exchange.demo.liquidation;

public class LiquidationException extends RuntimeException {
    private final LiquidationErrorCode errorCode;

    public LiquidationException(LiquidationErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LiquidationErrorCode getErrorCode() {
        return errorCode;
    }
}
