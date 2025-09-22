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
package com.alibaba.langengine.deeplake.vectorstore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DeepLakeExceptionTest {

    @Test
    public void testConnectionError() {
        DeepLakeException exception = DeepLakeException.connectionError("Connection failed");
        
        assertEquals(DeepLakeException.ErrorCodes.CONNECTION_ERROR, exception.getErrorCode());
        assertEquals("Connection failed", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testConnectionErrorWithCause() {
        RuntimeException cause = new RuntimeException("Network timeout");
        DeepLakeException exception = DeepLakeException.connectionError("Connection failed", cause);
        
        assertEquals(DeepLakeException.ErrorCodes.CONNECTION_ERROR, exception.getErrorCode());
        assertEquals("Connection failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testAuthenticationError() {
        DeepLakeException exception = DeepLakeException.authenticationError("Invalid API token");
        
        assertEquals(DeepLakeException.ErrorCodes.AUTHENTICATION_ERROR, exception.getErrorCode());
        assertEquals("Invalid API token", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testInvalidParameterError() {
        DeepLakeException exception = DeepLakeException.invalidParameter("Invalid dimension");
        
        assertEquals(DeepLakeException.ErrorCodes.INVALID_PARAMETER, exception.getErrorCode());
        assertEquals("Invalid dimension", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testDatasetNotFoundError() {
        DeepLakeException exception = DeepLakeException.datasetNotFound("test_dataset");
        
        assertEquals(DeepLakeException.ErrorCodes.DATASET_NOT_FOUND, exception.getErrorCode());
        assertEquals("Dataset not found: test_dataset", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testOperationFailedError() {
        DeepLakeException exception = DeepLakeException.operationFailed("Operation failed");
        
        assertEquals(DeepLakeException.ErrorCodes.OPERATION_FAILED, exception.getErrorCode());
        assertEquals("Operation failed", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    public void testErrorCodes() {
        // 测试错误码常量
        assertEquals("DEEPLAKE_CONNECTION_ERROR", DeepLakeException.ErrorCodes.CONNECTION_ERROR);
        assertEquals("DEEPLAKE_AUTH_ERROR", DeepLakeException.ErrorCodes.AUTHENTICATION_ERROR);
        assertEquals("DEEPLAKE_INVALID_PARAMETER", DeepLakeException.ErrorCodes.INVALID_PARAMETER);
        assertEquals("DEEPLAKE_DATASET_NOT_FOUND", DeepLakeException.ErrorCodes.DATASET_NOT_FOUND);
        assertEquals("DEEPLAKE_OPERATION_FAILED", DeepLakeException.ErrorCodes.OPERATION_FAILED);
        assertEquals("DEEPLAKE_VECTOR_DIMENSION_MISMATCH", DeepLakeException.ErrorCodes.VECTOR_DIMENSION_MISMATCH);
        assertEquals("DEEPLAKE_SERIALIZATION_ERROR", DeepLakeException.ErrorCodes.SERIALIZATION_ERROR);
        assertEquals("DEEPLAKE_NETWORK_ERROR", DeepLakeException.ErrorCodes.NETWORK_ERROR);
    }

    @Test
    public void testExceptionHierarchy() {
        DeepLakeException exception = DeepLakeException.connectionError("Test");
        
        // 测试异常继承关系
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    public void testExceptionWithNullMessage() {
        DeepLakeException exception = DeepLakeException.connectionError(null);
        
        assertEquals(DeepLakeException.ErrorCodes.CONNECTION_ERROR, exception.getErrorCode());
        assertNull(exception.getMessage());
    }

    @Test
    public void testExceptionWithEmptyMessage() {
        DeepLakeException exception = DeepLakeException.invalidParameter("");
        
        assertEquals(DeepLakeException.ErrorCodes.INVALID_PARAMETER, exception.getErrorCode());
        assertEquals("", exception.getMessage());
    }

    @Test
    public void testGetErrorCodeMethod() {
        DeepLakeException exception = new DeepLakeException(
            DeepLakeException.ErrorCodes.OPERATION_FAILED, 
            "Test message"
        );
        
        assertEquals(DeepLakeException.ErrorCodes.OPERATION_FAILED, exception.getErrorCode());
        assertEquals("Test message", exception.getMessage());
    }
}
