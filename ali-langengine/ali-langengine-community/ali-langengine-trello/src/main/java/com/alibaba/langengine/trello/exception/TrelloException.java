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
package com.alibaba.langengine.trello.exception;


public class TrelloException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * HTTP状态码
     */
    private int statusCode;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 错误详情
     */
    private String errorDetail;
    
    /**
     * 默认构造函数
     */
    public TrelloException() {
        super();
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public TrelloException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因异常
     */
    public TrelloException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     * 
     * @param cause 原因异常
     */
    public TrelloException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param statusCode HTTP状态码
     */
    public TrelloException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param statusCode HTTP状态码
     * @param errorCode 错误代码
     */
    public TrelloException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param statusCode HTTP状态码
     * @param errorCode 错误代码
     * @param errorDetail 错误详情
     */
    public TrelloException(String message, int statusCode, String errorCode, String errorDetail) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
    }
    
    /**
     * 获取HTTP状态码
     * 
     * @return HTTP状态码
     */
    public int getStatusCode() {
        return statusCode;
    }
    
    /**
     * 设置HTTP状态码
     * 
     * @param statusCode HTTP状态码
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 设置错误代码
     * 
     * @param errorCode 错误代码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误详情
     * 
     * @return 错误详情
     */
    public String getErrorDetail() {
        return errorDetail;
    }
    
    /**
     * 设置错误详情
     * 
     * @param errorDetail 错误详情
     */
    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }
    
    /**
     * 是否为认证错误
     * 
     * @return 是否为认证错误
     */
    public boolean isAuthenticationError() {
        return statusCode == 401 || statusCode == 403;
    }
    
    /**
     * 是否为资源不存在错误
     * 
     * @return 是否为资源不存在错误
     */
    public boolean isNotFoundError() {
        return statusCode == 404;
    }
    
    /**
     * 是否为速率限制错误
     * 
     * @return 是否为速率限制错误
     */
    public boolean isRateLimitError() {
        return statusCode == 429;
    }
    
    /**
     * 是否为服务器错误
     * 
     * @return 是否为服务器错误
     */
    public boolean isServerError() {
        return statusCode >= 500;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TrelloException{");
        sb.append("message='").append(getMessage()).append("'");
        if (statusCode > 0) {
            sb.append(", statusCode=").append(statusCode);
        }
        if (errorCode != null) {
            sb.append(", errorCode='").append(errorCode).append("'");
        }
        if (errorDetail != null) {
            sb.append(", errorDetail='").append(errorDetail).append("'");
        }
        sb.append("}");
        return sb.toString();
    }
}
