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
package com.alibaba.langengine.nebulagraph.exception;


public class NebulaGraphVectorStoreException extends RuntimeException {

    private final ErrorCode errorCode;
    
    public enum ErrorCode {
        CONNECTION_ERROR("CONNECTION_ERROR", "连接错误"),
        CONFIGURATION_ERROR("CONFIGURATION_ERROR", "配置错误"),
        SPACE_ERROR("SPACE_ERROR", "图空间错误"),
        TAG_ERROR("TAG_ERROR", "标签错误"),
        INDEX_ERROR("INDEX_ERROR", "索引错误"),
        QUERY_ERROR("QUERY_ERROR", "查询错误"),
        INSERT_ERROR("INSERT_ERROR", "插入错误"),
        UPDATE_ERROR("UPDATE_ERROR", "更新错误"),
        DELETE_ERROR("DELETE_ERROR", "删除错误"),
        VECTOR_DIMENSION_ERROR("VECTOR_DIMENSION_ERROR", "向量维度错误"),
        DATA_FORMAT_ERROR("DATA_FORMAT_ERROR", "数据格式错误"),
        CACHE_ERROR("CACHE_ERROR", "缓存错误"),
        TIMEOUT_ERROR("TIMEOUT_ERROR", "超时错误"),
        VALIDATION_ERROR("VALIDATION_ERROR", "验证错误"),
        AUTHENTICATION_ERROR("AUTHENTICATION_ERROR", "认证错误"),
        AUTHORIZATION_ERROR("AUTHORIZATION_ERROR", "授权错误"),
        NETWORK_ERROR("NETWORK_ERROR", "网络错误"),
        SERIALIZATION_ERROR("SERIALIZATION_ERROR", "序列化错误"),
        DESERIALIZATION_ERROR("DESERIALIZATION_ERROR", "反序列化错误"),
        VECTOR_SEARCH_ERROR("VECTOR_SEARCH_ERROR", "向量搜索错误"),
        GRAPH_SCHEMA_ERROR("GRAPH_SCHEMA_ERROR", "图模式错误"),
        SESSION_ERROR("SESSION_ERROR", "会话错误"),
        UNKNOWN_ERROR("UNKNOWN_ERROR", "未知错误");
        
        private final String code;
        private final String description;
        
        ErrorCode(String code, String description) {
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
    
    public NebulaGraphVectorStoreException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public NebulaGraphVectorStoreException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    // 静态工厂方法
    public static NebulaGraphVectorStoreException connectionError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.CONNECTION_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException configurationError(String message) {
        return new NebulaGraphVectorStoreException(ErrorCode.CONFIGURATION_ERROR, message);
    }

    public static NebulaGraphVectorStoreException initError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.CONFIGURATION_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException spaceError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.SPACE_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException tagError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.TAG_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException indexError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.INDEX_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException queryError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.QUERY_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException insertError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.INSERT_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException updateError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.UPDATE_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException deleteError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.DELETE_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException vectorDimensionError(String message) {
        return new NebulaGraphVectorStoreException(ErrorCode.VECTOR_DIMENSION_ERROR, message);
    }
    
    public static NebulaGraphVectorStoreException dataFormatError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.DATA_FORMAT_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException cacheError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.CACHE_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException timeoutError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.TIMEOUT_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException validationError(String message) {
        return new NebulaGraphVectorStoreException(ErrorCode.VALIDATION_ERROR, message);
    }
    
    public static NebulaGraphVectorStoreException authenticationError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.AUTHENTICATION_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException authorizationError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.AUTHORIZATION_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException networkError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.NETWORK_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException serializationError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.SERIALIZATION_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException deserializationError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.DESERIALIZATION_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException vectorSearchError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.VECTOR_SEARCH_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException graphSchemaError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.GRAPH_SCHEMA_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException sessionError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.SESSION_ERROR, message, cause);
    }
    
    public static NebulaGraphVectorStoreException unknownError(String message, Throwable cause) {
        return new NebulaGraphVectorStoreException(ErrorCode.UNKNOWN_ERROR, message, cause);
    }
    
    /**
     * 创建带上下文的异常
     */
    public static NebulaGraphVectorStoreException withContext(ErrorCode errorCode, String operation, 
                                                            String context, Throwable cause) {
        String message = String.format("操作失败: %s, 上下文: %s", operation, context);
        return new NebulaGraphVectorStoreException(errorCode, message, cause);
    }
    
    /**
     * 创建带性能信息的异常
     */
    public static NebulaGraphVectorStoreException withPerformance(ErrorCode errorCode, String operation, 
                                                                long executionTime, Throwable cause) {
        String message = String.format("操作失败: %s, 执行时间: %dms", operation, executionTime);
        return new NebulaGraphVectorStoreException(errorCode, message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("NebulaGraphVectorStoreException{errorCode=%s, message=%s, cause=%s}", 
                           errorCode.getCode(), getMessage(), getCause());
    }
}
