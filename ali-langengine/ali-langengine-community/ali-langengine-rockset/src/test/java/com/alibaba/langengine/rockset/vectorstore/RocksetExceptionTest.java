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
package com.alibaba.langengine.rockset.vectorstore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class RocksetExceptionTest {

    @Test
    public void testBasicExceptionCreation() {
        String message = "Test error message";
        RocksetException exception = new RocksetException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals("ROCKSET_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testExceptionWithErrorCode() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        RocksetException exception = new RocksetException(errorCode, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    public void testExceptionWithCause() {
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Root cause");
        RocksetException exception = new RocksetException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals("ROCKSET_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testExceptionWithErrorCodeAndCause() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        RuntimeException cause = new RuntimeException("Root cause");
        RocksetException exception = new RocksetException(errorCode, message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testConnectionErrorFactory() {
        String message = "Connection failed";
        RocksetException exception = RocksetException.connectionError(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.CONNECTION_ERROR, exception.getErrorCode());
    }

    @Test
    public void testConnectionErrorWithCause() {
        String message = "Connection failed";
        RuntimeException cause = new RuntimeException("Network error");
        RocksetException exception = RocksetException.connectionError(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.CONNECTION_ERROR, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testAuthenticationError() {
        String message = "Invalid API key";
        RocksetException exception = RocksetException.authenticationError(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.AUTHENTICATION_ERROR, exception.getErrorCode());
    }

    @Test
    public void testCollectionNotFound() {
        String collectionName = "test_collection";
        RocksetException exception = RocksetException.collectionNotFound(collectionName);
        
        assertEquals("Collection not found: " + collectionName, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.COLLECTION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testWorkspaceNotFound() {
        String workspaceName = "test_workspace";
        RocksetException exception = RocksetException.workspaceNotFound(workspaceName);
        
        assertEquals("Workspace not found: " + workspaceName, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.WORKSPACE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testInvalidParameter() {
        String message = "Invalid parameter value";
        RocksetException exception = RocksetException.invalidParameter(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.INVALID_PARAMETER, exception.getErrorCode());
    }

    @Test
    public void testVectorDimensionError() {
        String message = "Vector dimension mismatch";
        RocksetException exception = RocksetException.vectorDimensionError(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.VECTOR_DIMENSION_MISMATCH, exception.getErrorCode());
    }

    @Test
    public void testOperationFailed() {
        String message = "Operation failed";
        RocksetException exception = RocksetException.operationFailed(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.OPERATION_FAILED, exception.getErrorCode());
    }

    @Test
    public void testOperationFailedWithCause() {
        String message = "Operation failed";
        RuntimeException cause = new RuntimeException("Database error");
        RocksetException exception = RocksetException.operationFailed(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.OPERATION_FAILED, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testSerializationError() {
        String message = "JSON parsing error";
        RuntimeException cause = new RuntimeException("Invalid JSON");
        RocksetException exception = RocksetException.serializationError(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.SERIALIZATION_ERROR, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testNetworkError() {
        String message = "Network timeout";
        RuntimeException cause = new RuntimeException("Connection timeout");
        RocksetException exception = RocksetException.networkError(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.NETWORK_ERROR, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testQueryError() {
        String message = "SQL query failed";
        RocksetException exception = RocksetException.queryError(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.QUERY_ERROR, exception.getErrorCode());
    }

    @Test
    public void testQueryErrorWithCause() {
        String message = "SQL query failed";
        RuntimeException cause = new RuntimeException("Syntax error");
        RocksetException exception = RocksetException.queryError(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(RocksetException.ErrorCodes.QUERY_ERROR, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void testErrorCodes() {
        // 验证所有错误码常量存在
        assertEquals("ROCKSET_CONNECTION_ERROR", RocksetException.ErrorCodes.CONNECTION_ERROR);
        assertEquals("ROCKSET_AUTH_ERROR", RocksetException.ErrorCodes.AUTHENTICATION_ERROR);
        assertEquals("ROCKSET_COLLECTION_NOT_FOUND", RocksetException.ErrorCodes.COLLECTION_NOT_FOUND);
        assertEquals("ROCKSET_WORKSPACE_NOT_FOUND", RocksetException.ErrorCodes.WORKSPACE_NOT_FOUND);
        assertEquals("ROCKSET_INVALID_PARAMETER", RocksetException.ErrorCodes.INVALID_PARAMETER);
        assertEquals("ROCKSET_VECTOR_DIMENSION_MISMATCH", RocksetException.ErrorCodes.VECTOR_DIMENSION_MISMATCH);
        assertEquals("ROCKSET_OPERATION_FAILED", RocksetException.ErrorCodes.OPERATION_FAILED);
        assertEquals("ROCKSET_SERIALIZATION_ERROR", RocksetException.ErrorCodes.SERIALIZATION_ERROR);
        assertEquals("ROCKSET_NETWORK_ERROR", RocksetException.ErrorCodes.NETWORK_ERROR);
        assertEquals("ROCKSET_QUERY_ERROR", RocksetException.ErrorCodes.QUERY_ERROR);
    }
}
