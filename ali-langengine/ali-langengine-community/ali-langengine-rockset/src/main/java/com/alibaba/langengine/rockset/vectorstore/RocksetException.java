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


public class RocksetException extends RuntimeException {

    private String errorCode;

    public RocksetException(String message) {
        super(message);
        this.errorCode = "ROCKSET_ERROR";
    }

    public RocksetException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RocksetException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ROCKSET_ERROR";
    }

    public RocksetException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Error codes for different scenarios
     */
    public static class ErrorCodes {
        public static final String CONNECTION_ERROR = "ROCKSET_CONNECTION_ERROR";
        public static final String AUTHENTICATION_ERROR = "ROCKSET_AUTH_ERROR";
        public static final String COLLECTION_NOT_FOUND = "ROCKSET_COLLECTION_NOT_FOUND";
        public static final String WORKSPACE_NOT_FOUND = "ROCKSET_WORKSPACE_NOT_FOUND";
        public static final String INVALID_PARAMETER = "ROCKSET_INVALID_PARAMETER";
        public static final String VECTOR_DIMENSION_MISMATCH = "ROCKSET_VECTOR_DIMENSION_MISMATCH";
        public static final String OPERATION_FAILED = "ROCKSET_OPERATION_FAILED";
        public static final String SERIALIZATION_ERROR = "ROCKSET_SERIALIZATION_ERROR";
        public static final String NETWORK_ERROR = "ROCKSET_NETWORK_ERROR";
        public static final String QUERY_ERROR = "ROCKSET_QUERY_ERROR";
    }

    // Static factory methods for common error scenarios
    public static RocksetException connectionError(String message) {
        return new RocksetException(ErrorCodes.CONNECTION_ERROR, message);
    }

    public static RocksetException connectionError(String message, Throwable cause) {
        return new RocksetException(ErrorCodes.CONNECTION_ERROR, message, cause);
    }

    public static RocksetException authenticationError(String message) {
        return new RocksetException(ErrorCodes.AUTHENTICATION_ERROR, message);
    }

    public static RocksetException collectionNotFound(String collectionName) {
        return new RocksetException(ErrorCodes.COLLECTION_NOT_FOUND, 
            "Collection not found: " + collectionName);
    }

    public static RocksetException workspaceNotFound(String workspaceName) {
        return new RocksetException(ErrorCodes.WORKSPACE_NOT_FOUND, 
            "Workspace not found: " + workspaceName);
    }

    public static RocksetException invalidParameter(String message) {
        return new RocksetException(ErrorCodes.INVALID_PARAMETER, message);
    }

    public static RocksetException vectorDimensionError(String message) {
        return new RocksetException(ErrorCodes.VECTOR_DIMENSION_MISMATCH, message);
    }

    public static RocksetException operationFailed(String message) {
        return new RocksetException(ErrorCodes.OPERATION_FAILED, message);
    }

    public static RocksetException operationFailed(String message, Throwable cause) {
        return new RocksetException(ErrorCodes.OPERATION_FAILED, message, cause);
    }

    public static RocksetException serializationError(String message, Throwable cause) {
        return new RocksetException(ErrorCodes.SERIALIZATION_ERROR, message, cause);
    }

    public static RocksetException networkError(String message, Throwable cause) {
        return new RocksetException(ErrorCodes.NETWORK_ERROR, message, cause);
    }

    public static RocksetException queryError(String message) {
        return new RocksetException(ErrorCodes.QUERY_ERROR, message);
    }

    public static RocksetException queryError(String message, Throwable cause) {
        return new RocksetException(ErrorCodes.QUERY_ERROR, message, cause);
    }
}
