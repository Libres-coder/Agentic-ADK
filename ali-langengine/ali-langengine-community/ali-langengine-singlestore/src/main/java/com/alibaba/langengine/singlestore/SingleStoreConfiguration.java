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
package com.alibaba.langengine.singlestore;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


@Slf4j
public class SingleStoreConfiguration {

    // Public static constants for backward compatibility with tests
    public static final String SINGLESTORE_SERVER_URL = getConfigValue("singlestore_server_url", "localhost:3306");
    public static final String SINGLESTORE_DATABASE = getConfigValue("singlestore_database", "vectordb");
    public static final String SINGLESTORE_USERNAME = getConfigValue("singlestore_username");
    public static final String SINGLESTORE_PASSWORD = getConfigValue("singlestore_password");

    private static volatile boolean initialized = false;

    /**
     * Get configuration value with optional default
     */
    private static String getConfigValue(String key, String defaultValue) {
        String value = WorkPropertiesUtils.get(key, defaultValue);
        return value;
    }

    /**
     * Get configuration value without default
     */
    private static String getConfigValue(String key) {
        return WorkPropertiesUtils.get(key);
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
        if (StringUtils.isBlank(SINGLESTORE_SERVER_URL)) {
            log.warn("SingleStore server URL is not configured. Using default: localhost:3306");
        }
        if (StringUtils.isBlank(SINGLESTORE_USERNAME)) {
            log.warn("SingleStore username is not configured. Please set 'singlestore_username' property.");
        }
        if (StringUtils.isBlank(SINGLESTORE_PASSWORD)) {
            log.warn("SingleStore password is not configured. This may cause authentication failures.");
        }
        log.info("SingleStore configuration loaded - Server: {}, Database: {}, Username: {}", 
                SINGLESTORE_SERVER_URL, SINGLESTORE_DATABASE, 
                StringUtils.isBlank(SINGLESTORE_USERNAME) ? "[NOT_SET]" : SINGLESTORE_USERNAME);
    }

    /**
     * Get SingleStore server URL
     */
    public static String getSingleStoreServerUrl() {
        if (!initialized) {
            initialize();
        }
        return SINGLESTORE_SERVER_URL;
    }

    /**
     * Get SingleStore database name
     */
    public static String getSingleStoreDatabase() {
        if (!initialized) {
            initialize();
        }
        return SINGLESTORE_DATABASE;
    }

    /**
     * Get SingleStore username
     */
    public static String getSingleStoreUsername() {
        if (!initialized) {
            initialize();
        }
        return SINGLESTORE_USERNAME;
    }

    /**
     * Get SingleStore password (should not be logged)
     */
    public static String getSingleStorePassword() {
        if (!initialized) {
            initialize();
        }
        return SINGLESTORE_PASSWORD;
    }

    /**
     * Get masked password for logging purposes
     */
    public static String getMaskedPassword() {
        if (!initialized) {
            initialize();
        }
        return StringUtils.isBlank(SINGLESTORE_PASSWORD) ? "[NOT_SET]" : "****";
    }

}