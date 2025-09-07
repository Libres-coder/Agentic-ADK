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
package com.alibaba.langengine.wenxin;

import org.apache.commons.lang3.StringUtils;
import java.util.Optional;


public class WenxinConfiguration {

    /**
     * 文心一言 API Base URL
     */
    public static final String WENXIN_SERVER_URL = getConfigValue("wenxin_server_url", "WENXIN_SERVER_URL", "https://aip.baidubce.com/");

    /**
     * 安全地获取配置值，避免在静态常量中暴露敏感信息
     * @param systemProperty 系统属性名
     * @param envVar 环境变量名
     * @param defaultValue 默认值
     * @return 配置值
     */
    private static String getConfigValue(String systemProperty, String envVar, String defaultValue) {
        return Optional.ofNullable(System.getProperty(systemProperty))
                .filter(StringUtils::isNotEmpty)
                .or(() -> Optional.ofNullable(System.getenv(envVar))
                        .filter(StringUtils::isNotEmpty))
                .orElse(defaultValue);
    }

    /**
     * 安全地获取API密钥，不在静态常量中暴露
     * @return API密钥，如果未配置则返回null
     */
    public static String getApiKey() {
        return getConfigValue("wenxin_api_key", "WENXIN_API_KEY", null);
    }

    /**
     * 安全地获取Secret密钥，不在静态常量中暴露
     * @return Secret密钥，如果未配置则返回null
     */
    public static String getSecretKey() {
        return getConfigValue("wenxin_secret_key", "WENXIN_SECRET_KEY", null);
    }

    /**
     * 文心一言 API 超时时间
     */
    public static final String WENXIN_API_TIMEOUT = !StringUtils.isEmpty(System.getProperty("wenxin_api_timeout")) ? 
        System.getProperty("wenxin_api_timeout") : 
        (!StringUtils.isEmpty(System.getenv("WENXIN_API_TIMEOUT")) ? 
        System.getenv("WENXIN_API_TIMEOUT") : 
        "100");

    /**
     * 默认超时时间（秒）
     */
    public static final Long WENXIN_DEFAULT_TIMEOUT_SECONDS = Long.parseLong(WENXIN_API_TIMEOUT);

    /**
     * 文心一言访问令牌（可选，如果没有API Key和Secret Key的话）
     */
    public static final String WENXIN_ACCESS_TOKEN = !StringUtils.isEmpty(System.getProperty("wenxin_access_token")) ? 
        System.getProperty("wenxin_access_token") : 
        (!StringUtils.isEmpty(System.getenv("WENXIN_ACCESS_TOKEN")) ? 
        System.getenv("WENXIN_ACCESS_TOKEN") : 
        null);

    /**
     * 默认用户代理
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 WenxinLangEngine/1.0";

    private WenxinConfiguration() {
        // 工具类，不允许实例化
    }
}
