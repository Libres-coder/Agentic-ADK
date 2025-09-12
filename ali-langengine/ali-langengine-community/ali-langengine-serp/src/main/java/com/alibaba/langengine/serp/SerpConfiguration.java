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
package com.alibaba.langengine.serp;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import static com.alibaba.langengine.serp.sdk.SerpConstant.BASE_URL;

public class SerpConfiguration {
    public static String SERP_API_KEY = WorkPropertiesUtils.get("serp_api_key");

    /**
     * SerpAPI base URL, defaults to the constant BASE_URL if not configured
     */
    public static String SERP_API_URL = WorkPropertiesUtils.get("serp_api_url", BASE_URL);
}