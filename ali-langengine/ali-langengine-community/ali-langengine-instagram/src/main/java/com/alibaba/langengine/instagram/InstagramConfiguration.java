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
package com.alibaba.langengine.instagram;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class InstagramConfiguration {
    
    /**
     * Instagram App ID
     */
    public static String INSTAGRAM_APP_ID = WorkPropertiesUtils.get("instagram_app_id");
    
    /**
     * Instagram App Secret
     */
    public static String INSTAGRAM_APP_SECRET = WorkPropertiesUtils.get("instagram_app_secret");
    
    /**
     * Instagram Access Token
     */
    public static String INSTAGRAM_ACCESS_TOKEN = WorkPropertiesUtils.get("instagram_access_token");
    
    /**
     * Instagram API base URL
     */
    public static String INSTAGRAM_API_URL = WorkPropertiesUtils.get("instagram_api_url", "https://graph.instagram.com");
}