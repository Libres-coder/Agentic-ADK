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

public class GitCodeConstant {

    /**
     * User search endpoint
     */
    public static final String USERS_SEARCH_ENDPOINT = "/users";

    /**
     * Issues search endpoint
     */
    public static final String ISSUES_SEARCH_ENDPOINT = "/issues";

    /**
     * Repository search endpoint
     */
    public static final String REPOSITORIES_SEARCH_ENDPOINT = "/repositories";

    /**
     * Default timeout in seconds
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Sort field: registration time
     */
    public static final String SORT_JOINED_AT = "joined_at";

    /**
     * Sort field: creation time
     */
    public static final String SORT_CREATED_AT = "created_at";

    /**
     * Sort field: last push time
     */
    public static final String SORT_LAST_PUSH_AT = "last_push_at";

    /**
     * Sort field: star count
     */
    public static final String SORT_STARS_COUNT = "stars_count";

    /**
     * Sort field: fork count
     */
    public static final String SORT_FORKS_COUNT = "forks_count";

    /**
     * Sort order: ascending
     */
    public static final String ORDER_ASC = "asc";

    /**
     * Sort order: descending
     */
    public static final String ORDER_DESC = "desc";

    /**
     * Issue state: open
     */
    public static final String STATE_OPEN = "open";

    /**
     * Issue state: closed
     */
    public static final String STATE_CLOSED = "closed";

    /**
     * HTTP status code: success
     */
    public static final int HTTP_OK = 200;

    /**
     * HTTP status code: unauthorized
     */
    public static final int HTTP_UNAUTHORIZED = 401;

    /**
     * HTTP status code: forbidden
     */
    public static final int HTTP_FORBIDDEN = 403;

    /**
     * HTTP status code: not found
     */
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * HTTP status code: rate limit exceeded
     */
    public static final int HTTP_RATE_LIMIT = 429;
}