package com.swingfrog.summer2.starter;

/**
 * @author: toke
 */
public class StarterRuntimeException extends RuntimeException {
    public StarterRuntimeException() {
    }

    public StarterRuntimeException(String message) {
        super(message);
    }

    public StarterRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public StarterRuntimeException(Throwable cause) {
        super(cause);
    }

    public StarterRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
