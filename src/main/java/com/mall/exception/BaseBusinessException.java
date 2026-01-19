package com.mall.exception;

/**
 * 基础业务异常类
 * 用于统一管理业务异常
 *
 * @author mall
 */
public abstract class BaseBusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] params;

    public BaseBusinessException(String message) {
        super(message);
        this.errorCode = getDefaultErrorCode();
        this.params = null;
    }

    public BaseBusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.params = null;
    }

    public BaseBusinessException(String message, String errorCode, Object... params) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
    }

    public BaseBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = getDefaultErrorCode();
        this.params = null;
    }

    public BaseBusinessException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.params = null;
    }

    public BaseBusinessException(String message, Throwable cause, String errorCode, Object... params) {
        super(message, cause);
        this.errorCode = errorCode;
        this.params = params;
    }

    /**
     * 获取默认错误码
     */
    protected abstract String getDefaultErrorCode();

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }
}