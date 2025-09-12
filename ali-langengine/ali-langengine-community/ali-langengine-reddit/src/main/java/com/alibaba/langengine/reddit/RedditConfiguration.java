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
package com.alibaba.langengine.reddit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Data
@Slf4j
public class RedditConfiguration {

    /**
     * 默认User Agent
     */
    public static final String DEFAULT_USER_AGENT = "ali-langengine-reddit/1.0";

    /**
     * Reddit API基础URL
     */
    private String baseUrl = "https://www.reddit.com";

    /**
     * User Agent - Reddit API要求必须设置
     */
    private String userAgent = DEFAULT_USER_AGENT;

    /**
     * 超时时间（秒）
     */
    private int timeoutSeconds = 30;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 请求间隔（毫秒）- Reddit API有速率限制
     */
    private int requestIntervalMs = 1000;

    /**
     * 默认子论坛
     */
    private String defaultSubreddit = "all";

    /**
     * 默认排序方式
     */
    private String defaultSort = "hot";

    /**
     * 默认时间范围
     */
    private String defaultTimeRange = "day";

    /**
     * 构造函数
     */
    public RedditConfiguration() {
        // 从环境变量读取配置
        String envBaseUrl = System.getenv("REDDIT_BASE_URL");
        if (StringUtils.isNotBlank(envBaseUrl)) {
            this.baseUrl = envBaseUrl;
        }

        String envUserAgent = System.getenv("REDDIT_USER_AGENT");
        if (StringUtils.isNotBlank(envUserAgent)) {
            this.userAgent = envUserAgent;
        }

        String envTimeout = System.getenv("REDDIT_TIMEOUT_SECONDS");
        if (StringUtils.isNotBlank(envTimeout)) {
            try {
                this.timeoutSeconds = Integer.parseInt(envTimeout);
            } catch (NumberFormatException e) {
                log.warn("Invalid REDDIT_TIMEOUT_SECONDS: {}, using default: {}", envTimeout, this.timeoutSeconds);
            }
        }

        String envMaxRetries = System.getenv("REDDIT_MAX_RETRIES");
        if (StringUtils.isNotBlank(envMaxRetries)) {
            try {
                this.maxRetries = Integer.parseInt(envMaxRetries);
            } catch (NumberFormatException e) {
                log.warn("Invalid REDDIT_MAX_RETRIES: {}, using default: {}", envMaxRetries, this.maxRetries);
            }
        }

        String envInterval = System.getenv("REDDIT_REQUEST_INTERVAL_MS");
        if (StringUtils.isNotBlank(envInterval)) {
            try {
                this.requestIntervalMs = Integer.parseInt(envInterval);
            } catch (NumberFormatException e) {
                log.warn("Invalid REDDIT_REQUEST_INTERVAL_MS: {}, using default: {}", envInterval, this.requestIntervalMs);
            }
        }
    }

    /**
     * 验证配置是否有效
     *
     * @return 验证结果
     */
    public boolean validateConfiguration() {
        try {
            if (StringUtils.isBlank(baseUrl)) {
                log.error("Reddit base URL cannot be empty");
                return false;
            }

            if (StringUtils.isBlank(userAgent)) {
                log.error("Reddit User-Agent cannot be empty");
                return false;
            }

            if (timeoutSeconds <= 0) {
                log.error("Reddit timeout must be positive, got: {}", timeoutSeconds);
                return false;
            }

            if (maxRetries < 0) {
                log.error("Reddit max retries cannot be negative, got: {}", maxRetries);
                return false;
            }

            if (requestIntervalMs < 0) {
                log.error("Reddit request interval cannot be negative, got: {}", requestIntervalMs);
                return false;
            }

            log.info("Reddit configuration validation passed");
            return true;
        } catch (Exception e) {
            log.error("Reddit configuration validation failed", e);
            return false;
        }
    }

    /**
     * 获取配置摘要信息
     *
     * @return 配置摘要
     */
    public String getConfigurationSummary() {
        return String.format("Reddit Configuration: baseUrl=%s, userAgent=%s, timeout=%ds, maxRetries=%d, interval=%dms",
                baseUrl, userAgent, timeoutSeconds, maxRetries, requestIntervalMs);
    }

    /**
     * 检查是否有有效的User-Agent设置
     *
     * @return true如果User-Agent有效
     */
    public boolean hasValidUserAgent() {
        return StringUtils.isNotBlank(userAgent) && !userAgent.equals(DEFAULT_USER_AGENT);
    }
}
