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
package com.alibaba.langengine.hugegraph.vectorstore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HugeGraphParamTest {

    @Test
    public void testParamBuilder() {
        HugeGraphParam param = HugeGraphParam.builder()
                .serverConfig(HugeGraphParam.ServerConfig.builder()
                        .host("localhost")
                        .port(8080)
                        .graph("hugegraph")
                        .username("admin")
                        .password("password")
                        .build())
                .vectorConfig(HugeGraphParam.VectorConfig.builder()
                        .vectorDimension(1536)
                        .vectorIndexName("vectorIndex")
                        .build())
                .connectionConfig(HugeGraphParam.ConnectionConfig.builder()
                        .connectionTimeout(60)
                        .connectionPoolSize(16)
                        .maxIdleConnections(16)
                        .build())
                .build();

        assertEquals("localhost", param.getServerConfig().getHost());
        assertEquals(8080, param.getServerConfig().getPort());
        assertEquals("hugegraph", param.getServerConfig().getGraph());
        assertEquals("admin", param.getServerConfig().getUsername());
        assertEquals("password", param.getServerConfig().getPassword());
        assertEquals(1536, param.getVectorConfig().getVectorDimension());
        assertEquals("vectorIndex", param.getVectorConfig().getVectorIndexName());
        assertEquals(60, param.getConnectionConfig().getConnectionTimeout());
        assertEquals(16, param.getConnectionConfig().getConnectionPoolSize());
        assertEquals(16, param.getConnectionConfig().getMaxIdleConnections());
    }

    @Test
    public void testDefaultValues() {
        HugeGraphParam param = HugeGraphParam.builder().build();

        assertNotNull(param.getServerConfig());
        assertEquals("localhost", param.getServerConfig().getHost());
        assertEquals(8080, param.getServerConfig().getPort());
        assertEquals("hugegraph", param.getServerConfig().getGraph());
        assertNull(param.getServerConfig().getUsername());
        assertNull(param.getServerConfig().getPassword());

        assertNotNull(param.getVectorConfig());
        assertEquals(1536, param.getVectorConfig().getVectorDimension()); 
        assertEquals("vector_index", param.getVectorConfig().getVectorIndexName());

        assertNotNull(param.getConnectionConfig());
        assertEquals(30000, param.getConnectionConfig().getConnectionTimeout());
        assertEquals(10, param.getConnectionConfig().getConnectionPoolSize());
        assertEquals(5, param.getConnectionConfig().getMaxIdleConnections());
    }
}
