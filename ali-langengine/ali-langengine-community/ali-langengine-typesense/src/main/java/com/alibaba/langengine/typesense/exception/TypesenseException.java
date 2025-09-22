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
package com.alibaba.langengine.typesense.exception;


public class TypesenseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误码常量
     */
    public static final String CONNECTION_ERROR = "TYPESENSE_CONNECTION_ERROR";
    public static final String CONFIGURATION_ERROR = "TYPESENSE_CONFIG_ERROR";
    public static final String OPERATION_ERROR = "TYPESENSE_OPERATION_ERROR";
    public static final String VALIDATION_ERROR = "TYPESENSE_VALIDATION_ERROR";
    public static final String INITIALIZATION_ERROR = "TYPESENSE_INIT_ERROR";

    public TypesenseException(String message) {
        this(OPERATION_ERROR, message);
    }

    public TypesenseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TypesenseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 连接错误
     */
    public static TypesenseException connectionError(String message, Throwable cause) {
        return new TypesenseException(CONNECTION_ERROR, "Typesense连接失败: " + message, cause);
    }

    /**
     * 配置错误
     */
    public static TypesenseException configurationError(String message) {
        return new TypesenseException(CONFIGURATION_ERROR, "Typesense配置错误: " + message);
    }

    /**
     * 操作错误
     */
    public static TypesenseException operationError(String message, Throwable cause) {
        return new TypesenseException(OPERATION_ERROR, "Typesense操作失败: " + message, cause);
    }

    /**
     * 参数验证错误
     */
    public static TypesenseException validationError(String message) {
        return new TypesenseException(VALIDATION_ERROR, "参数验证失败: " + message);
    }

    /**
     * 初始化错误
     */
    public static TypesenseException initializationError(String message, Throwable cause) {
        return new TypesenseException(INITIALIZATION_ERROR, "Typesense初始化失败: " + message, cause);
    }

    @Override
    public String toString() {
        return "TypesenseException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}