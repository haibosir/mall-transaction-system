package com.mall.exception;

/**
 * 余额不足异常
 *
 * @author mall
 */
public class InsufficientBalanceException extends BaseBusinessException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public InsufficientBalanceException(String message, String errorCode, Object... params) {
        super(message, errorCode, params);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientBalanceException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }

    public InsufficientBalanceException(String message, Throwable cause, String errorCode, Object... params) {
        super(message, cause, errorCode, params);
    }

    @Override
    protected String getDefaultErrorCode() {
        return "INSUFFICIENT_BALANCE";
    }
}