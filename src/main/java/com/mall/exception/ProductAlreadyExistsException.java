package com.mall.exception;

/**
 * 商品已存在异常
 *
 * @author mall
 */
public class ProductAlreadyExistsException extends BaseBusinessException {

    public ProductAlreadyExistsException(String message) {
        super(message);
    }

    public ProductAlreadyExistsException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ProductAlreadyExistsException(String message, String errorCode, Object... params) {
        super(message, errorCode, params);
    }

    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductAlreadyExistsException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }

    public ProductAlreadyExistsException(String message, Throwable cause, String errorCode, Object... params) {
        super(message, cause, errorCode, params);
    }

    @Override
    protected String getDefaultErrorCode() {
        return "PRODUCT_ALREADY_EXISTS";
    }
}