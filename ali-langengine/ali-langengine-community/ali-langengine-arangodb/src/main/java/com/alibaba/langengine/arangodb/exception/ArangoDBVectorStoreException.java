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
        QUERY_ERROR("QUERY_ERROR", "查询错误"),
        INSERT_ERROR("INSERT_ERROR", "插入错误"),
        UPDATE_ERROR("UPDATE_ERROR", "更新错误"),
        DELETE_ERROR("DELETE_ERROR", "删除错误"),
        VECTOR_DIMENSION_ERROR("VECTOR_DIMENSION_ERROR", "向量维度错误"),
        DATA_FORMAT_ERROR("DATA_FORMAT_ERROR", "数据格式错误"),
        CACHE_ERROR("CACHE_ERROR", "缓存错误"),
        TIMEOUT_ERROR("TIMEOUT_ERROR", "超时错误"),
        VALIDATION_ERROR("VALIDATION_ERROR", "验证错误"),
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
    
    public static ArangoDBVectorStoreException unknownError(String message, Throwable cause) {
        return new ArangoDBVectorStoreException(ErrorCode.UNKNOWN_ERROR, message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("ArangoDBVectorStoreException{errorCode=%s, message=%s}", 
                           errorCode.getCode(), getMessage());
    }
}
