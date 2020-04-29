package com.swingfrog.summer2.core.ioc;

/**
 * @author: toke
 */
public class IocRuntimeException extends RuntimeException {
    public IocRuntimeException() {
    }

    public IocRuntimeException(String message) {
        super(message);
    }

    public IocRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IocRuntimeException(Throwable cause) {
        super(cause);
    }

    public IocRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
