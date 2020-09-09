package com.swingfrog.summer2.dao.meta;

/**
 * @author: toke
 */
public class MetaRuntimeException extends RuntimeException {
    public MetaRuntimeException() {
    }

    public MetaRuntimeException(String message) {
        super(message);
    }

    public MetaRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetaRuntimeException(Throwable cause) {
        super(cause);
    }

    public MetaRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
