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
package com.alibaba.langengine.google;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

import static com.alibaba.langengine.google.sdk.GoogleConstant.DEFAULT_USER_AGENT;
import static com.alibaba.langengine.google.sdk.GoogleConstant.DEFAULT_TIMEOUT_SECONDS;

/**
 * Google搜索配置类
 * 提供可配置的用户代理和超时设置
 */
public class GoogleSearchConfiguration {
    
    /**
     * 可选的Google请求自定义用户代理
     */
    public static String GOOGLE_USER_AGENT = WorkPropertiesUtils.get("google_user_agent", DEFAULT_USER_AGENT);
    
    /**
     * 可选的Google请求超时时间（秒）
     */
    public static int GOOGLE_TIMEOUT_SECONDS = Integer.parseInt(
        WorkPropertiesUtils.get("google_timeout_seconds", String.valueOf(DEFAULT_TIMEOUT_SECONDS))
    );
    
    /**
     * 可选的Google搜索语言设置
     */
    public static String GOOGLE_LANGUAGE = WorkPropertiesUtils.get("google_language", "en");
    
    /**
     * 可选的Google搜索国家/地区设置
     */
    public static String GOOGLE_COUNTRY = WorkPropertiesUtils.get("google_country", "US");
} 