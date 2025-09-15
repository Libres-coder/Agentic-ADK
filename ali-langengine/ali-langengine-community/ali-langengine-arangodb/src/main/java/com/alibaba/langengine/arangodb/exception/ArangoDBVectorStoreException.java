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
package com.alibaba.langengine.arangodb.exception;


public class ArangoDBVectorStoreException extends RuntimeException {

    private final ErrorCode errorCode;
    
    public enum ErrorCode {
        CONNECTION_ERROR("CONNECTION_ERROR", "连接错误"),
        CONFIGURATION_ERROR("CONFIGURATION_ERROR", "配置错误"),
        DATABASE_ERROR("DATABASE_ERROR", "数据库错误"),
        COLLECTION_ERROR("COLLECTION_ERROR", "集合错误"),
        INDEX_ERROR("INDEX_ERROR", "索引错误"),
        VIEW_ERROR("VIEW_ERROR", "视图错误"),
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
        ARANGOSEARCH_ERROR("ARANGOSEARCH_ERROR", "ArangoSearch 错误"),
        FULLTEXT_SEARCH_ERROR("FULLTEXT_SEARCH_ERROR", "全文搜索错误"),
        HYBRID_SEARCH_ERROR("HYBRID_SEARCH_ERROR", "混合搜索错误"),
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
    
    public ArangoDBVectorStoreException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ArangoDBVectorStoreException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    // 静态工厂方法
    public static ArangoDBVectorStoreException connectionError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.CONNECTION_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException configurationError(String message) {
        return new ArangoDBVectorStoreException(ErrorCode.CONFIGURATION_ERROR, message);
    }
    
    public static ArangoDBVectorStoreException databaseError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.DATABASE_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException collectionError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.COLLECTION_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException indexError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.INDEX_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException queryError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.QUERY_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException insertError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.INSERT_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException updateError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.UPDATE_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException deleteError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.DELETE_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException vectorDimensionError(String message) {
        return new ArangoDBVectorStoreException(ErrorCode.VECTOR_DIMENSION_ERROR, message);
    }
    
    public static ArangoDBVectorStoreException dataFormatError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.DATA_FORMAT_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException cacheError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.CACHE_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException timeoutError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.TIMEOUT_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException validationError(String message) {
        return new ArangoDBVectorStoreException(ErrorCode.VALIDATION_ERROR, message);
    }
    
    public static ArangoDBVectorStoreException viewError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.VIEW_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException authenticationError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.AUTHENTICATION_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException authorizationError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.AUTHORIZATION_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException networkError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.NETWORK_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException serializationError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.SERIALIZATION_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException deserializationError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.DESERIALIZATION_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException arangoSearchError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.ARANGOSEARCH_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException fullTextSearchError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.FULLTEXT_SEARCH_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException hybridSearchError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.HYBRID_SEARCH_ERROR, message, cause);
    }
    
    public static ArangoDBVectorStoreException unknownError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.UNKNOWN_ERROR, message, cause);
    }
    
    /**
     * 创建带上下文的异常
     */
    public static ArangoDBVectorStoreException withContext(ErrorCode errorCode, String operation, 
                                                          String context, Throwable cause) {
        String message = String.format("操作失败: %s, 上下文: %s", operation, context);
        return new ArangoDBVectorStoreException(errorCode, message, cause);
    }
    
    /**
     * 创建带性能信息的异常
     */
    public static ArangoDBVectorStoreException withPerformance(ErrorCode errorCode, String operation, 
                                                              long executionTime, Throwable cause) {
        String message = String.format("操作失败: %s, 执行时间: %dms", operation, executionTime);
        return new ArangoDBVectorStoreException(errorCode, message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("ArangoDBVectorStoreException{errorCode=%s, message=%s, cause=%s}", 
                           errorCode.getCode(), getMessage(), getCause());
    }
}
