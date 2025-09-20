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

public class GiteeConstant {

    /**
     * Search endpoints
     */
    public static final String USERS_SEARCH_ENDPOINT = "/users";
    public static final String ISSUES_SEARCH_ENDPOINT = "/issues";
    public static final String REPOSITORIES_SEARCH_ENDPOINT = "/repositories";

    /**
     * HTTP Status Codes
     */
    public static final int HTTP_OK = 200;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_RATE_LIMIT = 429;

    /**
     * Default timeout in seconds
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * Default pagination
     */
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PER_PAGE = 20;
    public static final int MAX_PER_PAGE = 100;

    /**
     * Sort options for users
     */
    public static final String SORT_JOINED_AT = "joined_at";

    /**
     * Sort options for issues
     */
    public static final String SORT_CREATED_AT = "created_at";
    public static final String SORT_LAST_PUSH_AT = "last_push_at";
    public static final String SORT_NOTES_COUNT = "notes_count";

    /**
     * Sort options for repositories
     */
    public static final String SORT_STARS_COUNT = "stars_count";
    public static final String SORT_FORKS_COUNT = "forks_count";
    public static final String SORT_WATCHES_COUNT = "watches_count";

    /**
     * Sort order
     */
    public static final String ORDER_ASC = "asc";
    public static final String ORDER_DESC = "desc";

    /**
     * Issue states
     */
    public static final String ISSUE_STATE_OPEN = "open";
    public static final String ISSUE_STATE_CLOSED = "closed";
    public static final String ISSUE_STATE_REJECTED = "rejected";

    /**
     * User agent
     */
    public static final String USER_AGENT = "ali-langengine-gitee/1.0";
}