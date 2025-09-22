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
package com.alibaba.langengine.hugegraph.exception;


public class HugeGraphVectorStoreException extends RuntimeException {
    
    /**
     * 异常类型枚举
     */
    public enum ErrorType {
        /**
         * 连接错误
         */
        CONNECTION_ERROR("CONNECTION_ERROR", "HugeGraph connection failed"),
        
        /**
         * 配置错误
         */
        CONFIGURATION_ERROR("CONFIGURATION_ERROR", "HugeGraph configuration error"),
        
        /**
         * 向量搜索失败
         */
        VECTOR_SEARCH_FAILED("VECTOR_SEARCH_FAILED", "Vector similarity search failed"),
        
        /**
         * 向量插入失败
         */
        VECTOR_INSERT_FAILED("VECTOR_INSERT_FAILED", "Vector insertion failed"),
        
        /**
         * 向量维度错误
         */
        VECTOR_DIMENSION_ERROR("VECTOR_DIMENSION_ERROR", "Vector dimension mismatch"),
        
        /**
         * 图操作失败
         */
        GRAPH_OPERATION_FAILED("GRAPH_OPERATION_FAILED", "Graph operation failed"),
        
        /**
         * 索引操作失败
         */
        INDEX_OPERATION_FAILED("INDEX_OPERATION_FAILED", "Index operation failed"),
        
        /**
         * 数据序列化错误
         */
        SERIALIZATION_ERROR("SERIALIZATION_ERROR", "Data serialization/deserialization error"),
        
        /**
         * 超时错误
         */
        TIMEOUT_ERROR("TIMEOUT_ERROR", "Operation timeout"),
        
        /**
         * 权限错误
         */
        PERMISSION_ERROR("PERMISSION_ERROR", "Permission denied"),
        
        /**
         * 资源不存在错误
         */
        RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found"),
        
        /**
         * 未知错误
         */
        UNKNOWN_ERROR("UNKNOWN_ERROR", "Unknown error occurred");
        
        private final String code;
        private final String description;
        
        ErrorType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final ErrorType errorType;
    private final String errorCode;
    
    /**
     * 基础构造函数
     */
    public HugeGraphVectorStoreException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
    }
    
    /**
     * 带原因的构造函数
     */
    public HugeGraphVectorStoreException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
    }
    
    /**
     * 带错误码的构造函数
     */
    public HugeGraphVectorStoreException(ErrorType errorType, String errorCode, String message) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorCode;
    }
    
    /**
     * 完整的构造函数
     */
    public HugeGraphVectorStoreException(ErrorType errorType, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误类型
     */
    public ErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * 获取错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    // 静态工厂方法，提供更方便的异常创建方式
    
    /**
     * 创建连接错误异常
     */
    public static HugeGraphVectorStoreException connectionError(String message) {
        return new HugeGraphVectorStoreException(ErrorType.CONNECTION_ERROR, message);
    }
    
    /**
     * 创建连接错误异常（带原因）
     */
    public static HugeGraphVectorStoreException connectionError(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.CONNECTION_ERROR, message, cause);
    }
    
    /**
     * 创建配置错误异常
     */
    public static HugeGraphVectorStoreException configurationError(String message) {
        return new HugeGraphVectorStoreException(ErrorType.CONFIGURATION_ERROR, message);
    }
    
    /**
     * 创建配置错误异常（带原因）
     */
    public static HugeGraphVectorStoreException configurationError(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.CONFIGURATION_ERROR, message, cause);
    }
    
    /**
     * 创建向量搜索失败异常
     */
    public static HugeGraphVectorStoreException vectorSearchFailed(String message) {
        return new HugeGraphVectorStoreException(ErrorType.VECTOR_SEARCH_FAILED, message);
    }
    
    /**
     * 创建向量搜索失败异常（带原因）
     */
    public static HugeGraphVectorStoreException vectorSearchFailed(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.VECTOR_SEARCH_FAILED, message, cause);
    }
    
    /**
     * 创建向量插入失败异常
     */
    public static HugeGraphVectorStoreException vectorInsertFailed(String message) {
        return new HugeGraphVectorStoreException(ErrorType.VECTOR_INSERT_FAILED, message);
    }
    
    /**
     * 创建向量插入失败异常（带原因）
     */
    public static HugeGraphVectorStoreException vectorInsertFailed(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.VECTOR_INSERT_FAILED, message, cause);
    }
    
    /**
     * 创建向量维度错误异常
     */
    public static HugeGraphVectorStoreException vectorDimensionError(String message) {
        return new HugeGraphVectorStoreException(ErrorType.VECTOR_DIMENSION_ERROR, message);
    }
    
    /**
     * 创建图操作失败异常
     */
    public static HugeGraphVectorStoreException graphOperationFailed(String message) {
        return new HugeGraphVectorStoreException(ErrorType.GRAPH_OPERATION_FAILED, message);
    }
    
    /**
     * 创建图操作失败异常（带原因）
     */
    public static HugeGraphVectorStoreException graphOperationFailed(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.GRAPH_OPERATION_FAILED, message, cause);
    }
    
    /**
     * 创建索引操作失败异常
     */
    public static HugeGraphVectorStoreException indexOperationFailed(String message) {
        return new HugeGraphVectorStoreException(ErrorType.INDEX_OPERATION_FAILED, message);
    }
    
    /**
     * 创建索引操作失败异常（带原因）
     */
    public static HugeGraphVectorStoreException indexOperationFailed(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.INDEX_OPERATION_FAILED, message, cause);
    }
    
    /**
     * 创建序列化错误异常
     */
    public static HugeGraphVectorStoreException serializationError(String message) {
        return new HugeGraphVectorStoreException(ErrorType.SERIALIZATION_ERROR, message);
    }
    
    /**
     * 创建序列化错误异常（带原因）
     */
    public static HugeGraphVectorStoreException serializationError(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.SERIALIZATION_ERROR, message, cause);
    }
    
    /**
     * 创建超时错误异常
     */
    public static HugeGraphVectorStoreException timeoutError(String message) {
        return new HugeGraphVectorStoreException(ErrorType.TIMEOUT_ERROR, message);
    }
    
    /**
     * 创建权限错误异常
     */
    public static HugeGraphVectorStoreException permissionError(String message) {
        return new HugeGraphVectorStoreException(ErrorType.PERMISSION_ERROR, message);
    }
    
    /**
     * 创建资源不存在错误异常
     */
    public static HugeGraphVectorStoreException resourceNotFound(String message) {
        return new HugeGraphVectorStoreException(ErrorType.RESOURCE_NOT_FOUND, message);
    }
    
    /**
     * 创建未知错误异常
     */
    public static HugeGraphVectorStoreException unknownError(String message, Throwable cause) {
        return new HugeGraphVectorStoreException(ErrorType.UNKNOWN_ERROR, message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("HugeGraphVectorStoreException{errorType=%s, errorCode='%s', message='%s'}", 
                           errorType, errorCode, getMessage());
    }
}
