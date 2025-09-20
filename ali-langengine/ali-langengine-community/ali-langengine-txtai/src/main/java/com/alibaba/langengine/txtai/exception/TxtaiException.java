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
package com.alibaba.langengine.txtai.exception;

/**
 * Txtai向量数据库操作专用异常类
 *
 * <p>提供结构化的异常处理机制，支持多种错误类型分类和详细的错误代码标识。
 * 通过静态工厂方法创建不同类型的异常实例，便于异常处理和问题定位。
 *
 * <p>支持的错误类型：
 * <ul>
 *   <li>配置错误 - 服务器URL、认证信息等配置问题</li>
 *   <li>验证错误 - 输入参数、索引名称等验证失败</li>
 *   <li>网络错误 - 连接超时、网络不可达等网络问题</li>
 *   <li>API错误 - HTTP状态码异常、API响应错误</li>
 *   <li>处理错误 - 数据序列化、业务逻辑处理等错误</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 创建配置错误
 * throw TxtaiException.configurationError("服务器URL未配置");
 *
 * // 创建验证错误
 * throw TxtaiException.validationError("索引名称不能为空");
 *
 * // 创建网络错误
 * throw TxtaiException.networkError("连接超时", cause);
 *
 * // 创建API错误
 * throw TxtaiException.apiError(404, "资源未找到");
 * }</pre>
 *
 * @author xiaoxuan.lp
 * @since 1.0.0
 */
public class TxtaiException extends RuntimeException {

    /**
     * 错误代码标识
     *
     * <p>用于唯一标识异常类型的错误代码，格式为 TXTAI_ERROR_TYPE 或 TXTAI_ERROR_TYPE_CODE。
     * 便于日志记录、监控告警和问题排查。
     *
     * @see #getErrorCode()
     */
    private final String errorCode;

    /**
     * 错误类型枚举
     *
     * <p>将异常按照错误原因分类，便于统一的异常处理和错误恢复策略。
     * 每种错误类型对应不同的处理逻辑和用户提示。
     *
     * @see ErrorType
     * @see #getErrorType()
     */
    private final ErrorType errorType;

    /**
     * Txtai异常错误类型枚举
     *
     * <p>定义了Txtai操作中可能出现的所有错误类型，用于异常分类和处理策略选择。
     *
     * @since 1.0.0
     */
    public enum ErrorType {
        /**
         * 配置错误
         *
         * <p>包括但不限于：
         * <ul>
         *   <li>服务器URL未配置或格式错误</li>
         *   <li>认证信息缺失或无效</li>
         *   <li>连接参数配置错误</li>
         *   <li>Embedding对象未正确初始化</li>
         * </ul>
         */
        CONFIGURATION_ERROR,

        /**
         * 参数验证错误
         *
         * <p>包括但不限于：
         * <ul>
         *   <li>输入参数为空或格式不正确</li>
         *   <li>索引名称不符合规范</li>
         *   <li>查询文本为空或过长</li>
         *   <li>返回结果数量超出限制</li>
         * </ul>
         */
        VALIDATION_ERROR,

        /**
         * 网络连接错误
         *
         * <p>包括但不限于：
         * <ul>
         *   <li>连接超时或网络不可达</li>
         *   <li>DNS解析失败</li>
         *   <li>SSL/TLS握手失败</li>
         *   <li>网络中断或连接重置</li>
         * </ul>
         */
        NETWORK_ERROR,

        /**
         * API调用错误
         *
         * <p>包括但不限于：
         * <ul>
         *   <li>HTTP状态码异常（4xx、5xx）</li>
         *   <li>API响应格式错误</li>
         *   <li>认证或授权失败</li>
         *   <li>API限流或配额耗尽</li>
         * </ul>
         */
        API_ERROR,

        /**
         * 处理错误
         *
         * <p>包括但不限于：
         * <ul>
         *   <li>JSON序列化/反序列化失败</li>
         *   <li>数据格式转换错误</li>
         *   <li>业务逻辑处理异常</li>
         *   <li>其他未分类的处理错误</li>
         * </ul>
         */
        PROCESSING_ERROR
    }

    /**
     * 构造一个简单的Txtai异常
     *
     * <p>使用默认的错误代码和处理错误类型创建异常实例。
     * 主要用于快速创建异常，不推荐直接使用，建议使用静态工厂方法。
     *
     * @param message 异常消息
     * @see #configurationError(String)
     * @see #validationError(String)
     * @see #processingError(String, Throwable)
     */
    public TxtaiException(String message) {
        super(message);
        this.errorCode = "TXTAI_UNKNOWN";
        this.errorType = ErrorType.PROCESSING_ERROR;
    }

    /**
     * 构造一个带原因的Txtai异常
     *
     * <p>使用默认的错误代码和处理错误类型创建异常实例，同时保留原始异常信息。
     * 主要用于包装其他异常，不推荐直接使用，建议使用静态工厂方法。
     *
     * @param message 异常消息
     * @param cause   原始异常原因
     * @see #networkError(String, Throwable)
     * @see #processingError(String, Throwable)
     */
    public TxtaiException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TXTAI_UNKNOWN";
        this.errorType = ErrorType.PROCESSING_ERROR;
    }

    /**
     * 构造一个完整的Txtai异常
     *
     * <p>使用指定的错误代码、消息和错误类型创建异常实例。
     * 这是最完整的构造函数，通常由静态工厂方法内部调用。
     *
     * @param errorCode 错误代码，用于唯一标识异常类型
     * @param message   异常消息，描述具体的错误情况
     * @param errorType 错误类型，用于异常分类和处理
     * @see ErrorType
     */
    public TxtaiException(String errorCode, String message, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    /**
     * 构造一个完整的Txtai异常（带原始异常）
     *
     * <p>使用指定的错误代码、消息、错误类型和原始异常创建异常实例。
     * 这是最完整的构造函数，通常由静态工厂方法内部调用，用于包装其他异常。
     *
     * @param errorCode 错误代码，用于唯一标识异常类型
     * @param message   异常消息，描述具体的错误情况
     * @param errorType 错误类型，用于异常分类和处理
     * @param cause     原始异常原因，用于保留完整的异常链
     * @see ErrorType
     */
    public TxtaiException(String errorCode, String message, ErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    /**
     * 获取异常的错误代码
     *
     * <p>错误代码用于唯一标识异常类型，便于日志记录、监控告警和问题排查。
     * 错误代码格式为 TXTAI_ERROR_TYPE 或 TXTAI_ERROR_TYPE_CODE。
     *
     * @return 错误代码字符串
     * @see ErrorType
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取异常的错误类型
     *
     * <p>错误类型用于将异常按照错误原因分类，便于统一的异常处理和错误恢复策略。
     * 可以根据错误类型选择不同的处理逻辑和用户提示。
     *
     * @return 错误类型枚举值
     * @see ErrorType
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建配置错误异常
     *
     * <p>用于表示配置相关的错误，如服务器URL未配置、认证信息缺失等。
     * 这类错误通常需要用户检查配置文件或环境变量设置。
     *
     * <p>错误代码：TXTAI_CONFIG_ERROR
     *
     * @param message 详细的错误描述信息
     * @return 配置错误异常实例
     * @see ErrorType#CONFIGURATION_ERROR
     */
    public static TxtaiException configurationError(String message) {
        return new TxtaiException("TXTAI_CONFIG_ERROR", message, ErrorType.CONFIGURATION_ERROR);
    }

    /**
     * 创建参数验证错误异常
     *
     * <p>用于表示输入参数验证失败的错误，如参数为空、格式不正确、超出范围等。
     * 这类错误通常需要用户检查输入参数的有效性。
     *
     * <p>错误代码：TXTAI_VALIDATION_ERROR
     *
     * @param message 详细的错误描述信息
     * @return 验证错误异常实例
     * @see ErrorType#VALIDATION_ERROR
     */
    public static TxtaiException validationError(String message) {
        return new TxtaiException("TXTAI_VALIDATION_ERROR", message, ErrorType.VALIDATION_ERROR);
    }

    /**
     * 创建网络连接错误异常
     *
     * <p>用于表示网络连接相关的错误，如连接超时、网络不可达、SSL握手失败等。
     * 这类错误通常需要检查网络连接状态或服务器可用性。
     *
     * <p>错误代码：TXTAI_NETWORK_ERROR
     *
     * @param message 详细的错误描述信息
     * @param cause   原始异常原因，保留完整的异常链
     * @return 网络错误异常实例
     * @see ErrorType#NETWORK_ERROR
     */
    public static TxtaiException networkError(String message, Throwable cause) {
        return new TxtaiException("TXTAI_NETWORK_ERROR", message, ErrorType.NETWORK_ERROR, cause);
    }

    /**
     * 创建API调用错误异常
     *
     * <p>用于表示API调用过程中的一般错误，如认证失败、权限不足等。
     * 这类错误通常需要检查API调用的参数和权限设置。
     *
     * <p>错误代码：TXTAI_API_ERROR
     *
     * @param message 详细的错误描述信息
     * @return API错误异常实例
     * @see ErrorType#API_ERROR
     * @see #apiError(int, String)
     */
    public static TxtaiException apiError(String message) {
        return new TxtaiException("TXTAI_API_ERROR", message, ErrorType.API_ERROR);
    }

    /**
     * 创建带HTTP状态码的API调用错误异常
     *
     * <p>用于表示HTTP API调用返回异常状态码的错误，包含具体的状态码信息。
     * 错误消息会自动包含HTTP状态码前缀，便于快速定位问题。
     *
     * <p>错误代码：TXTAI_API_ERROR_[statusCode]
     *
     * @param statusCode HTTP状态码（如400、404、500等）
     * @param message    详细的错误描述信息
     * @return API错误异常实例，消息格式为 "HTTP [statusCode]: [message]"
     * @see ErrorType#API_ERROR
     * @see #apiError(String)
     */
    public static TxtaiException apiError(int statusCode, String message) {
        return new TxtaiException("TXTAI_API_ERROR_" + statusCode,
                                "HTTP " + statusCode + ": " + message,
                                ErrorType.API_ERROR);
    }

    /**
     * 创建处理错误异常
     *
     * <p>用于表示数据处理过程中的错误，如JSON序列化失败、数据格式转换错误等。
     * 这类错误通常需要检查数据格式或处理逻辑。
     *
     * <p>错误代码：TXTAI_PROCESSING_ERROR
     *
     * @param message 详细的错误描述信息
     * @param cause   原始异常原因，保留完整的异常链
     * @return 处理错误异常实例
     * @see ErrorType#PROCESSING_ERROR
     */
    public static TxtaiException processingError(String message, Throwable cause) {
        return new TxtaiException("TXTAI_PROCESSING_ERROR", message, ErrorType.PROCESSING_ERROR, cause);
    }

    /**
     * 返回异常的字符串表示形式
     *
     * <p>格式化输出异常的关键信息，包括错误代码、错误类型和异常消息，
     * 便于日志记录和调试分析。
     *
     * <p>输出格式：TxtaiException{errorCode='错误代码', errorType=错误类型, message='异常消息'}
     *
     * @return 格式化的异常字符串表示
     */
    @Override
    public String toString() {
        return String.format("TxtaiException{errorCode='%s', errorType=%s, message='%s'}",
                           errorCode, errorType, getMessage());
    }
}