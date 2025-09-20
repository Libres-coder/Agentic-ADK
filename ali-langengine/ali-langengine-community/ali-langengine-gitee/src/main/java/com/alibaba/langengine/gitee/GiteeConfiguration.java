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

package com.alibaba.langengine.gitee;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GiteeConfiguration {

    /**
     * Gitee API Base URL
     */
    public static final String GITEE_API_URL = getEnvOrDefault("GITEE_API_URL", "https://gitee.com/api/v5");

    /**
     * Gitee API Token - read from environment variable first, then from config file
     */
    @Value("${ali.langengine.community.gitee.access_token:}")
    private String configAccessToken;
    
    private static GiteeConfiguration instance;
    
    public GiteeConfiguration() {
        instance = this;
    }
    
    /**
     * Get Gitee Access Token
     * Priority: environment variable > config file
     */
    public static String getAccessToken() {
        // Try to get from environment variable first
        String envToken = System.getenv("GITEE_ACCESS_TOKEN");
        if (StringUtils.hasText(envToken)) {
            return envToken;
        }
        
        // If environment variable is not set, get from config file
        if (instance != null && StringUtils.hasText(instance.configAccessToken)) {
            return instance.configAccessToken;
        }
        
        return null;
    }
    
    /**
     * Backward compatibility: keep original static constant, but now calls new method
     * @deprecated Recommend using getAccessToken() method
     */
    @Deprecated
    public static final String GITEE_ACCESS_TOKEN = getAccessToken();

    /**
     * Gitee Search API URL
     */
    public static final String GITEE_SEARCH_API_URL = GITEE_API_URL + "/search";

    /**
     * Default timeout in seconds
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /**
     * Default number of results per page
     */
    public static final int DEFAULT_PER_PAGE = 20;

    /**
     * Maximum number of results per page
     */
    public static final int MAX_PER_PAGE = 100;

    /**
     * Get environment variable value, return default value if not exists
     *
     * @param key Environment variable key
     * @param defaultValue Default value
     * @return Environment variable value or default value
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Get integer environment variable value, return default value if not exists or cannot parse
     *
     * @param key Environment variable key
     * @param defaultValue Default value
     * @return Environment variable value or default value
     */
    public static int getIntEnvOrDefault(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Validate if Gitee Access Token exists
     *
     * @return true if token exists and is not empty, false otherwise
     */
    public static boolean hasValidToken() {
        String token = getAccessToken();
        return StringUtils.hasText(token);
    }

    /**
     * Validate if configuration is complete
     *
     * @throws IllegalStateException if configuration is incomplete
     */
    public static void validateConfiguration() {
        if (!hasValidToken()) {
            throw new IllegalStateException(
                "Gitee Access Token is not configured. " +
                "Please set GITEE_ACCESS_TOKEN environment variable or " +
                "configure ali.langengine.community.gitee.access_token in application.yml");
        }
        
        if (GITEE_API_URL == null || GITEE_API_URL.trim().isEmpty()) {
            throw new IllegalStateException("Gitee API URL is not configured.");
        }
    }

    /**
     * Get configuration summary (without sensitive information)
     *
     * @return Configuration summary string
     */
    public static String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Gitee Configuration:\n");
        summary.append("  API URL: ").append(GITEE_API_URL).append("\n");
        summary.append("  Token configured: ").append(hasValidToken() ? "Yes" : "No").append("\n");
        summary.append("  Search API URL: ").append(GITEE_SEARCH_API_URL).append("\n");
        summary.append("  Default timeout: ").append(DEFAULT_TIMEOUT_SECONDS).append("s\n");
        summary.append("  Default per page: ").append(DEFAULT_PER_PAGE).append("\n");
        summary.append("  Max per page: ").append(MAX_PER_PAGE);
        return summary.toString();
    }
}