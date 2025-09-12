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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ElasticsearchConfigurationTest {

    @Test
    void testDefaultConfiguration() {
        // Test default values
        assertNotNull(ElasticsearchConfiguration.ELASTICSEARCH_SERVER_URL);
        assertEquals("http://localhost:9200", ElasticsearchConfiguration.ELASTICSEARCH_SERVER_URL);
        
        assertEquals(30000, ElasticsearchConfiguration.ELASTICSEARCH_CONNECTION_TIMEOUT);
        assertEquals(30000, ElasticsearchConfiguration.ELASTICSEARCH_SOCKET_TIMEOUT);
    }

    @Test
    void testConfigurationFields() {
        // Test that all configuration fields are accessible
        assertDoesNotThrow(() -> {
            String serverUrl = ElasticsearchConfiguration.ELASTICSEARCH_SERVER_URL;
            String username = ElasticsearchConfiguration.ELASTICSEARCH_USERNAME;
            String password = ElasticsearchConfiguration.ELASTICSEARCH_PASSWORD;
            String apiKey = ElasticsearchConfiguration.ELASTICSEARCH_API_KEY;
            int connectionTimeout = ElasticsearchConfiguration.ELASTICSEARCH_CONNECTION_TIMEOUT;
            int socketTimeout = ElasticsearchConfiguration.ELASTICSEARCH_SOCKET_TIMEOUT;
            
            // Fields should be accessible (may be null for auth fields)
            assertNotNull(serverUrl);
            // username, password, apiKey can be null if not configured
            assertTrue(connectionTimeout > 0);
            assertTrue(socketTimeout > 0);
        });
    }
}
