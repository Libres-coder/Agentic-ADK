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
package com.alibaba.langengine.elasticsearch;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class ElasticsearchConfiguration {

    /**
     * Elasticsearch server URL (e.g., http://localhost:9200)
     */
    public static String ELASTICSEARCH_SERVER_URL = WorkPropertiesUtils.get("elasticsearch_server_url", "http://localhost:9200");

    /**
     * Elasticsearch username for authentication
     */
    public static String ELASTICSEARCH_USERNAME = WorkPropertiesUtils.get("elasticsearch_username");

    /**
     * Elasticsearch password for authentication
     */
    public static String ELASTICSEARCH_PASSWORD = WorkPropertiesUtils.get("elasticsearch_password");

    /**
     * Elasticsearch API key for authentication
     */
    public static String ELASTICSEARCH_API_KEY = WorkPropertiesUtils.get("elasticsearch_api_key");

    /**
     * Connection timeout in milliseconds
     */
    public static int ELASTICSEARCH_CONNECTION_TIMEOUT = Integer.parseInt(
            WorkPropertiesUtils.get("elasticsearch_connection_timeout", "30000"));

    /**
     * Socket timeout in milliseconds
     */
    public static int ELASTICSEARCH_SOCKET_TIMEOUT = Integer.parseInt(
            WorkPropertiesUtils.get("elasticsearch_socket_timeout", "30000"));
}
