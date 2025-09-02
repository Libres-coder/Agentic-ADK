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
package com.alibaba.langengine.twitter;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class TwitterConfiguration {
    
    /**
     * Twitter API Bearer Token
     */
    public static String TWITTER_BEARER_TOKEN = WorkPropertiesUtils.get("twitter_bearer_token");
    
    /**
     * Twitter API Key
     */
    public static String TWITTER_API_KEY = WorkPropertiesUtils.get("twitter_api_key");
    
    /**
     * Twitter API Secret Key
     */
    public static String TWITTER_API_SECRET = WorkPropertiesUtils.get("twitter_api_secret");
    
    /**
     * Twitter Access Token
     */
    public static String TWITTER_ACCESS_TOKEN = WorkPropertiesUtils.get("twitter_access_token");
    
    /**
     * Twitter Access Token Secret
     */
    public static String TWITTER_ACCESS_TOKEN_SECRET = WorkPropertiesUtils.get("twitter_access_token_secret");
    
    /**
     * Twitter API base URL
     */
    public static String TWITTER_API_URL = WorkPropertiesUtils.get("twitter_api_url", "https://api.twitter.com/2");
}