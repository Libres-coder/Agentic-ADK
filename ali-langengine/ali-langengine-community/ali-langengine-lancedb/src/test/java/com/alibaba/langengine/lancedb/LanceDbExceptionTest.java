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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LanceDB异常测试")
class LanceDbExceptionTest {

    @Test
    @DisplayName("测试基础异常构造函数")
    void testBasicExceptionConstructors() {
        // 测试只有消息的构造函数
        String message = "Test error message";
        LanceDbException exception = new LanceDbException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getHttpStatusCode());
        assertFalse(exception.hasErrorCode());
        assertFalse(exception.hasHttpStatusCode());
        
        // 测试消息和原因的构造函数
        RuntimeException cause = new RuntimeException("Cause exception");
        LanceDbException exceptionWithCause = new LanceDbException(message, cause);
        
        assertEquals(message, exceptionWithCause.getMessage());
        assertEquals(cause, exceptionWithCause.getCause());
        assertNull(exceptionWithCause.getErrorCode());
        assertNull(exceptionWithCause.getHttpStatusCode());
    }

    @Test
    @DisplayName("测试带错误代码的异常构造函数")
    void testExceptionWithErrorCode() {
        String message = "Test error message";
        String errorCode = "TEST_ERROR_001";
        
        LanceDbException exception = new LanceDbException(message, errorCode);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getHttpStatusCode());
        assertTrue(exception.hasErrorCode());
        assertFalse(exception.hasHttpStatusCode());
    }

    @Test
    @DisplayName("测试带HTTP状态码的异常构造函数")
    void testExceptionWithHttpStatusCode() {
        String message = "HTTP error occurred";
        String errorCode = "HTTP_ERROR";
        Integer httpStatusCode = 500;
        
        LanceDbException exception = new LanceDbException(message, errorCode, httpStatusCode);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatusCode, exception.getHttpStatusCode());
        assertTrue(exception.hasErrorCode());
        assertTrue(exception.hasHttpStatusCode());
    }

    @Test
    @DisplayName("测试完整异常构造函数")
    void testFullExceptionConstructor() {
        String message = "Complete error";
        RuntimeException cause = new RuntimeException("Root cause");
        String errorCode = "COMPLETE_ERROR";
        Integer httpStatusCode = 400;
        
        LanceDbException exception = new LanceDbException(message, cause, errorCode, httpStatusCode);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(httpStatusCode, exception.getHttpStatusCode());
        assertTrue(exception.hasErrorCode());
        assertTrue(exception.hasHttpStatusCode());
    }

    @Test
    @DisplayName("测试异常toString方法")
    void testExceptionToString() {
        String message = "Test message";
        
        // 基础异常
        LanceDbException basicException = new LanceDbException(message);
        String basicString = basicException.toString();
        assertTrue(basicString.contains(message));
        assertFalse(basicString.contains("ErrorCode"));
        assertFalse(basicString.contains("HTTP"));
        
        // 带错误代码的异常
        String errorCode = "TEST_CODE";
        LanceDbException codeException = new LanceDbException(message, errorCode);
        String codeString = codeException.toString();
        assertTrue(codeString.contains(message));
        assertTrue(codeString.contains("ErrorCode: " + errorCode));
        
        // 带HTTP状态码的异常
        Integer httpStatus = 404;
        LanceDbException httpException = new LanceDbException(message, errorCode, httpStatus);
        String httpString = httpException.toString();
        assertTrue(httpString.contains(message));
        assertTrue(httpString.contains("ErrorCode: " + errorCode));
        assertTrue(httpString.contains("HTTP: " + httpStatus));
    }

    @Test
    @DisplayName("测试客户端异常")
    void testClientException() {
        String message = "Client error";
        LanceDbClientException clientException = new LanceDbClientException(message);
        
        assertEquals(message, clientException.getMessage());
        assertTrue(clientException instanceof LanceDbException);
        
        // 测试带原因的客户端异常
        RuntimeException cause = new RuntimeException("Client cause");
        LanceDbClientException clientExceptionWithCause = new LanceDbClientException(message, cause);
        
        assertEquals(message, clientExceptionWithCause.getMessage());
        assertEquals(cause, clientExceptionWithCause.getCause());
        
        // 测试带错误代码和HTTP状态码的客户端异常
        String errorCode = "CLIENT_ERROR";
        Integer httpStatus = 400;
        LanceDbClientException fullClientException = new LanceDbClientException(
                message, cause, errorCode, httpStatus);
        
        assertEquals(message, fullClientException.getMessage());
        assertEquals(cause, fullClientException.getCause());
        assertEquals(errorCode, fullClientException.getErrorCode());
        assertEquals(httpStatus, fullClientException.getHttpStatusCode());
    }

    @Test
    @DisplayName("测试连接异常")
    void testConnectionException() {
        String message = "Connection failed";
        LanceDbConnectionException connectionException = new LanceDbConnectionException(message);
        
        assertEquals(message, connectionException.getMessage());
        assertEquals("CONNECTION_ERROR", connectionException.getErrorCode());
        assertTrue(connectionException.hasErrorCode());
        assertTrue(connectionException instanceof LanceDbException);
        
        // 测试带原因的连接异常
        RuntimeException cause = new RuntimeException("Network error");
        LanceDbConnectionException connectionExceptionWithCause = 
                new LanceDbConnectionException(message, cause);
        
        assertEquals(message, connectionExceptionWithCause.getMessage());
        assertEquals(cause, connectionExceptionWithCause.getCause());
        assertEquals("CONNECTION_ERROR", connectionExceptionWithCause.getErrorCode());
    }

    @Test
    @DisplayName("测试配置异常")
    void testConfigurationException() {
        String message = "Invalid configuration";
        LanceDbConfigurationException configException = new LanceDbConfigurationException(message);
        
        assertEquals(message, configException.getMessage());
        assertEquals("CONFIGURATION_ERROR", configException.getErrorCode());
        assertTrue(configException.hasErrorCode());
        assertTrue(configException instanceof LanceDbException);
        
        // 测试带原因的配置异常
        IllegalArgumentException cause = new IllegalArgumentException("Invalid parameter");
        LanceDbConfigurationException configExceptionWithCause = 
                new LanceDbConfigurationException(message, cause);
        
        assertEquals(message, configExceptionWithCause.getMessage());
        assertEquals(cause, configExceptionWithCause.getCause());
        assertEquals("CONFIGURATION_ERROR", configExceptionWithCause.getErrorCode());
    }

    @Test
    @DisplayName("测试向量异常")
    void testVectorException() {
        String message = "Vector operation failed";
        LanceDbVectorException vectorException = new LanceDbVectorException(message);
        
        assertEquals(message, vectorException.getMessage());
        assertEquals("VECTOR_ERROR", vectorException.getErrorCode());
        assertTrue(vectorException.hasErrorCode());
        assertTrue(vectorException instanceof LanceDbException);
        
        // 测试带原因的向量异常
        ArrayIndexOutOfBoundsException cause = new ArrayIndexOutOfBoundsException("Index out of bounds");
        LanceDbVectorException vectorExceptionWithCause = 
                new LanceDbVectorException(message, cause);
        
        assertEquals(message, vectorExceptionWithCause.getMessage());
        assertEquals(cause, vectorExceptionWithCause.getCause());
        assertEquals("VECTOR_ERROR", vectorExceptionWithCause.getErrorCode());
    }

    @Test
    @DisplayName("测试异常层次结构")
    void testExceptionHierarchy() {
        LanceDbException baseException = new LanceDbException("Base error");
        LanceDbClientException clientException = new LanceDbClientException("Client error");
        LanceDbConnectionException connectionException = new LanceDbConnectionException("Connection error");
        LanceDbConfigurationException configException = new LanceDbConfigurationException("Config error");
        LanceDbVectorException vectorException = new LanceDbVectorException("Vector error");
        
        // 验证继承关系
        assertTrue(baseException instanceof Exception);
        assertTrue(clientException instanceof LanceDbException);
        assertTrue(connectionException instanceof LanceDbException);
        assertTrue(configException instanceof LanceDbException);
        assertTrue(vectorException instanceof LanceDbException);
        
        // 验证不是彼此的实例
        assertFalse(clientException instanceof LanceDbConnectionException);
        assertFalse(connectionException instanceof LanceDbConfigurationException);
        assertFalse(configException instanceof LanceDbVectorException);
        assertFalse(vectorException instanceof LanceDbClientException);
    }

    @Test
    @DisplayName("测试空值和边界情况")
    void testNullAndEdgeCases() {
        // 测试空消息
        LanceDbException emptyMessageException = new LanceDbException("");
        assertEquals("", emptyMessageException.getMessage());
        
        // 测试null消息
        LanceDbException nullMessageException = new LanceDbException((String) null);
        assertNull(nullMessageException.getMessage());
        
        // 测试空错误代码
        LanceDbException emptyCodeException = new LanceDbException("message", "");
        assertEquals("", emptyCodeException.getErrorCode());
        assertFalse(emptyCodeException.hasErrorCode()); // 空字符串应该返回false
        
        // 测试空白错误代码
        LanceDbException blankCodeException = new LanceDbException("message", "   ");
        assertEquals("   ", blankCodeException.getErrorCode());
        assertFalse(blankCodeException.hasErrorCode()); // 空白字符串应该返回false
        
        // 测试null HTTP状态码
        LanceDbException nullHttpException = new LanceDbException("message", "code", null);
        assertNull(nullHttpException.getHttpStatusCode());
        assertFalse(nullHttpException.hasHttpStatusCode());
    }
}
