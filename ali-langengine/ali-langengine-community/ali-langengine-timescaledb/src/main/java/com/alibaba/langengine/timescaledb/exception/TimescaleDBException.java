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
package com.alibaba.langengine.timescaledb.exception;


public class TimescaleDBException extends RuntimeException {
    
    /**
     * 异常类型枚举
     */
    public enum ErrorType {
        /**
         * 连接错误
         */
        CONNECTION_ERROR,
        
        /**
         * 配置错误
         */
        CONFIGURATION_ERROR,
        
        /**
         * 数据格式错误
         */
        DATA_FORMAT_ERROR,
        
        /**
         * 向量维度错误
         */
        VECTOR_DIMENSION_ERROR,
        
        /**
         * SQL执行错误
         */
        SQL_EXECUTION_ERROR,
        
        /**
         * 写入错误
         */
        WRITE_ERROR,
        
        /**
         * 查询错误
         */
        QUERY_ERROR,
        
        /**
         * 时序操作错误
         */
        TIMESERIES_ERROR,
        
        /**
         * 缓存错误
         */
        CACHE_ERROR,
        
        /**
         * 索引错误
         */
        INDEX_ERROR,
        
        /**
         * 事务错误
         */
        TRANSACTION_ERROR,
        
        /**
         * 资源不足错误
         */
        RESOURCE_ERROR,
        
        /**
         * 超时错误
         */
        TIMEOUT_ERROR,
        
        /**
         * 验证错误
         */
        VALIDATION_ERROR,
        
        /**
         * 未知错误
         */
        UNKNOWN_ERROR
    }
    
    private final ErrorType errorType;
    private final String errorCode;
    
    public TimescaleDBException(String message) {
        super(message);
        this.errorType = ErrorType.UNKNOWN_ERROR;
        this.errorCode = "UNKNOWN";
    }
    
    public TimescaleDBException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.UNKNOWN_ERROR;
        this.errorCode = "UNKNOWN";
    }
    
    public TimescaleDBException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorType.name();
    }
    
    public TimescaleDBException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorType.name();
    }
    
    public TimescaleDBException(ErrorType errorType, String errorCode, String message) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorCode;
    }
    
    public TimescaleDBException(ErrorType errorType, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorCode;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    // ========== 静态工厂方法 ==========
    
    /**
     * 连接错误
     */
    public static TimescaleDBException connectionError(String message) {
        return new TimescaleDBException(ErrorType.CONNECTION_ERROR, "CONN_001", message);
    }
    
    public static TimescaleDBException connectionError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.CONNECTION_ERROR, "CONN_001", message, cause);
    }
    
    /**
     * 配置错误
     */
    public static TimescaleDBException configurationError(String message) {
        return new TimescaleDBException(ErrorType.CONFIGURATION_ERROR, "CONFIG_001", message);
    }
    
    public static TimescaleDBException configurationError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.CONFIGURATION_ERROR, "CONFIG_001", message, cause);
    }
    
    /**
     * 数据格式错误
     */
    public static TimescaleDBException dataFormatError(String message) {
        return new TimescaleDBException(ErrorType.DATA_FORMAT_ERROR, "DATA_001", message);
    }
    
    public static TimescaleDBException dataFormatError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.DATA_FORMAT_ERROR, "DATA_001", message, cause);
    }
    
    /**
     * 向量维度错误
     */
    public static TimescaleDBException vectorDimensionError(String message) {
        return new TimescaleDBException(ErrorType.VECTOR_DIMENSION_ERROR, "VECTOR_001", message);
    }
    
    public static TimescaleDBException vectorDimensionError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.VECTOR_DIMENSION_ERROR, "VECTOR_001", message, cause);
    }
    
    /**
     * SQL执行错误
     */
    public static TimescaleDBException sqlExecutionError(String message) {
        return new TimescaleDBException(ErrorType.SQL_EXECUTION_ERROR, "SQL_001", message);
    }
    
    public static TimescaleDBException sqlExecutionError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.SQL_EXECUTION_ERROR, "SQL_001", message, cause);
    }
    
    /**
     * 写入错误
     */
    public static TimescaleDBException writeError(String message) {
        return new TimescaleDBException(ErrorType.WRITE_ERROR, "WRITE_001", message);
    }
    
    public static TimescaleDBException writeError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.WRITE_ERROR, "WRITE_001", message, cause);
    }
    
    /**
     * 查询错误
     */
    public static TimescaleDBException queryError(String message) {
        return new TimescaleDBException(ErrorType.QUERY_ERROR, "QUERY_001", message);
    }
    
    public static TimescaleDBException queryError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.QUERY_ERROR, "QUERY_001", message, cause);
    }
    
    /**
     * 时序操作错误
     */
    public static TimescaleDBException timeSeriesError(String message) {
        return new TimescaleDBException(ErrorType.TIMESERIES_ERROR, "TIMESERIES_001", message);
    }
    
    public static TimescaleDBException timeSeriesError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.TIMESERIES_ERROR, "TIMESERIES_001", message, cause);
    }
    
    /**
     * 缓存错误
     */
    public static TimescaleDBException cacheError(String message) {
        return new TimescaleDBException(ErrorType.CACHE_ERROR, "CACHE_001", message);
    }
    
    public static TimescaleDBException cacheError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.CACHE_ERROR, "CACHE_001", message, cause);
    }
    
    /**
     * 索引错误
     */
    public static TimescaleDBException indexError(String message) {
        return new TimescaleDBException(ErrorType.INDEX_ERROR, "INDEX_001", message);
    }
    
    public static TimescaleDBException indexError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.INDEX_ERROR, "INDEX_001", message, cause);
    }
    
    /**
     * 验证错误
     */
    public static TimescaleDBException validationError(String message) {
        return new TimescaleDBException(ErrorType.VALIDATION_ERROR, "VALIDATION_001", message);
    }
    
    public static TimescaleDBException validationError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.VALIDATION_ERROR, "VALIDATION_001", message, cause);
    }
    
    /**
     * 超时错误
     */
    public static TimescaleDBException timeoutError(String message) {
        return new TimescaleDBException(ErrorType.TIMEOUT_ERROR, "TIMEOUT_001", message);
    }
    
    public static TimescaleDBException timeoutError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.TIMEOUT_ERROR, "TIMEOUT_001", message, cause);
    }
    
    /**
     * 资源错误
     */
    public static TimescaleDBException resourceError(String message) {
        return new TimescaleDBException(ErrorType.RESOURCE_ERROR, "RESOURCE_001", message);
    }
    
    public static TimescaleDBException resourceError(String message, Throwable cause) {
        return new TimescaleDBException(ErrorType.RESOURCE_ERROR, "RESOURCE_001", message, cause);
    }
    
    @Override
    public String toString() {
        return String.format("TimescaleDBException[%s:%s] %s", 
                errorType.name(), errorCode, getMessage());
    }
}
