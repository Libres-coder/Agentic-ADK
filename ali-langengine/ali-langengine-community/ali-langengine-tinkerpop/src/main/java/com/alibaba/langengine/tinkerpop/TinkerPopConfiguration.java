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
package com.alibaba.langengine.tinkerpop;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class TinkerPopConfiguration {

    /**
     * TinkerPop Gremlin server url
     */
    public static String TINKERPOP_SERVER_URL = WorkPropertiesUtils.get("tinkerpop_server_url");

    /**
     * TinkerPop connection timeout
     */
    public static String TINKERPOP_CONNECTION_TIMEOUT = WorkPropertiesUtils.get("tinkerpop_connection_timeout");

    /**
     * TinkerPop request timeout
     */
    public static String TINKERPOP_REQUEST_TIMEOUT = WorkPropertiesUtils.get("tinkerpop_request_timeout");

}