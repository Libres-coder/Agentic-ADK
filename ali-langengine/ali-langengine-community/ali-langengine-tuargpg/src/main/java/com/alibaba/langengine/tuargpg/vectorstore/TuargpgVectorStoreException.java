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
package com.alibaba.langengine.tuargpg.vectorstore;

public class TuargpgVectorStoreException extends RuntimeException {

    private final String errorCode;

    private final String errorMessage;

    public TuargpgVectorStoreException(String errorCode, String errorMessage) {
        super(String.format("[%s] %s", errorCode, errorMessage));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public TuargpgVectorStoreException(String errorCode, String errorMessage, Throwable cause) {
        super(String.format("[%s] %s", errorCode, errorMessage), cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static class ErrorCodes {
        public static final String CONNECTION_FAILED = "TUARGPG_CONNECTION_FAILED";
        public static final String AUTHENTICATION_FAILED = "TUARGPG_AUTH_FAILED";
        public static final String TABLE_NOT_FOUND = "TUARGPG_TABLE_NOT_FOUND";
        public static final String INVALID_VECTOR_DIMENSION = "TUARGPG_INVALID_VECTOR_DIMENSION";
        public static final String QUERY_EXECUTION_FAILED = "TUARGPG_QUERY_FAILED";
        public static final String INVALID_PARAMETER = "TUARGPG_INVALID_PARAMETER";
        public static final String OPERATION_TIMEOUT = "TUARGPG_OPERATION_TIMEOUT";
        public static final String UNKNOWN_ERROR = "TUARGPG_UNKNOWN_ERROR";
    }
}