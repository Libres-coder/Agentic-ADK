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
package com.alibaba.langengine.rockset.vectorstore;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import org.apache.commons.lang3.StringUtils;


public class RocksetConfiguration {

    private static final String ROCKSET_SERVER_URL = "ROCKSET_SERVER_URL";
    private static final String ROCKSET_API_KEY = "ROCKSET_API_KEY";
    private static final String ROCKSET_DEFAULT_WORKSPACE = "ROCKSET_DEFAULT_WORKSPACE";

    public static String getServerUrl() {
        String serverUrl = WorkPropertiesUtils.get(ROCKSET_SERVER_URL);
        if (StringUtils.isBlank(serverUrl)) {
            serverUrl = "https://api.use1a1.rockset.com";
        }
        return serverUrl;
    }

    public static String getApiKey() {
        return WorkPropertiesUtils.get(ROCKSET_API_KEY);
    }

    public static String getDefaultWorkspace() {
        String workspace = WorkPropertiesUtils.get(ROCKSET_DEFAULT_WORKSPACE);
        if (StringUtils.isBlank(workspace)) {
            workspace = "commons";
        }
        return workspace;
    }
}
