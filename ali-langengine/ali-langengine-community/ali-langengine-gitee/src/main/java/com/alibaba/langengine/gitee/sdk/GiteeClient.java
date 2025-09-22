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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.alibaba.langengine.gitee.GiteeConfiguration;
import static com.alibaba.langengine.gitee.GiteeConfiguration.GITEE_SEARCH_API_URL;
import static com.alibaba.langengine.gitee.sdk.GiteeConstant.*;

@Slf4j
public class GiteeClient {

    private final String accessToken;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    private static volatile OkHttpClient defaultClient;

    /**
     * Get the default singleton instance of OkHttpClient
     *
     * @return Default OkHttpClient instance
     */
    private static OkHttpClient getDefaultClient() {
        if (defaultClient == null) {
            synchronized (GiteeClient.class) {
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
     * Constructor with specified access token
     *
     * @param accessToken Gitee access token
     */
    public GiteeClient(String accessToken) {
        this.accessToken = accessToken;
        this.client = getDefaultClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructor using default access token
     */
    public GiteeClient() {
        this.accessToken = GiteeConfiguration.getAccessToken();
        this.client = getDefaultClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructor with specified access token and custom OkHttpClient
     *
     * @param accessToken Gitee access token
     * @param okHttpClient Custom HTTP client
     */
    public GiteeClient(String accessToken, OkHttpClient okHttpClient) {
        this.accessToken = accessToken;
        this.client = okHttpClient != null ? okHttpClient : getDefaultClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Search Gitee users
     *
     * @param request Search request parameters
     * @return List of users
     * @throws GiteeException Thrown when API call fails
     */
    public List<User> searchUsers(SearchRequest request) throws GiteeException {
        return executeSearch(USERS_SEARCH_ENDPOINT, request, new TypeReference<List<User>>() {});
    }

    /**
     * Search Gitee Issues
     *
     * @param request Search request parameters
     * @return List of issues
     * @throws GiteeException Thrown when API call fails
     */
    public List<Issue> searchIssues(SearchRequest request) throws GiteeException {
        return executeSearch(ISSUES_SEARCH_ENDPOINT, request, new TypeReference<List<Issue>>() {});
    }

    /**
     * Search Gitee repositories
     *
     * @param request Search request parameters
     * @return List of repositories
     * @throws GiteeException Thrown when API call fails
     */
    public List<Repository> searchRepositories(SearchRequest request) throws GiteeException {
        return executeSearch(REPOSITORIES_SEARCH_ENDPOINT, request, new TypeReference<List<Repository>>() {});
    }

    /**
     * Generic method for executing search requests
     *
     * @param endpoint Search endpoint
     * @param request Search request parameters
     * @param typeReference Type reference for deserialization
     * @return Search results
     * @throws GiteeException Thrown when API call fails
     */
    private <T> T executeSearch(String endpoint, SearchRequest request, TypeReference<T> typeReference) throws GiteeException {
        try {
            String fullUrl = GITEE_SEARCH_API_URL + endpoint;
            log.debug("构建API URL: {}", fullUrl);
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(fullUrl)).newBuilder();

            if (accessToken != null && !accessToken.trim().isEmpty()) {
                urlBuilder.addQueryParameter("access_token", accessToken);
            }

            if (request.getQ() != null) {
                urlBuilder.addQueryParameter("q", request.getQ());
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

            // Repository specific parameters
            if (request.getOwner() != null) {
                urlBuilder.addQueryParameter("owner", request.getOwner());
            }

            if (request.getFork() != null) {
                urlBuilder.addQueryParameter("fork", request.getFork().toString());
            }

            if (request.getLanguage() != null) {
                urlBuilder.addQueryParameter("language", request.getLanguage());
            }

            // Issue specific parameters
            if (request.getRepo() != null) {
                urlBuilder.addQueryParameter("repo", request.getRepo());
            }

            if (request.getState() != null) {
                urlBuilder.addQueryParameter("state", request.getState());
            }

            if (request.getLabel() != null) {
                urlBuilder.addQueryParameter("label", request.getLabel());
            }

            if (request.getAuthor() != null) {
                urlBuilder.addQueryParameter("author", request.getAuthor());
            }

            if (request.getAssignee() != null) {
                urlBuilder.addQueryParameter("assignee", request.getAssignee());
            }

            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", USER_AGENT)
                    .get();

            Request httpRequest = requestBuilder.build();
            log.debug("发送请求到: {}", httpRequest.url());

            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = "";
                    ResponseBody body = response.body();
                    if (body != null) {
                        errorBody = body.string();
                    }

                    log.error("API request failed. Status: {}, URL: {}, Error: {}",
                            response.code(), httpRequest.url(), errorBody);

                    if (response.code() == HTTP_FORBIDDEN) {
                        throw new GiteeException("Gitee API rate limit exceeded or forbidden access",
                                response.code(), errorBody);
                    } else if (response.code() == HTTP_UNAUTHORIZED) {
                        throw new GiteeException("Gitee API authentication failed. Please check your access token",
                                response.code(), errorBody);
                    } else if (response.code() == HTTP_NOT_FOUND) {
                        throw new GiteeException("Gitee API endpoint not found",
                                response.code(), errorBody);
                    } else if (response.code() == HTTP_RATE_LIMIT) {
                        throw new GiteeException("Gitee API rate limit exceeded",
                                response.code(), errorBody);
                    } else {
                        throw new GiteeException("Gitee API request failed: " + response.code() + " " + response.message(),
                                response.code(), errorBody);
                    }
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new GiteeException("Gitee API returned empty response");
                }

                return objectMapper.readValue(body.string(), typeReference);
            }
        } catch (IOException e) {
            log.error("Error occurred during Gitee API call", e);
            throw new GiteeException("Error occurred during Gitee API call", e);
        }
    }

    /**
     * Simplified user search method
     *
     * @param query Search query string
     * @return List of users
     * @throws GiteeException Thrown when API call fails
     */
    public List<User> searchUsers(String query) throws GiteeException {
        SearchRequest request = SearchRequest.forUsers(query);
        return searchUsers(request);
    }

    /**
     * Simplified user search method with result limit
     *
     * @param query Search query string
     * @param perPage Number of results per page
     * @return List of users
     * @throws GiteeException Thrown when API call fails
     */
    public List<User> searchUsers(String query, int perPage) throws GiteeException {
        SearchRequest request = SearchRequest.forUsers(query).withPagination(null, perPage);
        return searchUsers(request);
    }

    /**
     * Simplified issue search method
     *
     * @param query Search query string
     * @return List of issues
     * @throws GiteeException Thrown when API call fails
     */
    public List<Issue> searchIssues(String query) throws GiteeException {
        SearchRequest request = SearchRequest.forIssues(query);
        return searchIssues(request);
    }

    /**
     * Simplified issue search method with result limit
     *
     * @param query Search query string
     * @param perPage Number of results per page
     * @return List of issues
     * @throws GiteeException Thrown when API call fails
     */
    public List<Issue> searchIssues(String query, int perPage) throws GiteeException {
        SearchRequest request = SearchRequest.forIssues(query).withPagination(null, perPage);
        return searchIssues(request);
    }

    /**
     * Simplified repository search method
     *
     * @param query Search query string
     * @return List of repositories
     * @throws GiteeException Thrown when API call fails
     */
    public List<Repository> searchRepositories(String query) throws GiteeException {
        SearchRequest request = SearchRequest.forRepositories(query);
        return searchRepositories(request);
    }

    /**
     * Simplified repository search method with result limit
     *
     * @param query Search query string
     * @param perPage Number of results per page
     * @return List of repositories
     * @throws GiteeException Thrown when API call fails
     */
    public List<Repository> searchRepositories(String query, int perPage) throws GiteeException {
        SearchRequest request = SearchRequest.forRepositories(query).withPagination(null, perPage);
        return searchRepositories(request);
    }
}