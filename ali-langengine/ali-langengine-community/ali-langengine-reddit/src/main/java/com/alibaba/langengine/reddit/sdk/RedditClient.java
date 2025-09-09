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
package com.alibaba.langengine.reddit.sdk;

import com.alibaba.langengine.reddit.RedditConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


@Slf4j
public class RedditClient {

    private final RedditConfiguration configuration;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private volatile long lastRequestTime = 0;

    /**
     * 单例HTTP客户端
     */
    private static volatile OkHttpClient defaultClient;

    /**
     * 构造函数
     *
     * @param configuration Reddit配置
     */
    public RedditClient(RedditConfiguration configuration) {
        this.configuration = configuration;
        this.httpClient = getDefaultClient(configuration);
        this.objectMapper = new ObjectMapper();
        log.info("Reddit client initialized with configuration: {}", configuration.getConfigurationSummary());
    }

    /**
     * 获取默认HTTP客户端（单例模式）
     *
     * @param config 配置
     * @return HTTP客户端
     */
    private static OkHttpClient getDefaultClient(RedditConfiguration config) {
        if (defaultClient == null) {
            synchronized (RedditClient.class) {
                if (defaultClient == null) {
                    defaultClient = new OkHttpClient.Builder()
                            .connectTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                            .readTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                            .writeTimeout(config.getTimeoutSeconds(), TimeUnit.SECONDS)
                            .build();
                    log.info("Created new Reddit HTTP client with timeout: {}s", config.getTimeoutSeconds());
                }
            }
        }
        return defaultClient;
    }

    /**
     * 搜索Reddit帖子
     *
     * @param request 搜索请求
     * @return 搜索响应
     * @throws RedditException 搜索失败时抛出
     */
    public RedditSearchResponse search(RedditSearchRequest request) throws RedditException {
        if (request == null || StringUtils.isBlank(request.getQuery())) {
            throw new RedditException("Search request and query cannot be null or empty");
        }

        try {
            // 速率限制
            enforceRateLimit();

            String url = buildSearchUrl(request);
            log.debug("Searching Reddit with URL: {}", url);

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .header("User-Agent", configuration.getUserAgent())
                    .header("Accept", "application/json")
                    .build();

            try (Response response = executeWithRetry(httpRequest)) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    throw new RedditException("Reddit API request failed: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                log.debug("Reddit API response received, length: {}", responseBody.length());

                RedditSearchResponse searchResponse = objectMapper.readValue(responseBody, RedditSearchResponse.class);
                log.info("Reddit search completed, found {} posts", 
                        searchResponse.getPosts() != null ? searchResponse.getPosts().size() : 0);

                return searchResponse;
            }
        } catch (IOException e) {
            log.error("Error executing Reddit search", e);
            throw new RedditException("Failed to execute Reddit search: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Reddit search", e);
            throw new RedditException("Unexpected error during Reddit search: " + e.getMessage(), e);
        }
    }

    /**
     * 构建搜索URL
     *
     * @param request 搜索请求
     * @return 搜索URL
     * @throws RedditException URL构建失败时抛出
     */
    private String buildSearchUrl(RedditSearchRequest request) throws RedditException {
        StringBuilder url = new StringBuilder(configuration.getBaseUrl());

        // 确定搜索路径
        if (StringUtils.isNotBlank(request.getSubreddit())) {
            url.append("/r/").append(request.getSubreddit()).append("/search.json");
        } else {
            url.append("/search.json");
        }

        // 添加查询参数
        url.append("?q=").append(encodeUrl(request.getQuery()));

        if (StringUtils.isNotBlank(request.getSubreddit())) {
            url.append("&restrict_sr=1"); // 限制在指定子论坛搜索
        }

        if (StringUtils.isNotBlank(request.getSort())) {
            url.append("&sort=").append(request.getSort());
        }

        if (StringUtils.isNotBlank(request.getTimeRange())) {
            url.append("&t=").append(request.getTimeRange());
        }

        if (request.getLimit() != null && request.getLimit() > 0) {
            url.append("&limit=").append(Math.min(request.getLimit(), 100));
        }

        if (StringUtils.isNotBlank(request.getAfter())) {
            url.append("&after=").append(request.getAfter());
        }

        if (StringUtils.isNotBlank(request.getType())) {
            url.append("&type=").append(request.getType());
        }

        if (request.getIncludeOver18() != null) {
            url.append("&include_over_18=").append(request.getIncludeOver18());
        }

        return url.toString();
    }

    /**
     * URL编码
     *
     * @param value 待编码的值
     * @return 编码后的值
     * @throws RedditException 编码失败时抛出
     */
    private String encodeUrl(String value) throws RedditException {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            log.error("Failed to encode URL parameter: {}", value, e);
            throw new RedditException("URL encoding failed for parameter: " + value, e);
        }
    }

    /**
     * 执行HTTP请求（带重试）
     *
     * @param request HTTP请求
     * @return HTTP响应
     * @throws IOException 请求失败时抛出
     */
    private Response executeWithRetry(Request request) throws IOException {
        IOException lastException = null;

        for (int i = 0; i <= configuration.getMaxRetries(); i++) {
            try {
                if (i > 0) {
                    log.debug("Retrying Reddit request, attempt {}/{}", i, configuration.getMaxRetries());
                    Thread.sleep(1000 * i); // 指数退避
                }

                Response response = httpClient.newCall(request).execute();
                
                // 检查响应状态码
                if (response.isSuccessful()) {
                    return response;
                }
                
                // 根据状态码决定是否重试
                int statusCode = response.code();
                String errorMessage = String.format("HTTP %d: %s", statusCode, response.message());
                
                response.close();
                
                // 4xx错误（除了429）通常不需要重试
                if (statusCode >= 400 && statusCode < 500 && statusCode != 429) {
                    String specificError = getSpecificErrorMessage(statusCode);
                    throw new IOException(specificError + " - " + errorMessage);
                }
                
                lastException = new IOException(errorMessage);

            } catch (IOException e) {
                lastException = e;
                log.warn("Reddit request failed, attempt {}/{}: {}", i + 1, configuration.getMaxRetries() + 1, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }

        throw lastException;
    }

    /**
     * 执行速率限制
     */
    private void enforceRateLimit() {
        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime;

            if (timeSinceLastRequest < configuration.getRequestIntervalMs()) {
                try {
                    long sleepTime = configuration.getRequestIntervalMs() - timeSinceLastRequest;
                    log.debug("Rate limiting: sleeping for {}ms", sleepTime);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Rate limiting interrupted", e);
                }
            }

            lastRequestTime = System.currentTimeMillis();
        }
    }

    /**
     * 关闭客户端资源
     */
    public void close() {
        // OkHttpClient的连接池会自动管理，这里不需要显式关闭
        log.info("Reddit client closed");
    }

    /**
     * 根据HTTP状态码获取具体的错误信息
     *
     * @param statusCode HTTP状态码
     * @return 具体的错误信息
     */
    private String getSpecificErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Bad Request - Invalid parameters or malformed request";
            case 401:
                return "Unauthorized - Reddit API access denied";
            case 403:
                return "Forbidden - Access to Reddit content is not allowed";
            case 404:
                return "Not Found - Reddit resource does not exist";
            case 429:
                return "Rate Limited - Too many requests to Reddit API";
            case 500:
                return "Internal Server Error - Reddit server error";
            case 502:
                return "Bad Gateway - Reddit service temporarily unavailable";
            case 503:
                return "Service Unavailable - Reddit service is temporarily down";
            case 504:
                return "Gateway Timeout - Reddit API response timeout";
            default:
                return "Reddit API Error";
        }
    }
}
