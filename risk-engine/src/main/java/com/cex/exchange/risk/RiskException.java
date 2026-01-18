package com.cex.exchange.risk;

public class RiskException extends RuntimeException {
    private final RiskErrorCode errorCode;

    public RiskException(RiskErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RiskErrorCode getErrorCode() {
        return errorCode;
    }
}
