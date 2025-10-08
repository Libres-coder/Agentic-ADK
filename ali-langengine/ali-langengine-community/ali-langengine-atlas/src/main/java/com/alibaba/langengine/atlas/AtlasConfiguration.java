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
package com.alibaba.langengine.atlas;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class AtlasConfiguration {

    /**
     * Atlas connection string
     */
    public static String ATLAS_CONNECTION_STRING = WorkPropertiesUtils.get("atlas_connection_string", "mongodb://localhost:27017");

    /**
     * Atlas database name
     */
    public static String ATLAS_DATABASE_NAME = WorkPropertiesUtils.get("atlas_database_name", "vector_db");

    /**
     * Default connection timeout in milliseconds
     */
    public static int ATLAS_CONNECTION_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("atlas_connection_timeout", "30000"));

    /**
     * Default socket timeout in milliseconds
     */
    public static int ATLAS_SOCKET_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("atlas_socket_timeout", "30000"));

}