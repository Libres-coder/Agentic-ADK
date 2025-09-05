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
package com.alibaba.langengine.wenxin.exception;


public class WenxinApiException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;
    private final WenxinErrorType errorType;

    /**
     * 错误类型枚举
     */
    public enum WenxinErrorType {
        AUTHENTICATION_ERROR("认证错误"),
        RATE_LIMIT_ERROR("请求限流"),
        NETWORK_ERROR("网络错误"),
        INVALID_REQUEST("请求参数错误"),
        SERVICE_ERROR("服务端错误"),
        UNKNOWN_ERROR("未知错误");

        private final String description;

        WenxinErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public WenxinApiException(String message) {
        super(message);
        this.errorCode = null;
        this.httpStatus = 0;
        this.errorType = WenxinErrorType.UNKNOWN_ERROR;
    }

    public WenxinApiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.httpStatus = 0;
        this.errorType = WenxinErrorType.UNKNOWN_ERROR;
    }

    public WenxinApiException(String message, String errorCode, int httpStatus, WenxinErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.errorType = errorType;
    }

    public WenxinApiException(String message, String errorCode, int httpStatus, WenxinErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.errorType = errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public WenxinErrorType getErrorType() {
        return errorType;
    }

    /**
     * 根据HTTP状态码判断错误类型
     */
    public static WenxinErrorType getErrorTypeByHttpStatus(int httpStatus) {
        switch (httpStatus) {
            case 401:
            case 403:
                return WenxinErrorType.AUTHENTICATION_ERROR;
            case 429:
                return WenxinErrorType.RATE_LIMIT_ERROR;
            case 400:
                return WenxinErrorType.INVALID_REQUEST;
            case 500:
            case 502:
            case 503:
            case 504:
                return WenxinErrorType.SERVICE_ERROR;
            default:
                return WenxinErrorType.UNKNOWN_ERROR;
        }
    }

    @Override
    public String toString() {
        return String.format("WenxinApiException{errorType=%s, errorCode='%s', httpStatus=%d, message='%s'}", 
                            errorType, errorCode, httpStatus, getMessage());
    }
}
