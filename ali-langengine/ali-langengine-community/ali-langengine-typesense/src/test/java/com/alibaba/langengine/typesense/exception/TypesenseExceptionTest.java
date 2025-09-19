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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class TypesenseExceptionTest {

    @Test
    public void testDefaultConstructor() {
        String message = "Test error message";
        TypesenseException exception = new TypesenseException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(TypesenseException.OPERATION_ERROR, exception.getErrorCode());
    }

    @Test
    public void testConstructorWithErrorCode() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        TypesenseException exception = new TypesenseException(errorCode, message);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    public void testConstructorWithCause() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        Exception cause = new RuntimeException("Root cause");
        TypesenseException exception = new TypesenseException(errorCode, message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConnectionError() {
        String message = "Connection failed";
        Exception cause = new RuntimeException("Network error");
        TypesenseException exception = TypesenseException.connectionError(message, cause);

        assertTrue(exception.getMessage().contains("Typesense连接失败"));
        assertTrue(exception.getMessage().contains(message));
        assertEquals(TypesenseException.CONNECTION_ERROR, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConfigurationError() {
        String message = "Invalid configuration";
        TypesenseException exception = TypesenseException.configurationError(message);

        assertTrue(exception.getMessage().contains("Typesense配置错误"));
        assertTrue(exception.getMessage().contains(message));
        assertEquals(TypesenseException.CONFIGURATION_ERROR, exception.getErrorCode());
    }

    @Test
    public void testOperationError() {
        String message = "Operation failed";
        Exception cause = new RuntimeException("Operation error");
        TypesenseException exception = TypesenseException.operationError(message, cause);

        assertTrue(exception.getMessage().contains("Typesense操作失败"));
        assertTrue(exception.getMessage().contains(message));
        assertEquals(TypesenseException.OPERATION_ERROR, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testValidationError() {
        String message = "Invalid parameter";
        TypesenseException exception = TypesenseException.validationError(message);

        assertTrue(exception.getMessage().contains("参数验证失败"));
        assertTrue(exception.getMessage().contains(message));
        assertEquals(TypesenseException.VALIDATION_ERROR, exception.getErrorCode());
    }

    @Test
    public void testInitializationError() {
        String message = "Initialization failed";
        Exception cause = new RuntimeException("Init error");
        TypesenseException exception = TypesenseException.initializationError(message, cause);

        assertTrue(exception.getMessage().contains("Typesense初始化失败"));
        assertTrue(exception.getMessage().contains(message));
        assertEquals(TypesenseException.INITIALIZATION_ERROR, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testToString() {
        String errorCode = "TEST_ERROR";
        String message = "Test message";
        TypesenseException exception = new TypesenseException(errorCode, message);

        String result = exception.toString();
        assertTrue(result.contains("TypesenseException"));
        assertTrue(result.contains(errorCode));
        assertTrue(result.contains(message));
    }

    @Test
    public void testErrorCodeConstants() {
        assertEquals("TYPESENSE_CONNECTION_ERROR", TypesenseException.CONNECTION_ERROR);
        assertEquals("TYPESENSE_CONFIG_ERROR", TypesenseException.CONFIGURATION_ERROR);
        assertEquals("TYPESENSE_OPERATION_ERROR", TypesenseException.OPERATION_ERROR);
        assertEquals("TYPESENSE_VALIDATION_ERROR", TypesenseException.VALIDATION_ERROR);
        assertEquals("TYPESENSE_INIT_ERROR", TypesenseException.INITIALIZATION_ERROR);
    }
}