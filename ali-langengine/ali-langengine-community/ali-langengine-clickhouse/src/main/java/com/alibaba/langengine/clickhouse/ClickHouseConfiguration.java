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
package com.alibaba.langengine.clickhouse;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class ClickHouseConfiguration {

    /**
     * ClickHouse JDBC URL
     */
    public static final String CLICKHOUSE_URL = WorkPropertiesUtils.get("clickhouse_url");

    /**
     * ClickHouse HTTP URL
     */
    public static final String CLICKHOUSE_HTTP_URL = WorkPropertiesUtils.get("clickhouse_http_url");

    /**
     * ClickHouse Username
     */
    public static final String CLICKHOUSE_USERNAME = WorkPropertiesUtils.get("clickhouse_username");

    /**
     * ClickHouse Password
     */
    public static final String CLICKHOUSE_PASSWORD = WorkPropertiesUtils.get("clickhouse_password");

    /**
     * ClickHouse Database
     */
    public static final String CLICKHOUSE_DATABASE = WorkPropertiesUtils.get("clickhouse_database");

    /**
     * ClickHouse Connection Timeout
     */
    public static final String CLICKHOUSE_CONNECTION_TIMEOUT = WorkPropertiesUtils.get("clickhouse_connection_timeout");

    /**
     * ClickHouse Query Timeout
     */
    public static final String CLICKHOUSE_QUERY_TIMEOUT = WorkPropertiesUtils.get("clickhouse_query_timeout");

    /**
     * ClickHouse Max Pool Size
     */
    public static final String CLICKHOUSE_MAX_POOL_SIZE = WorkPropertiesUtils.get("clickhouse_max_pool_size");
}
