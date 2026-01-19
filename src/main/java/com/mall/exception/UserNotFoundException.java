package com.mall.exception;

/**
 * 用户未找到异常
 *
 * @author mall
 */
public class UserNotFoundException extends BaseBusinessException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }

    public UserNotFoundException(String message, String errorCode, Object... params) {
        super(message, errorCode, params);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(String message, Throwable cause, String errorCode) {
        super(message, cause, errorCode);
    }

    public UserNotFoundException(String message, Throwable cause, String errorCode, Object... params) {
        super(message, cause, errorCode, params);
    }

    @Override
    protected String getDefaultErrorCode() {
        return "USER_NOT_FOUND";
    }
}