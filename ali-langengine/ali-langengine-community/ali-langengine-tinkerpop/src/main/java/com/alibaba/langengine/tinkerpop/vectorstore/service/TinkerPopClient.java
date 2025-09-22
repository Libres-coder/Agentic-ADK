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
package com.alibaba.langengine.tinkerpop.vectorstore.service;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
public class TinkerPopClient {

    /**
     * Gremlin server URL
     */
    private String serverUrl;

    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeout;

    /**
     * Request timeout in milliseconds
     */
    private int requestTimeout;

    public TinkerPopClient(String serverUrl) {
        this(serverUrl, 30000, 60000); // 30s connection, 60s request timeout defaults
    }

    public TinkerPopClient(String serverUrl, int connectionTimeout, int requestTimeout) {
        if (StringUtils.isBlank(serverUrl)) {
            throw new IllegalArgumentException("Server URL cannot be null or empty");
        }
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("Connection timeout must be positive: " + connectionTimeout);
        }
        if (requestTimeout <= 0) {
            throw new IllegalArgumentException("Request timeout must be positive: " + requestTimeout);
        }
        
        this.serverUrl = serverUrl;
        this.connectionTimeout = connectionTimeout;
        this.requestTimeout = requestTimeout;
    }

}