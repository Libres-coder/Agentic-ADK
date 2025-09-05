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


public class WenxinConfiguration {

    /**
     * 文心一言 API Base URL
     */
    public static final String WENXIN_SERVER_URL = !StringUtils.isEmpty(System.getProperty("wenxin_server_url")) ? 
        System.getProperty("wenxin_server_url") : 
        (!StringUtils.isEmpty(System.getenv("WENXIN_SERVER_URL")) ? 
        System.getenv("WENXIN_SERVER_URL") : 
        "https://aip.baidubce.com/");

    /**
     * 文心一言 API Key
     */
    public static final String WENXIN_API_KEY = !StringUtils.isEmpty(System.getProperty("wenxin_api_key")) ? 
        System.getProperty("wenxin_api_key") : 
        (!StringUtils.isEmpty(System.getenv("WENXIN_API_KEY")) ? 
        System.getenv("WENXIN_API_KEY") : 
        null);

    /**
     * 文心一言 Secret Key  
     */
    public static final String WENXIN_SECRET_KEY = !StringUtils.isEmpty(System.getProperty("wenxin_secret_key")) ? 
        System.getProperty("wenxin_secret_key") : 
        (!StringUtils.isEmpty(System.getenv("WENXIN_SECRET_KEY")) ? 
        System.getenv("WENXIN_SECRET_KEY") : 
        null);

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
