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
package com.alibaba.langengine.slack;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlackConfiguration {

    /**
     * Slack Bot Token环境变量名
     */
    public static final String SLACK_BOT_TOKEN_ENV = "SLACK_BOT_TOKEN";

    /**
     * Slack App Token环境变量名（用于Socket Mode）
     */
    public static final String SLACK_APP_TOKEN_ENV = "SLACK_APP_TOKEN";

    /**
     * Slack Signing Secret环境变量名
     */
    public static final String SLACK_SIGNING_SECRET_ENV = "SLACK_SIGNING_SECRET";

    /**
     * Slack Client ID环境变量名
     */
    public static final String SLACK_CLIENT_ID_ENV = "SLACK_CLIENT_ID";

    /**
     * Slack Client Secret环境变量名
     */
    public static final String SLACK_CLIENT_SECRET_ENV = "SLACK_CLIENT_SECRET";

    /**
     * 配置文件中的Slack Bot Token键名
     */
    public static final String SLACK_BOT_TOKEN_KEY = "slack.bot.token";

    /**
     * 配置文件中的Slack App Token键名
     */
    public static final String SLACK_APP_TOKEN_KEY = "slack.app.token";

    /**
     * 配置文件中的Slack Signing Secret键名
     */
    public static final String SLACK_SIGNING_SECRET_KEY = "slack.signing.secret";

    /**
     * 配置文件中的Slack Client ID键名
     */
    public static final String SLACK_CLIENT_ID_KEY = "slack.client.id";

    /**
     * 配置文件中的Slack Client Secret键名
     */
    public static final String SLACK_CLIENT_SECRET_KEY = "slack.client.secret";

    /**
     * 默认API超时时间（秒）
     */
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;

    /**
     * 默认重连间隔（毫秒）
     */
    public static final long DEFAULT_RECONNECT_INTERVAL_MS = 5000;

    /**
     * 默认最大重连次数
     */
    public static final int DEFAULT_MAX_RECONNECT_ATTEMPTS = 5;

    /**
     * Socket Mode 默认端点
     */
    public static final String SLACK_SOCKET_MODE_ENDPOINT = "wss://wss-primary.slack.com/";

    /**
     * Slack API 基础URL
     */
    public static final String SLACK_API_BASE_URL = "https://slack.com/api/";

    /**
     * 获取Slack Bot Token
     *
     * @return Bot Token
     */
    public static String getSlackBotToken() {
        String token = WorkPropertiesUtils.get(SLACK_BOT_TOKEN_KEY);
        if (token == null || token.trim().isEmpty()) {
            token = System.getenv(SLACK_BOT_TOKEN_ENV);
        }
        if (token == null || token.trim().isEmpty()) {
            log.warn("Slack Bot Token not configured. Please set {} in properties or {} environment variable", 
                    SLACK_BOT_TOKEN_KEY, SLACK_BOT_TOKEN_ENV);
        }
        return token;
    }

    /**
     * 获取Slack App Token（用于Socket Mode）
     *
     * @return App Token
     */
    public static String getSlackAppToken() {
        String token = WorkPropertiesUtils.get(SLACK_APP_TOKEN_KEY);
        if (token == null || token.trim().isEmpty()) {
            token = System.getenv(SLACK_APP_TOKEN_ENV);
        }
        if (token == null || token.trim().isEmpty()) {
            log.warn("Slack App Token not configured. Please set {} in properties or {} environment variable", 
                    SLACK_APP_TOKEN_KEY, SLACK_APP_TOKEN_ENV);
        }
        return token;
    }

    /**
     * 获取Slack Signing Secret
     *
     * @return Signing Secret
     */
    public static String getSlackSigningSecret() {
        String secret = WorkPropertiesUtils.get(SLACK_SIGNING_SECRET_KEY);
        if (secret == null || secret.trim().isEmpty()) {
            secret = System.getenv(SLACK_SIGNING_SECRET_ENV);
        }
        if (secret == null || secret.trim().isEmpty()) {
            log.warn("Slack Signing Secret not configured. Please set {} in properties or {} environment variable", 
                    SLACK_SIGNING_SECRET_KEY, SLACK_SIGNING_SECRET_ENV);
        }
        return secret;
    }

    /**
     * 获取Slack Client ID
     *
     * @return Client ID
     */
    public static String getSlackClientId() {
        String clientId = WorkPropertiesUtils.get(SLACK_CLIENT_ID_KEY);
        if (clientId == null || clientId.trim().isEmpty()) {
            clientId = System.getenv(SLACK_CLIENT_ID_ENV);
        }
        return clientId;
    }

    /**
     * 获取Slack Client Secret
     *
     * @return Client Secret
     */
    public static String getSlackClientSecret() {
        String clientSecret = WorkPropertiesUtils.get(SLACK_CLIENT_SECRET_KEY);
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            clientSecret = System.getenv(SLACK_CLIENT_SECRET_ENV);
        }
        return clientSecret;
    }

    /**
     * 获取API超时时间
     *
     * @return 超时时间（秒）
     */
    public static int getApiTimeout() {
        String timeout = WorkPropertiesUtils.get("slack.api.timeout");
        try {
            return timeout != null ? Integer.parseInt(timeout) : DEFAULT_TIMEOUT_SECONDS;
        } catch (NumberFormatException e) {
            log.warn("Invalid timeout value: {}, using default: {}", timeout, DEFAULT_TIMEOUT_SECONDS);
            return DEFAULT_TIMEOUT_SECONDS;
        }
    }

    /**
     * 获取重连间隔
     *
     * @return 重连间隔（毫秒）
     */
    public static long getReconnectInterval() {
        String interval = WorkPropertiesUtils.get("slack.reconnect.interval");
        try {
            return interval != null ? Long.parseLong(interval) : DEFAULT_RECONNECT_INTERVAL_MS;
        } catch (NumberFormatException e) {
            log.warn("Invalid reconnect interval value: {}, using default: {}", interval, DEFAULT_RECONNECT_INTERVAL_MS);
            return DEFAULT_RECONNECT_INTERVAL_MS;
        }
    }

    /**
     * 获取最大重连次数
     *
     * @return 最大重连次数
     */
    public static int getMaxReconnectAttempts() {
        String attempts = WorkPropertiesUtils.get("slack.reconnect.max.attempts");
        try {
            return attempts != null ? Integer.parseInt(attempts) : DEFAULT_MAX_RECONNECT_ATTEMPTS;
        } catch (NumberFormatException e) {
            log.warn("Invalid max reconnect attempts value: {}, using default: {}", attempts, DEFAULT_MAX_RECONNECT_ATTEMPTS);
            return DEFAULT_MAX_RECONNECT_ATTEMPTS;
        }
    }

    /**
     * 检查Socket Mode是否启用
     *
     * @return 是否启用Socket Mode
     */
    public static boolean isSocketModeEnabled() {
        String enabled = WorkPropertiesUtils.get("slack.socket.mode.enabled");
        return "true".equalsIgnoreCase(enabled);
    }

    /**
     * 获取默认频道ID
     *
     * @return 默认频道ID
     */
    public static String getDefaultChannelId() {
        return WorkPropertiesUtils.get("slack.default.channel.id");
    }

    /**
     * 获取环境变量或默认值的辅助方法
     *
     * @param envName 环境变量名
     * @param defaultValue 默认值
     * @return 配置值
     */
    private static String getEnvOrDefault(String envName, String defaultValue) {
        String value = System.getenv(envName);
        return value != null ? value : defaultValue;
    }
}
