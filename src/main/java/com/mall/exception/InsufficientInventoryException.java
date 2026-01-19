package com.mall.exception;

/**
 * 库存不足异常
 *
 * @author mall
 */
public class InsufficientInventoryException extends BaseBusinessException {

    public InsufficientInventoryException(String message) {
        super(message);
    }

    public InsufficientInventoryException(String message, String errorCode) {
        super(message, errorCode);
    }

    public InsufficientInventoryException(String message, String errorCode, Object... params) {
        super(message, errorCode, params);
    }

    public InsufficientInventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientInventoryException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }

    public InsufficientInventoryException(String message, Throwable cause, String errorCode, Object... params) {
        super(message, cause, errorCode, params);
    }

    @Override
    protected String getDefaultErrorCode() {
        return "INSUFFICIENT_INVENTORY";
    }
}