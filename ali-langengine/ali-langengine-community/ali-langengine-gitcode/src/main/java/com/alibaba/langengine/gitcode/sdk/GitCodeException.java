/**
 * Copyright (C) 2025 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.gitcode.sdk;

public class GitCodeException extends Exception {

    private int statusCode;
    private String errorBody;

    /**
     * Construct GitCodeException
     *
     * @param message Error message
     */
    public GitCodeException(String message) {
        super(message);
    }

    /**
     * Construct GitCodeException
     *
     * @param message Error message
     * @param cause Cause
     */
    public GitCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct GitCodeException
     *
     * @param message Error message
     * @param statusCode HTTP status code
     * @param errorBody Error response body
     */
    public GitCodeException(String message, int statusCode, String errorBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }

    /**
     * Construct GitCodeException
     *
     * @param message Error message
     * @param cause Cause
     * @param statusCode HTTP status code
     * @param errorBody Error response body
     */
    public GitCodeException(String message, Throwable cause, int statusCode, String errorBody) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }

    /**
     * Get HTTP status code
     *
     * @return HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Get error response body
     *
     * @return Error response body
     */
    public String getErrorBody() {
        return errorBody;
    }

    /**
     * Check if it is authentication error
     *
     * @return true if it is authentication error
     */
    public boolean isAuthenticationError() {
        return statusCode == 401;
    }

    /**
     * Check if it is forbidden error
     *
     * @return true if it is forbidden error
     */
    public boolean isForbiddenError() {
        return statusCode == 403;
    }

    /**
     * Check if it is rate limit error
     *
     * @return true if it is rate limit error
     */
    public boolean isRateLimitError() {
        return statusCode == 429;
    }

    /**
     * Check if it is not found error
     *
     * @return true if it is not found error
     */
    public boolean isNotFoundError() {
        return statusCode == 404;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GitCodeException{");
        sb.append("message='").append(getMessage()).append('\'');
        if (statusCode > 0) {
            sb.append(", statusCode=").append(statusCode);
        }
        if (errorBody != null && !errorBody.trim().isEmpty()) {
            sb.append(", errorBody='").append(errorBody).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}