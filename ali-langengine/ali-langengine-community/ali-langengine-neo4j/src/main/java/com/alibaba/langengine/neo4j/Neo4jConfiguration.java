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
package com.alibaba.langengine.neo4j;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;


public class Neo4jConfiguration {

    /**
     * Neo4j server URI (bolt://localhost:7687)
     */
    public static String NEO4J_URI = WorkPropertiesUtils.get("neo4j_uri");

    /**
     * Neo4j username
     */
    public static String NEO4J_USERNAME = WorkPropertiesUtils.get("neo4j_username");

    /**
     * Neo4j password
     */
    public static String NEO4J_PASSWORD = WorkPropertiesUtils.get("neo4j_password");

    /**
     * Neo4j database name (default: neo4j)
     */
    public static String NEO4J_DATABASE = WorkPropertiesUtils.get("neo4j_database");

    /**
     * Connection timeout in seconds
     */
    public static String NEO4J_CONNECTION_TIMEOUT = WorkPropertiesUtils.get("neo4j_connection_timeout");

    /**
     * Max connection pool size
     */
    public static String NEO4J_MAX_CONNECTION_POOL_SIZE = WorkPropertiesUtils.get("neo4j_max_connection_pool_size");

}
