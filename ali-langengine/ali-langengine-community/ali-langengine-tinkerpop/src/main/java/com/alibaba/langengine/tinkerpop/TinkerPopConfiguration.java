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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class TinkerPopConfiguration {

    // Public static constants for backward compatibility with tests
    public static final String TINKERPOP_SERVER_URL = getConfigValue("tinkerpop_server_url", "ws://localhost:8182/gremlin");
    public static final String TINKERPOP_CONNECTION_TIMEOUT = getConfigValue("tinkerpop_connection_timeout", "30000");
    public static final String TINKERPOP_REQUEST_TIMEOUT = getConfigValue("tinkerpop_request_timeout", "60000");

    private static volatile boolean initialized = false;

    /**
     * Get configuration value with optional default
     */
    private static String getConfigValue(String key, String defaultValue) {
        return WorkPropertiesUtils.get(key, defaultValue);
    }

    /**
     * Initialize configuration with validation (called on first access)
     */
    private static synchronized void initialize() {
        if (!initialized) {
            validateConfiguration();
            initialized = true;
        }
    }

    /**
     * Validate required configuration parameters
     */
    private static void validateConfiguration() {
        if (StringUtils.isBlank(TINKERPOP_SERVER_URL)) {
            log.warn("TinkerPop server URL is not configured. Using default: ws://localhost:8182/gremlin");
        }
        
        try {
            int connectionTimeout = Integer.parseInt(TINKERPOP_CONNECTION_TIMEOUT);
            int requestTimeout = Integer.parseInt(TINKERPOP_REQUEST_TIMEOUT);
            
            if (connectionTimeout <= 0) {
                log.warn("TinkerPop connection timeout must be positive, got: {}. Using default: 30000", connectionTimeout);
            }
            if (requestTimeout <= 0) {
                log.warn("TinkerPop request timeout must be positive, got: {}. Using default: 60000", requestTimeout);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid timeout configuration: connection={}, request={}. Using defaults.", 
                    TINKERPOP_CONNECTION_TIMEOUT, TINKERPOP_REQUEST_TIMEOUT);
        }
        
        log.info("TinkerPop configuration loaded - Server: {}, Connection timeout: {}, Request timeout: {}", 
                TINKERPOP_SERVER_URL, TINKERPOP_CONNECTION_TIMEOUT, TINKERPOP_REQUEST_TIMEOUT);
    }

    /**
     * Get TinkerPop server URL
     */
    public static String getTinkerPopServerUrl() {
        if (!initialized) {
            initialize();
        }
        return TINKERPOP_SERVER_URL;
    }

    /**
     * Get TinkerPop connection timeout
     */
    public static int getTinkerPopConnectionTimeout() {
        if (!initialized) {
            initialize();
        }
        try {
            return Integer.parseInt(TINKERPOP_CONNECTION_TIMEOUT);
        } catch (NumberFormatException e) {
            log.warn("Invalid connection timeout: {}, using default: 30000", TINKERPOP_CONNECTION_TIMEOUT);
            return 30000;
        }
    }

    /**
     * Get TinkerPop request timeout
     */
    public static int getTinkerPopRequestTimeout() {
        if (!initialized) {
            initialize();
        }
        try {
            return Integer.parseInt(TINKERPOP_REQUEST_TIMEOUT);
        } catch (NumberFormatException e) {
            log.warn("Invalid request timeout: {}, using default: 60000", TINKERPOP_REQUEST_TIMEOUT);
            return 60000;
        }
    }

}