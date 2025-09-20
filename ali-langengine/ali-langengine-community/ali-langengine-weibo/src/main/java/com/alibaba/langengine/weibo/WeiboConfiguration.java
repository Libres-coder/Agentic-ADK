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
package com.alibaba.langengine.weibo;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class WeiboConfiguration {
    
    /**
     * 微博 App Key
     */
    public static String WEIBO_APP_KEY = WorkPropertiesUtils.get("weibo_app_key");
    
    /**
     * 微博 App Secret
     */
    public static String WEIBO_APP_SECRET = WorkPropertiesUtils.get("weibo_app_secret");
    
    /**
     * 微博 Access Token
     */
    public static String WEIBO_ACCESS_TOKEN = WorkPropertiesUtils.get("weibo_access_token");
    
    /**
     * 微博 API base URL
     */
    public static String WEIBO_API_URL = WorkPropertiesUtils.get("weibo_api_url", "https://api.weibo.com/2");
}