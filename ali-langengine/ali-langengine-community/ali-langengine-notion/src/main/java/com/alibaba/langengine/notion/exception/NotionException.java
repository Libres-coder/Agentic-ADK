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
package com.alibaba.langengine.notion.exception;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

/**
 * Notion API异常
 * 
 * @author xiaoxuan.lp
 */
@Getter
public class NotionException extends Exception {
    
    private final int statusCode;
    private final JSONObject errorDetails;
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     */
    public NotionException(String message) {
        super(message);
        this.statusCode = -1;
        this.errorDetails = null;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param cause 原因
     */
    public NotionException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.errorDetails = null;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param statusCode HTTP状态码
     * @param errorDetails 错误详情
     */
    public NotionException(String message, int statusCode, JSONObject errorDetails) {
        super(message);
        this.statusCode = statusCode;
        this.errorDetails = errorDetails;
    }
    
    /**
     * 获取错误代码
     * 
     * @return 错误代码
     */
    public String getErrorCode() {
        if (errorDetails != null) {
            return errorDetails.getString("code");
        }
        return "unknown";
    }
    
    /**
     * 获取错误类型
     * 
     * @return 错误类型
     */
    public String getErrorType() {
        if (errorDetails != null) {
            return errorDetails.getString("object");
        }
        return "error";
    }
    
    /**
     * 是否是认证错误
     * 
     * @return 是否是认证错误
     */
    public boolean isAuthenticationError() {
        return statusCode == 401;
    }
    
    /**
     * 是否是权限错误
     * 
     * @return 是否是权限错误
     */
    public boolean isForbiddenError() {
        return statusCode == 403;
    }
    
    /**
     * 是否是资源未找到错误
     * 
     * @return 是否是资源未找到错误
     */
    public boolean isNotFoundError() {
        return statusCode == 404;
    }
    
    /**
     * 是否是请求限制错误
     * 
     * @return 是否是请求限制错误
     */
    public boolean isRateLimitError() {
        return statusCode == 429;
    }
    
    /**
     * 是否是服务器错误
     * 
     * @return 是否是服务器错误
     */
    public boolean isServerError() {
        return statusCode >= 500 && statusCode < 600;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NotionException{");
        sb.append("message='").append(getMessage()).append('\'');
        if (statusCode > 0) {
            sb.append(", statusCode=").append(statusCode);
        }
        if (errorDetails != null) {
            sb.append(", errorCode='").append(getErrorCode()).append('\'');
            sb.append(", errorType='").append(getErrorType()).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
