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
package com.alibaba.langengine.pubmed;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Data
@Slf4j
public class PubMedConfiguration {

    /**
     * 默认User Agent
     */
    public static final String DEFAULT_USER_AGENT = "ali-langengine-pubmed/1.0";

    /**
     * PubMed ESearch API基础URL
     */
    private String eSearchBaseUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";

    /**
     * PubMed EFetch API基础URL
     */
    private String eFetchBaseUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";

    /**
     * User Agent - 推荐设置用于API访问
     */
    private String userAgent = DEFAULT_USER_AGENT;

    /**
     * 邮箱地址（PubMed API推荐设置）
     */
    private String email;

    /**
     * API Key（可选，有助于提高请求频率限制）
     */
    private String apiKey;

    /**
     * 超时时间（秒）
     */
    private int timeoutSeconds = 30;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 请求间隔（毫秒）- PubMed API有速率限制
     */
    private int requestIntervalMs = 1000;

    /**
     * 默认数据库
     */
    private String defaultDatabase = "pubmed";

    /**
     * 默认返回类型
     */
    private String defaultReturnType = "xml";

    /**
     * 默认返回模式
     */
    private String defaultReturnMode = "xml";

    /**
     * 构造函数
     */
    public PubMedConfiguration() {
        // 从环境变量读取配置
        String envESearchUrl = System.getenv("PUBMED_ESEARCH_BASE_URL");
        if (StringUtils.isNotBlank(envESearchUrl)) {
            this.eSearchBaseUrl = envESearchUrl;
        }

        String envEFetchUrl = System.getenv("PUBMED_EFETCH_BASE_URL");
        if (StringUtils.isNotBlank(envEFetchUrl)) {
            this.eFetchBaseUrl = envEFetchUrl;
        }

        String envUserAgent = System.getenv("PUBMED_USER_AGENT");
        if (StringUtils.isNotBlank(envUserAgent)) {
            this.userAgent = envUserAgent;
        }

        String envEmail = System.getenv("PUBMED_EMAIL");
        if (StringUtils.isNotBlank(envEmail)) {
            this.email = envEmail;
        }

        String envApiKey = System.getenv("PUBMED_API_KEY");
        if (StringUtils.isNotBlank(envApiKey)) {
            this.apiKey = envApiKey;
        }

        String envTimeout = System.getenv("PUBMED_TIMEOUT_SECONDS");
        if (StringUtils.isNotBlank(envTimeout)) {
            try {
                this.timeoutSeconds = Integer.parseInt(envTimeout);
            } catch (NumberFormatException e) {
                log.warn("Invalid PUBMED_TIMEOUT_SECONDS: {}, using default: {}", envTimeout, this.timeoutSeconds);
            }
        }

        String envMaxRetries = System.getenv("PUBMED_MAX_RETRIES");
        if (StringUtils.isNotBlank(envMaxRetries)) {
            try {
                this.maxRetries = Integer.parseInt(envMaxRetries);
            } catch (NumberFormatException e) {
                log.warn("Invalid PUBMED_MAX_RETRIES: {}, using default: {}", envMaxRetries, this.maxRetries);
            }
        }

        String envInterval = System.getenv("PUBMED_REQUEST_INTERVAL_MS");
        if (StringUtils.isNotBlank(envInterval)) {
            try {
                this.requestIntervalMs = Integer.parseInt(envInterval);
            } catch (NumberFormatException e) {
                log.warn("Invalid PUBMED_REQUEST_INTERVAL_MS: {}, using default: {}", envInterval, this.requestIntervalMs);
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
            if (StringUtils.isBlank(eSearchBaseUrl)) {
                log.error("PubMed ESearch base URL cannot be empty");
                return false;
            }

            if (StringUtils.isBlank(eFetchBaseUrl)) {
                log.error("PubMed EFetch base URL cannot be empty");
                return false;
            }

            if (StringUtils.isBlank(userAgent)) {
                log.error("PubMed User-Agent cannot be empty");
                return false;
            }

            if (timeoutSeconds <= 0) {
                log.error("PubMed timeout must be positive, got: {}", timeoutSeconds);
                return false;
            }

            if (maxRetries < 0) {
                log.error("PubMed max retries cannot be negative, got: {}", maxRetries);
                return false;
            }

            if (requestIntervalMs < 0) {
                log.error("PubMed request interval cannot be negative, got: {}", requestIntervalMs);
                return false;
            }

            // 警告：推荐设置邮箱地址
            if (StringUtils.isBlank(email)) {
                log.warn("PubMed API recommends setting an email address for better service");
            }

            log.info("PubMed configuration validation passed");
            return true;
        } catch (Exception e) {
            log.error("PubMed configuration validation failed", e);
            return false;
        }
    }

    /**
     * 获取配置摘要信息
     *
     * @return 配置摘要
     */
    public String getConfigurationSummary() {
        return String.format("PubMed Configuration: eSearchUrl=%s, eFetchUrl=%s, userAgent=%s, email=%s, timeout=%ds, maxRetries=%d, interval=%dms",
                eSearchBaseUrl, eFetchBaseUrl, userAgent, 
                StringUtils.isNotBlank(email) ? email : "not set", 
                timeoutSeconds, maxRetries, requestIntervalMs);
    }

    /**
     * 检查是否有有效的User-Agent设置
     *
     * @return true如果User-Agent有效
     */
    public boolean hasValidUserAgent() {
        return StringUtils.isNotBlank(userAgent) && !userAgent.equals(DEFAULT_USER_AGENT);
    }

    /**
     * 检查是否设置了API Key
     *
     * @return true如果API Key有效
     */
    public boolean hasApiKey() {
        return StringUtils.isNotBlank(apiKey);
    }

    /**
     * 检查是否设置了邮箱地址
     *
     * @return true如果邮箱地址有效
     */
    public boolean hasEmail() {
        return StringUtils.isNotBlank(email);
    }
}
