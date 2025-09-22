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
package com.alibaba.langengine.rockset;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class RocksetConfiguration {

    /**
     * Rockset服务器地址
     */
    public static String ROCKSET_SERVER_URL = WorkPropertiesUtils.get("rockset_server_url", "https://api.usw2a1.rockset.com");

    /**
     * Rockset API密钥
     */
    public static String ROCKSET_API_KEY = WorkPropertiesUtils.get("rockset_api_key");

    /**
     * 默认工作空间
     */
    public static String ROCKSET_DEFAULT_WORKSPACE = WorkPropertiesUtils.get("rockset_default_workspace", "commons");
}
