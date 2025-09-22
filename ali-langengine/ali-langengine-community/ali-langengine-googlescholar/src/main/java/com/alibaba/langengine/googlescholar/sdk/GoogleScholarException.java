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
package com.alibaba.langengine.googlescholar.sdk;

/**
 * Google Scholar 搜索异常类
 * 用于处理搜索过程中的各种异常情况
 */
public class GoogleScholarException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * HTTP状态码
     */
    private Integer httpStatusCode;
    
    /**
     * 构造函数
     */
    public GoogleScholarException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     */
    public GoogleScholarException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     */
    public GoogleScholarException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     */
    public GoogleScholarException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     */
    public GoogleScholarException(String errorCode, String message, Integer httpStatusCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }
    
    /**
     * 构造函数
     */
    public GoogleScholarException(String errorCode, String message, Integer httpStatusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }
    
    /**
     * 获取错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 设置错误代码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * 获取HTTP状态码
     */
    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }
    
    /**
     * 设置HTTP状态码
     */
    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
    
    /**
     * 检查是否是网络错误
     */
    public boolean isNetworkError() {
        return errorCode != null && errorCode.startsWith("NETWORK_");
    }
    
    /**
     * 检查是否是解析错误
     */
    public boolean isParseError() {
        return errorCode != null && errorCode.startsWith("PARSE_");
    }
    
    /**
     * 检查是否是认证错误
     */
    public boolean isAuthError() {
        return errorCode != null && errorCode.startsWith("AUTH_");
    }
    
    /**
     * 检查是否是限流错误
     */
    public boolean isRateLimitError() {
        return errorCode != null && errorCode.startsWith("RATE_LIMIT_");
    }
    
    /**
     * 获取详细的错误信息
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (errorCode != null) {
            sb.append("[").append(errorCode).append("] ");
        }
        
        sb.append(getMessage());
        
        if (httpStatusCode != null) {
            sb.append(" (HTTP ").append(httpStatusCode).append(")");
        }
        
        if (getCause() != null) {
            sb.append(" - Cause: ").append(getCause().getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * 创建网络错误异常
     */
    public static GoogleScholarException networkError(String message) {
        return new GoogleScholarException("NETWORK_ERROR", message);
    }
    
    /**
     * 创建网络错误异常
     */
    public static GoogleScholarException networkError(String message, Throwable cause) {
        return new GoogleScholarException("NETWORK_ERROR", message, cause);
    }
    
    /**
     * 创建解析错误异常
     */
    public static GoogleScholarException parseError(String message) {
        return new GoogleScholarException("PARSE_ERROR", message);
    }
    
    /**
     * 创建解析错误异常
     */
    public static GoogleScholarException parseError(String message, Throwable cause) {
        return new GoogleScholarException("PARSE_ERROR", message, cause);
    }
    
    /**
     * 创建认证错误异常
     */
    public static GoogleScholarException authError(String message) {
        return new GoogleScholarException("AUTH_ERROR", message);
    }
    
    /**
     * 创建认证错误异常
     */
    public static GoogleScholarException authError(String message, Integer httpStatusCode) {
        return new GoogleScholarException("AUTH_ERROR", message, httpStatusCode);
    }
    
    /**
     * 创建限流错误异常
     */
    public static GoogleScholarException rateLimitError(String message) {
        return new GoogleScholarException("RATE_LIMIT_ERROR", message);
    }
    
    /**
     * 创建限流错误异常
     */
    public static GoogleScholarException rateLimitError(String message, Integer httpStatusCode) {
        return new GoogleScholarException("RATE_LIMIT_ERROR", message, httpStatusCode);
    }
    
    /**
     * 创建参数错误异常
     */
    public static GoogleScholarException parameterError(String message) {
        return new GoogleScholarException("PARAMETER_ERROR", message);
    }
    
    /**
     * 创建服务不可用错误异常
     */
    public static GoogleScholarException serviceUnavailableError(String message) {
        return new GoogleScholarException("SERVICE_UNAVAILABLE_ERROR", message);
    }
    
    /**
     * 创建服务不可用错误异常
     */
    public static GoogleScholarException serviceUnavailableError(String message, Integer httpStatusCode) {
        return new GoogleScholarException("SERVICE_UNAVAILABLE_ERROR", message, httpStatusCode);
    }
}
