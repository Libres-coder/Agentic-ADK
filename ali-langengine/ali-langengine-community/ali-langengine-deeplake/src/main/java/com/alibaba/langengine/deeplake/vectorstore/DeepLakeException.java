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


public class DeepLakeException extends RuntimeException {

    private String errorCode;

    public DeepLakeException(String message) {
        super(message);
        this.errorCode = "DEEPLAKE_ERROR";
    }

    public DeepLakeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DeepLakeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DEEPLAKE_ERROR";
    }

    public DeepLakeException(String errorCode, String message, Throwable cause) {
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
        public static final String CONNECTION_ERROR = "DEEPLAKE_CONNECTION_ERROR";
        public static final String AUTHENTICATION_ERROR = "DEEPLAKE_AUTH_ERROR";
        public static final String DATASET_NOT_FOUND = "DEEPLAKE_DATASET_NOT_FOUND";
        public static final String INVALID_PARAMETER = "DEEPLAKE_INVALID_PARAMETER";
        public static final String VECTOR_DIMENSION_MISMATCH = "DEEPLAKE_VECTOR_DIMENSION_MISMATCH";
        public static final String OPERATION_FAILED = "DEEPLAKE_OPERATION_FAILED";
        public static final String SERIALIZATION_ERROR = "DEEPLAKE_SERIALIZATION_ERROR";
        public static final String NETWORK_ERROR = "DEEPLAKE_NETWORK_ERROR";
    }

    // Static factory methods for common error scenarios
    public static DeepLakeException connectionError(String message) {
        return new DeepLakeException(ErrorCodes.CONNECTION_ERROR, message);
    }

    public static DeepLakeException connectionError(String message, Throwable cause) {
        return new DeepLakeException(ErrorCodes.CONNECTION_ERROR, message, cause);
    }

    public static DeepLakeException authenticationError(String message) {
        return new DeepLakeException(ErrorCodes.AUTHENTICATION_ERROR, message);
    }

    public static DeepLakeException datasetNotFound(String datasetName) {
        return new DeepLakeException(ErrorCodes.DATASET_NOT_FOUND, 
            "Dataset not found: " + datasetName);
    }

    public static DeepLakeException invalidParameter(String message) {
        return new DeepLakeException(ErrorCodes.INVALID_PARAMETER, message);
    }

    public static DeepLakeException vectorDimensionError(String message) {
        return new DeepLakeException(ErrorCodes.VECTOR_DIMENSION_MISMATCH, message);
    }

    public static DeepLakeException operationFailed(String message) {
        return new DeepLakeException(ErrorCodes.OPERATION_FAILED, message);
    }

    public static DeepLakeException operationFailed(String message, Throwable cause) {
        return new DeepLakeException(ErrorCodes.OPERATION_FAILED, message, cause);
    }

    public static DeepLakeException serializationError(String message, Throwable cause) {
        return new DeepLakeException(ErrorCodes.SERIALIZATION_ERROR, message, cause);
    }

    public static DeepLakeException networkError(String message, Throwable cause) {
        return new DeepLakeException(ErrorCodes.NETWORK_ERROR, message, cause);
    }
}
