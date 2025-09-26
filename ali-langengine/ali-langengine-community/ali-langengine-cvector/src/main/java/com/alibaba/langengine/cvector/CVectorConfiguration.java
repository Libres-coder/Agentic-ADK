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
package com.alibaba.langengine.cvector;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class CVectorConfiguration {

    /**
     * cVector server URL
     */
    public static String CVECTOR_SERVER_URL = getConfigValue("cvector_server_url", "http://localhost:8080");

    /**
     * cVector API key
     */
    public static String CVECTOR_API_KEY = getConfigValue("cvector_api_key", "");

    /**
     * cVector database name
     */
    public static String CVECTOR_DATABASE = getConfigValue("cvector_database", "default");

    private static String getConfigValue(String key, String defaultValue) {
        String value = WorkPropertiesUtils.get(key);
        return value != null ? value : defaultValue;
    }
}