package com.swingfrog.summer2.dao.jdbc;

/**
 * @author: toke
 */
public class JdbcRuntimeException extends RuntimeException {
    public JdbcRuntimeException() {
    }

    public JdbcRuntimeException(String message) {
        super(message);
    }

    public JdbcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcRuntimeException(Throwable cause) {
        super(cause);
    }

    public JdbcRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
