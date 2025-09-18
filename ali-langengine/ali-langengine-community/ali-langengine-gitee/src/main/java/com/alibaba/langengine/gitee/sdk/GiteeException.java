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

package com.alibaba.langengine.gitee.sdk;

import static com.alibaba.langengine.gitee.sdk.GiteeConstant.*;

public class GiteeException extends Exception {

    private int statusCode;
    private String errorBody;

    public GiteeException(String message) {
        super(message);
    }

    public GiteeException(String message, Throwable cause) {
        super(message, cause);
    }

    public GiteeException(String message, int statusCode, String errorBody) {
        super(message);
        this.statusCode = statusCode;
        this.errorBody = errorBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public boolean isAuthenticationError() {
        return statusCode == HTTP_UNAUTHORIZED;
    }

    public boolean isForbiddenError() {
        return statusCode == HTTP_FORBIDDEN;
    }

    public boolean isNotFoundError() {
        return statusCode == HTTP_NOT_FOUND;
    }

    public boolean isRateLimitError() {
        return statusCode == HTTP_RATE_LIMIT;
    }

    @Override
    public String toString() {
        return "GiteeException{" +
                "message=" + getMessage() +
                ", statusCode=" + statusCode +
                ", errorBody='" + errorBody + '\'' +
                '}';
    }
}