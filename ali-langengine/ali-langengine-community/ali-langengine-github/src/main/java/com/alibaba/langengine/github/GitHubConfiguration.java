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
package com.alibaba.langengine.github;


public class GitHubConfiguration {

    /**
     * GitHub API Base URL
     */
    public static final String GITHUB_API_URL = getEnvOrDefault("GITHUB_API_URL", "https://api.github.com");

    /**
     * GitHub API Token
     */
    public static final String GITHUB_API_TOKEN = System.getenv("GITHUB_API_TOKEN");

    /**
     * GitHub Search API URL
     */
    public static final String GITHUB_SEARCH_API_URL = GITHUB_API_URL + "/search";

    /**
     * 默认超时时间(秒)
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /**
     * 默认每页结果数
     */
    public static final int DEFAULT_PER_PAGE = 30;

    /**
     * 最大每页结果数
     */
    public static final int MAX_PER_PAGE = 100;

    /**
     * 获取环境变量值，如果不存在则返回默认值
     *
     * @param key 环境变量键
     * @param defaultValue 默认值
     * @return 环境变量值或默认值
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取整数类型的环境变量值，如果不存在或无法解析则返回默认值
     *
     * @param key 环境变量键
     * @param defaultValue 默认值
     * @return 环境变量值或默认值
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
     * 验证GitHub API Token是否存在
     *
     * @return 如果token存在且不为空返回true，否则返回false
     */
    public static boolean hasValidToken() {
        return GITHUB_API_TOKEN != null && !GITHUB_API_TOKEN.trim().isEmpty();
    }

    /**
     * 验证配置是否完整
     *
     * @throws IllegalStateException 如果配置不完整
     */
    public static void validateConfiguration() {
        if (!hasValidToken()) {
            throw new IllegalStateException(
                "GitHub API Token is not configured. Please set GITHUB_API_TOKEN environment variable.");
        }
        
        if (GITHUB_API_URL == null || GITHUB_API_URL.trim().isEmpty()) {
            throw new IllegalStateException("GitHub API URL is not configured.");
        }
    }

    /**
     * 获取配置信息摘要（不包含敏感信息）
     *
     * @return 配置摘要字符串
     */
    public static String getConfigurationSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("GitHub Configuration:\n");
        summary.append("  API URL: ").append(GITHUB_API_URL).append("\n");
        summary.append("  Token configured: ").append(hasValidToken() ? "Yes" : "No").append("\n");
        summary.append("  Search API URL: ").append(GITHUB_SEARCH_API_URL).append("\n");
        summary.append("  Default timeout: ").append(DEFAULT_TIMEOUT_SECONDS).append("s\n");
        summary.append("  Default per page: ").append(DEFAULT_PER_PAGE).append("\n");
        summary.append("  Max per page: ").append(MAX_PER_PAGE);
        return summary.toString();
    }
}
