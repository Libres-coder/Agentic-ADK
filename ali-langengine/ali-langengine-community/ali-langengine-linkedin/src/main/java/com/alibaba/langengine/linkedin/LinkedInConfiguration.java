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
package com.alibaba.langengine.linkedin;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class LinkedInConfiguration {
    
    /**
     * LinkedIn Client ID
     */
    public static String LINKEDIN_CLIENT_ID = WorkPropertiesUtils.get("linkedin_client_id");
    
    /**
     * LinkedIn Client Secret
     */
    public static String LINKEDIN_CLIENT_SECRET = WorkPropertiesUtils.get("linkedin_client_secret");
    
    /**
     * LinkedIn Access Token
     */
    public static String LINKEDIN_ACCESS_TOKEN = WorkPropertiesUtils.get("linkedin_access_token");
    
    /**
     * LinkedIn API base URL
     */
    public static String LINKEDIN_API_URL = WorkPropertiesUtils.get("linkedin_api_url", "https://api.linkedin.com/v2");
}