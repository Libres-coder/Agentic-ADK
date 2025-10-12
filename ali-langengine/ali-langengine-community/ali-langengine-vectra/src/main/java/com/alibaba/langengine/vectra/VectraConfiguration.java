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
package com.alibaba.langengine.vectra;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class VectraConfiguration {

    /**
     * Tencent Cloud VectorDB server URL
     */
    public static String VECTRA_SERVER_URL = WorkPropertiesUtils.get("vectra_server_url", "http://localhost:30000");

    /**
     * Tencent Cloud VectorDB API key
     */
    public static String VECTRA_API_KEY = WorkPropertiesUtils.get("vectra_api_key");

    /**
     * Database name
     */
    public static String VECTRA_DATABASE_NAME = WorkPropertiesUtils.get("vectra_database_name", "default_database");

    /**
     * Connection timeout in seconds
     */
    public static int VECTRA_CONNECT_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("vectra_connect_timeout", "30"));

    /**
     * Read timeout in seconds
     */
    public static int VECTRA_READ_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("vectra_read_timeout", "60"));

    /**
     * Write timeout in seconds
     */
    public static int VECTRA_WRITE_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("vectra_write_timeout", "60"));
}