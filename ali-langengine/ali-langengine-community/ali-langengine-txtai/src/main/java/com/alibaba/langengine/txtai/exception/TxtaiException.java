/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.txtai.exception;


public class TxtaiException extends RuntimeException {

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 错误类型
     */
    private final ErrorType errorType;

    public enum ErrorType {
        CONFIGURATION_ERROR,  // 配置错误
        VALIDATION_ERROR,     // 参数验证错误
        NETWORK_ERROR,        // 网络连接错误
        API_ERROR,           // API调用错误
        PROCESSING_ERROR     // 处理错误
    }

    public TxtaiException(String message) {
        super(message);
        this.errorCode = "TXTAI_UNKNOWN";
        this.errorType = ErrorType.PROCESSING_ERROR;
    }

    public TxtaiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TXTAI_UNKNOWN";
        this.errorType = ErrorType.PROCESSING_ERROR;
    }

    public TxtaiException(String errorCode, String message, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    public TxtaiException(String errorCode, String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    // 静态工厂方法

    /**
     * 配置错误
     */
    public static TxtaiException configurationError(String message) {
        return new TxtaiException("TXTAI_CONFIG_ERROR", message, ErrorType.CONFIGURATION_ERROR);
    }

    /**
     * 参数验证错误
     */
    public static TxtaiException validationError(String message) {
        return new TxtaiException("TXTAI_VALIDATION_ERROR", message, ErrorType.VALIDATION_ERROR);
    }

    /**
     * 网络连接错误
     */
    public static TxtaiException networkError(String message, Throwable cause) {
        return new TxtaiException("TXTAI_NETWORK_ERROR", message, ErrorType.NETWORK_ERROR, cause);
    }

    /**
     * API调用错误
     */
    public static TxtaiException apiError(String message) {
        return new TxtaiException("TXTAI_API_ERROR", message, ErrorType.API_ERROR);
    }

    /**
     * API调用错误（带状态码）
     */
    public static TxtaiException apiError(int statusCode, String message) {
        return new TxtaiException("TXTAI_API_ERROR_" + statusCode,
                                "HTTP " + statusCode + ": " + message,
                                ErrorType.API_ERROR);
    }

    /**
     * 处理错误
     */
    public static TxtaiException processingError(String message, Throwable cause) {
        return new TxtaiException("TXTAI_PROCESSING_ERROR", message, ErrorType.PROCESSING_ERROR, cause);
    }

    @Override
    public String toString() {
        return String.format("TxtaiException{errorCode='%s', errorType=%s, message='%s'}",
                           errorCode, errorType, getMessage());
    }
}