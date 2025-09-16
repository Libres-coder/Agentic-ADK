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
package com.alibaba.langengine.onedrive.exception;

/**
 * OneDrive 异常类
 * 
 * @author AIDC-AI
 */
public class OneDriveException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     */
    public OneDriveException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public OneDriveException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     */
    public OneDriveException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     * 
     * @param message 错误消息
     * @param cause 原因
     * @param errorCode 错误代码
     */
    public OneDriveException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
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
}
