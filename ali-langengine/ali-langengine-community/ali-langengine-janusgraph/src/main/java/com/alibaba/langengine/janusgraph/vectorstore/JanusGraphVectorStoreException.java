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
package com.alibaba.langengine.janusgraph.vectorstore;


public class JanusGraphVectorStoreException extends RuntimeException {

    /**
     * 异常类型枚举
     */
    public enum ErrorType {
        CONNECTION_FAILED("数据库连接失败"),
        SCHEMA_INITIALIZATION_FAILED("Schema初始化失败"),
        INDEX_CREATION_FAILED("索引创建失败"),
        DOCUMENT_ADD_FAILED("文档添加失败"),
        DOCUMENT_DELETE_FAILED("文档删除失败"),
        VECTOR_SEARCH_FAILED("向量搜索失败"),
        CONFIGURATION_ERROR("配置错误"),
        EMBEDDING_GENERATION_FAILED("向量生成失败"),
        TRANSACTION_FAILED("事务操作失败"),
        RESOURCE_CLEANUP_FAILED("资源清理失败");

        private final String description;

        ErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorType errorType;
    private final String operation;

    /**
     * 构造函数
     * 
     * @param errorType 错误类型
     * @param message   错误消息
     */
    public JanusGraphVectorStoreException(ErrorType errorType, String message) {
        super(String.format("[%s] %s: %s", errorType.name(), errorType.getDescription(), message));
        this.errorType = errorType;
        this.operation = null;
    }

    /**
     * 构造函数
     * 
     * @param errorType 错误类型
     * @param operation 操作名称
     * @param message   错误消息
     */
    public JanusGraphVectorStoreException(ErrorType errorType, String operation, String message) {
        super(String.format("[%s] %s in operation '%s': %s", 
            errorType.name(), errorType.getDescription(), operation, message));
        this.errorType = errorType;
        this.operation = operation;
    }

    /**
     * 构造函数
     * 
     * @param errorType 错误类型
     * @param message   错误消息
     * @param cause     原因异常
     */
    public JanusGraphVectorStoreException(ErrorType errorType, String message, Throwable cause) {
        super(String.format("[%s] %s: %s", errorType.name(), errorType.getDescription(), message), cause);
        this.errorType = errorType;
        this.operation = null;
    }

    /**
     * 构造函数
     * 
     * @param errorType 错误类型
     * @param operation 操作名称
     * @param message   错误消息
     * @param cause     原因异常
     */
    public JanusGraphVectorStoreException(ErrorType errorType, String operation, String message, Throwable cause) {
        super(String.format("[%s] %s in operation '%s': %s", 
            errorType.name(), errorType.getDescription(), operation, message), cause);
        this.errorType = errorType;
        this.operation = operation;
    }

    /**
     * 获取错误类型
     * 
     * @return 错误类型
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * 获取操作名称
     * 
     * @return 操作名称
     */
    public String getOperation() {
        return operation;
    }

    /**
     * 检查是否是连接相关错误
     * 
     * @return 是否是连接错误
     */
    public boolean isConnectionError() {
        return errorType == ErrorType.CONNECTION_FAILED;
    }

    /**
     * 检查是否是配置相关错误
     * 
     * @return 是否是配置错误
     */
    public boolean isConfigurationError() {
        return errorType == ErrorType.CONFIGURATION_ERROR;
    }

    /**
     * 检查是否是事务相关错误
     * 
     * @return 是否是事务错误
     */
    public boolean isTransactionError() {
        return errorType == ErrorType.TRANSACTION_FAILED;
    }

    /**
     * 创建连接失败异常
     * 
     * @param message 错误消息
     * @param cause   原因异常
     * @return JanusGraphVectorStoreException
     */
    public static JanusGraphVectorStoreException connectionFailed(String message, Throwable cause) {
        return new JanusGraphVectorStoreException(ErrorType.CONNECTION_FAILED, message, cause);
    }

    /**
     * 创建配置错误异常
     * 
     * @param message 错误消息
     * @return JanusGraphVectorStoreException
     */
    public static JanusGraphVectorStoreException configurationError(String message) {
        return new JanusGraphVectorStoreException(ErrorType.CONFIGURATION_ERROR, message);
    }

    /**
     * 创建文档操作失败异常
     * 
     * @param operation 操作类型
     * @param message   错误消息
     * @param cause     原因异常
     * @return JanusGraphVectorStoreException
     */
    public static JanusGraphVectorStoreException documentOperationFailed(String operation, String message, Throwable cause) {
        ErrorType errorType = operation.toLowerCase().contains("add") ? 
            ErrorType.DOCUMENT_ADD_FAILED : ErrorType.DOCUMENT_DELETE_FAILED;
        return new JanusGraphVectorStoreException(errorType, operation, message, cause);
    }

    /**
     * 创建向量搜索失败异常
     * 
     * @param message 错误消息
     * @param cause   原因异常
     * @return JanusGraphVectorStoreException
     */
    public static JanusGraphVectorStoreException vectorSearchFailed(String message, Throwable cause) {
        return new JanusGraphVectorStoreException(ErrorType.VECTOR_SEARCH_FAILED, message, cause);
    }

    /**
     * 创建事务失败异常
     * 
     * @param operation 操作名称
     * @param message   错误消息
     * @param cause     原因异常
     * @return JanusGraphVectorStoreException
     */
    public static JanusGraphVectorStoreException transactionFailed(String operation, String message, Throwable cause) {
        return new JanusGraphVectorStoreException(ErrorType.TRANSACTION_FAILED, operation, message, cause);
    }
}
