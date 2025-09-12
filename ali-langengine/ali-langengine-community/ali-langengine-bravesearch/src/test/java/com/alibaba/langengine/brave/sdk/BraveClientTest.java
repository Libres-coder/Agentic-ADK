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
package com.alibaba.langengine.brave.sdk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the BraveClient class.
 */
@EnabledIfEnvironmentVariable(named = "BRAVE_API_KEY", matches = ".*")
class BraveClientTest {

    @Test
    void testBraveClientConstruction() {
        // Test default constructor
        assertDoesNotThrow(() -> new BraveClient());
        
        // Test constructor with API key
        assertDoesNotThrow(() -> new BraveClient("test-api-key"));
    }

    @Test
    void testSimpleSearch() {
        BraveClient client = new BraveClient();
        // This test will only run if BRAVE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            SearchResponse response = client.search("test query");
            assertNotNull(response);
        });
    }

    @Test
    void testSearchWithCount() {
        BraveClient client = new BraveClient();
        // This test will only run if BRAVE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            SearchResponse response = client.search("test query", 5);
            assertNotNull(response);
        });
    }

    @Test
    void testSearchWithRequestObject() {
        BraveClient client = new BraveClient();
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");
        request.setCount(3);
        request.setSafesearch("strict");
        
        // This test will only run if BRAVE_API_KEY environment variable is set
        assertDoesNotThrow(() -> {
            SearchResponse response = client.search(request);
            assertNotNull(response);
        });
    }
}