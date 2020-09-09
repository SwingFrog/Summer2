package com.swingfrog.summer2.core.configuration;

/**
 * @author: toke
 */
public class ConfigurationRuntimeException extends RuntimeException {
    public ConfigurationRuntimeException() {
    }

    public ConfigurationRuntimeException(String message) {
        super(message);
    }

    public ConfigurationRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationRuntimeException(Throwable cause) {
        super(cause);
    }

    public ConfigurationRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
