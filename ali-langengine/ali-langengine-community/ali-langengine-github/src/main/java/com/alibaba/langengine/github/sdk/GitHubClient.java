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

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.github.GitHubConfiguration.GITHUB_API_TOKEN;
import static com.alibaba.langengine.github.GitHubConfiguration.GITHUB_SEARCH_API_URL;
import static com.alibaba.langengine.github.sdk.GitHubConstant.*;


@Slf4j
public class GitHubClient {

    private final String apiToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    // 单例的默认 OkHttpClient，用于复用连接池
    private static volatile OkHttpClient defaultClient;

    /**
     * 获取默认的 OkHttpClient 单例实例
     * 
     * @return 默认的 OkHttpClient 实例
     */
    private static OkHttpClient getDefaultClient() {
        if (defaultClient == null) {
            synchronized (GitHubClient.class) {
                if (defaultClient == null) {
                    defaultClient = new OkHttpClient.Builder()
                            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                            .build();
                }
            }
        }
        return defaultClient;
    }

    /**
     * Constructs a GitHubClient with a specified API token.
     *
     * @param apiToken the API token for authentication with the GitHub service
     */
    public GitHubClient(String apiToken) {
        this.apiToken = apiToken;
        this.client = getDefaultClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a GitHubClient using the default API token from configuration.
     */
    public GitHubClient() {
        this.apiToken = GITHUB_API_TOKEN;
        this.client = getDefaultClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructs a GitHubClient with a specified API token and custom OkHttpClient.
     *
     * @param apiToken the API token for authentication with the GitHub service
     * @param okHttpClient the custom OkHttpClient instance to use for HTTP requests
     */
    public GitHubClient(String apiToken, OkHttpClient okHttpClient) {
        this.apiToken = apiToken;
        this.client = okHttpClient != null ? okHttpClient : getDefaultClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 搜索GitHub仓库
     *
     * @param request 搜索请求参数
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchRepositories(SearchRequest request) throws GitHubException {
        return executeSearch(REPOSITORIES_SEARCH_ENDPOINT, request);
    }

    /**
     * 搜索GitHub代码
     *
     * @param request 搜索请求参数
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchCode(SearchRequest request) throws GitHubException {
        return executeSearch(CODE_SEARCH_ENDPOINT, request);
    }

    /**
     * 搜索GitHub用户
     *
     * @param request 搜索请求参数
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchUsers(SearchRequest request) throws GitHubException {
        return executeSearch(USERS_SEARCH_ENDPOINT, request);
    }

    /**
     * 搜索GitHub问题
     *
     * @param request 搜索请求参数
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchIssues(SearchRequest request) throws GitHubException {
        return executeSearch(ISSUES_SEARCH_ENDPOINT, request);
    }

    /**
     * 执行搜索请求的通用方法
     *
     * @param endpoint 搜索端点
     * @param request 搜索请求参数
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    private SearchResponse executeSearch(String endpoint, SearchRequest request) throws GitHubException {
        try {
            // 构建HTTP URL及查询参数
            HttpUrl.Builder urlBuilder = HttpUrl.parse(GITHUB_SEARCH_API_URL + endpoint).newBuilder();
            
            if (request.getQuery() != null) {
                urlBuilder.addQueryParameter("q", request.getQuery());
            }
            
            if (request.getSort() != null) {
                urlBuilder.addQueryParameter("sort", request.getSort());
            }
            
            if (request.getOrder() != null) {
                urlBuilder.addQueryParameter("order", request.getOrder());
            }
            
            if (request.getPerPage() != null) {
                urlBuilder.addQueryParameter("per_page", request.getPerPage().toString());
            }
            
            if (request.getPage() != null) {
                urlBuilder.addQueryParameter("page", request.getPage().toString());
            }

            // 创建HTTP请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("User-Agent", "ali-langengine-github/1.0")
                    .get();

            // 添加认证头
            if (apiToken != null && !apiToken.trim().isEmpty()) {
                requestBuilder.addHeader("Authorization", "token " + apiToken);
            }

            Request httpRequest = requestBuilder.build();

            // 执行请求
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = "";
                    ResponseBody body = response.body();
                    if (body != null) {
                        errorBody = body.string();
                    }
                    
                    if (response.code() == 403) {
                        throw new GitHubException("GitHub API rate limit exceeded or forbidden access. Error: " + errorBody);
                    } else if (response.code() == 401) {
                        throw new GitHubException("GitHub API authentication failed. Please check your token. Error: " + errorBody);
                    } else {
                        throw new GitHubException("GitHub API request failed: " + response.code() + " " + response.message() + ". Error: " + errorBody);
                    }
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new GitHubException("GitHub API returned empty response");
                }

                // 解析响应
                return objectMapper.readValue(body.string(), SearchResponse.class);
            }
        } catch (IOException e) {
            log.error("Error occurred during GitHub API call", e);
            throw new GitHubException("Error occurred during GitHub API call", e);
        }
    }

    /**
     * 简化的仓库搜索方法
     *
     * @param query 搜索查询字符串
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchRepositories(String query) throws GitHubException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        return searchRepositories(request);
    }

    /**
     * 简化的仓库搜索方法，带结果数限制
     *
     * @param query 搜索查询字符串
     * @param perPage 每页结果数
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchRepositories(String query, int perPage) throws GitHubException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setPerPage(perPage);
        return searchRepositories(request);
    }

    /**
     * 简化的代码搜索方法
     *
     * @param query 搜索查询字符串
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchCode(String query) throws GitHubException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        return searchCode(request);
    }

    /**
     * 简化的代码搜索方法，带结果数限制
     *
     * @param query 搜索查询字符串
     * @param perPage 每页结果数
     * @return 搜索响应结果
     * @throws GitHubException 当API调用失败时抛出
     */
    public SearchResponse searchCode(String query, int perPage) throws GitHubException {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setPerPage(perPage);
        return searchCode(request);
    }
}
