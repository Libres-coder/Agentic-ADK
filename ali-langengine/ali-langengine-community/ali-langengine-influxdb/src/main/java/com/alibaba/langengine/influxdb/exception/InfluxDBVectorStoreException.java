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
package com.alibaba.langengine.influxdb.exception;


public class InfluxDBVectorStoreException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码
     */
    private final String errorCode;

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public InfluxDBVectorStoreException(String message) {
        super(message);
        this.errorCode = "INFLUXDB_ERROR";
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   异常原因
     */
    public InfluxDBVectorStoreException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INFLUXDB_ERROR";
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误代码
     * @param message   异常消息
     */
    public InfluxDBVectorStoreException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误代码
     * @param message   异常消息
     * @param cause     异常原因
     */
    public InfluxDBVectorStoreException(String errorCode, String message, Throwable cause) {
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
     * 创建连接异常
     *
     * @param message 异常消息
     * @param cause   异常原因
     * @return 连接异常
     */
    public static InfluxDBVectorStoreException connectionError(String message, Throwable cause) {
        return new InfluxDBVectorStoreException("CONNECTION_ERROR", message, cause);
    }

    /**
     * 创建写入异常
     *
     * @param message 异常消息
     * @param cause   异常原因
     * @return 写入异常
     */
    public static InfluxDBVectorStoreException writeError(String message, Throwable cause) {
        return new InfluxDBVectorStoreException("WRITE_ERROR", message, cause);
    }

    /**
     * 创建查询异常
     *
     * @param message 异常消息
     * @param cause   异常原因
     * @return 查询异常
     */
    public static InfluxDBVectorStoreException queryError(String message, Throwable cause) {
        return new InfluxDBVectorStoreException("QUERY_ERROR", message, cause);
    }

    /**
     * 创建配置异常
     *
     * @param message 异常消息
     * @return 配置异常
     */
    public static InfluxDBVectorStoreException configurationError(String message) {
        return new InfluxDBVectorStoreException("CONFIGURATION_ERROR", message);
    }

    /**
     * 创建向量维度异常
     *
     * @param message 异常消息
     * @return 向量维度异常
     */
    public static InfluxDBVectorStoreException vectorDimensionError(String message) {
        return new InfluxDBVectorStoreException("VECTOR_DIMENSION_ERROR", message);
    }

    /**
     * 创建数据格式异常
     *
     * @param message 异常消息
     * @param cause   异常原因
     * @return 数据格式异常
     */
    public static InfluxDBVectorStoreException dataFormatError(String message, Throwable cause) {
        return new InfluxDBVectorStoreException("DATA_FORMAT_ERROR", message, cause);
    }
}
