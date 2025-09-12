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
package com.alibaba.langengine.lancedb;


public class LanceDbException extends Exception {

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * HTTP状态码（如果适用）
     */
    private final Integer httpStatusCode;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public LanceDbException(String message) {
        super(message);
        this.errorCode = null;
        this.httpStatusCode = null;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause   原因异常
     */
    public LanceDbException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.httpStatusCode = null;
    }

    /**
     * 构造函数
     *
     * @param message    错误消息
     * @param errorCode  错误代码
     */
    public LanceDbException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = null;
    }

    /**
     * 构造函数
     *
     * @param message        错误消息
     * @param errorCode      错误代码
     * @param httpStatusCode HTTP状态码
     */
    public LanceDbException(String message, String errorCode, Integer httpStatusCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * 构造函数
     *
     * @param message        错误消息
     * @param cause          原因异常
     * @param errorCode      错误代码
     * @param httpStatusCode HTTP状态码
     */
    public LanceDbException(String message, Throwable cause, String errorCode, Integer httpStatusCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
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
     * 获取HTTP状态码
     *
     * @return HTTP状态码
     */
    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * 是否有错误代码
     *
     * @return 是否有错误代码
     */
    public boolean hasErrorCode() {
        return errorCode != null && !errorCode.trim().isEmpty();
    }

    /**
     * 是否有HTTP状态码
     *
     * @return 是否有HTTP状态码
     */
    public boolean hasHttpStatusCode() {
        return httpStatusCode != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (hasErrorCode()) {
            sb.append(" [ErrorCode: ").append(errorCode).append("]");
        }
        if (hasHttpStatusCode()) {
            sb.append(" [HTTP: ").append(httpStatusCode).append("]");
        }
        return sb.toString();
    }
}

/**
 * LanceDB客户端异常，继承自LanceDbException以统一异常处理
 */
class LanceDbClientException extends LanceDbException {

    public LanceDbClientException(String message) {
        super(message);
    }

    public LanceDbClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public LanceDbClientException(String message, String errorCode) {
        super(message, errorCode);
    }

    public LanceDbClientException(String message, String errorCode, Integer httpStatusCode) {
        super(message, errorCode, httpStatusCode);
    }

    public LanceDbClientException(String message, Throwable cause, String errorCode, Integer httpStatusCode) {
        super(message, cause, errorCode, httpStatusCode);
    }
}

/**
 * LanceDB连接异常
 */
class LanceDbConnectionException extends LanceDbException {

    public LanceDbConnectionException(String message) {
        super(message, "CONNECTION_ERROR");
    }

    public LanceDbConnectionException(String message, Throwable cause) {
        super(message, cause, "CONNECTION_ERROR", null);
    }
}

/**
 * LanceDB配置异常
 */
class LanceDbConfigurationException extends LanceDbException {

    public LanceDbConfigurationException(String message) {
        super(message, "CONFIGURATION_ERROR");
    }

    public LanceDbConfigurationException(String message, Throwable cause) {
        super(message, cause, "CONFIGURATION_ERROR", null);
    }
}

/**
 * LanceDB向量异常
 */
class LanceDbVectorException extends LanceDbException {

    public LanceDbVectorException(String message) {
        super(message, "VECTOR_ERROR");
    }

    public LanceDbVectorException(String message, Throwable cause) {
        super(message, cause, "VECTOR_ERROR", null);
    }
}
