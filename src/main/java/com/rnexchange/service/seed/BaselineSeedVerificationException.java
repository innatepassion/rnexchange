package com.rnexchange.service.seed;

public class BaselineSeedVerificationException extends RuntimeException {

    public BaselineSeedVerificationException(String message) {
        super(message);
    }

    public BaselineSeedVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
