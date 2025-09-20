package com.alibaba.langengine.wecom.exception;


public class WeComException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    public WeComException(String message) {
        super(message);
        this.errorMsg = message;
    }

    public WeComException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
    
    public WeComException(int errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = String.valueOf(errorCode);
        this.errorMsg = errorMsg;
    }

    public WeComException(String message, Throwable cause) {
        super(message, cause);
        this.errorMsg = message;
    }

    public WeComException(String errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return String.format("WeComException{errorCode='%s', errorMsg='%s'}", errorCode, errorMsg);
    }
}
