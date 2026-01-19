package com.mall.exception;

/**
 * 商家未找到异常
 *
 * @author mall
 */
public class MerchantNotFoundException extends BaseBusinessException {

    public MerchantNotFoundException(String message) {
        super(message);
    }

    public MerchantNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }

    public MerchantNotFoundException(String message, String errorCode, Object... params) {
        super(message, errorCode, params);
    }

    public MerchantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MerchantNotFoundException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }

    public MerchantNotFoundException(String message, Throwable cause, String errorCode, Object... params) {
        super(message, cause, errorCode, params);
    }

    @Override
    protected String getDefaultErrorCode() {
        return "MERCHANT_NOT_FOUND";
    }
}