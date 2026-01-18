package com.cex.exchange.demo.position;

public class PositionException extends RuntimeException {
    private final PositionErrorCode errorCode;

    public PositionException(PositionErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PositionErrorCode getErrorCode() {
        return errorCode;
    }
}
