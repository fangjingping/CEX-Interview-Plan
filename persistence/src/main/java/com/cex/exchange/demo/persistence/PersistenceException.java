package com.cex.exchange.demo.persistence;

public class PersistenceException extends RuntimeException {
    private final PersistenceErrorCode errorCode;

    public PersistenceException(PersistenceErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PersistenceException(PersistenceErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public PersistenceErrorCode getErrorCode() {
        return errorCode;
    }
}
