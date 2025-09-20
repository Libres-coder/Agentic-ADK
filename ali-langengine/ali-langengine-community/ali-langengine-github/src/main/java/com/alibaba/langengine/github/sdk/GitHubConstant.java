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
package com.alibaba.langengine.github.sdk;


public class GitHubConstant {

    /**
     * 默认超时时间（秒）
     */
    public static final int DEFAULT_TIMEOUT = 30;

    /**
     * 搜索仓库端点
     */
    public static final String REPOSITORIES_SEARCH_ENDPOINT = "/repositories";

    /**
     * 搜索代码端点
     */
    public static final String CODE_SEARCH_ENDPOINT = "/code";

    /**
     * 搜索用户端点
     */
    public static final String USERS_SEARCH_ENDPOINT = "/users";

    /**
     * 搜索问题端点
     */
    public static final String ISSUES_SEARCH_ENDPOINT = "/issues";

    /**
     * 默认每页结果数
     */
    public static final int DEFAULT_PER_PAGE = 30;

    /**
     * 最大每页结果数
     */
    public static final int MAX_PER_PAGE = 100;

    /**
     * 默认排序方式
     */
    public static final String DEFAULT_SORT = "best-match";

    /**
     * 默认排序顺序
     */
    public static final String DEFAULT_ORDER = "desc";
}
