package com.cex.exchange.demo.pricing;

public class PricingException extends RuntimeException {
    private final PricingErrorCode errorCode;

    public PricingException(PricingErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PricingErrorCode getErrorCode() {
        return errorCode;
    }
}
