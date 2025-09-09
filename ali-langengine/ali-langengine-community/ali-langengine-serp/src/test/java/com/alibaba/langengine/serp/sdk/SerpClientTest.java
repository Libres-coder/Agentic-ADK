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
package com.alibaba.langengine.serp.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerpClientTest {

    @Test
    void search() {
        // Test search functionality using the default API key
        SerpClient client = new SerpClient();
        assertDoesNotThrow(() -> {
            SearchResponse response = client.search("Artificial Intelligence");
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }

    @Test
    void testSearch() {
        // Test search functionality using a custom SearchRequest
        SerpClient client = new SerpClient();
        SearchRequest request = new SearchRequest();
        request.setQuery("Machine Learning");
        request.setEngine("google");
        
        assertDoesNotThrow(() -> {
            SearchResponse response = client.search(request);
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
}