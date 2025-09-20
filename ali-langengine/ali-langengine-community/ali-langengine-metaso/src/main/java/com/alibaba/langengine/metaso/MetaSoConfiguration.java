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
package com.alibaba.langengine.metaso;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import com.alibaba.langengine.metaso.sdk.MetaSoConstant;

public class MetaSoConfiguration {
    /**
     * MetaSo API key for authentication
     */
    public static String METASO_API_KEY = WorkPropertiesUtils.get("metaso_api_key");

    /**
     * MetaSo API base URL, defaults to the constant DEFAULT_BASE_URL if not configured
     */
    public static String METASO_API_URL = WorkPropertiesUtils.get("metaso_api_url", MetaSoConstant.DEFAULT_BASE_URL);
    
    /**
     * MetaSo API connect timeout in milliseconds
     */
    public static Long METASO_CONNECT_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("metaso_connect_timeout", "30000"));
    
    /**
     * MetaSo API read timeout in milliseconds
     */
    public static Long METASO_READ_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("metaso_read_timeout", "30000"));
    
    /**
     * MetaSo API write timeout in milliseconds
     */
    public static Long METASO_WRITE_TIMEOUT = Long.valueOf(WorkPropertiesUtils.get("metaso_write_timeout", "30000"));
}