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
package com.alibaba.langengine.metaso.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetaSoClientTest {
    
    @Test
    void testCreateClientWithDefaultConfig() {
        // Test creating client using the default configuration
        assertDoesNotThrow(() -> {
            MetaSoClient client = new MetaSoClient();
            assertNotNull(client);
            assertNotNull(client.getClient());
            assertNotNull(client.getObjectMapper());
        });
    }
    
    @Test
    void testCreateClientWithCustomConfig() {
        // Test creating client with custom base URL and API key
        assertDoesNotThrow(() -> {
            MetaSoClient client = new MetaSoClient("https://metaso.cn", "test-key");
            assertNotNull(client);
            assertEquals("https://metaso.cn", client.getBaseUrl());
            assertEquals("test-key", client.getApiKey());
        });
    }
    
    @Test
    void testSearch() {
        // Test search functionality using the default API key
        MetaSoClient client = new MetaSoClient();
        assertDoesNotThrow(() -> {
            SearchResponse response = client.search("Artificial Intelligence");
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
    
    @Test
    void testSearchWithRequest() {
        // Test search functionality using a custom SearchRequest
        MetaSoClient client = new MetaSoClient();
        SearchRequest request = new SearchRequest();
        request.setQuery("Machine Learning");
        request.setScope("webpage");
        request.setIncludeSummary(true);
        request.setSize(5);
        
        assertDoesNotThrow(() -> {
            SearchResponse response = client.search(request);
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
    
    @Test
    void testQA() {
        // Test QA functionality using the default API key
        MetaSoClient client = new MetaSoClient();
        assertDoesNotThrow(() -> {
            QAResponse response = client.qa("What is Artificial Intelligence?");
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
    
    @Test
    void testQAWithRequest() {
        // Test QA functionality using a custom QARequest
        MetaSoClient client = new MetaSoClient();
        QARequest request = new QARequest();
        request.setQuestion("What is Machine Learning?");
        request.setScope("webpage");
        request.setSize(5);
        
        assertDoesNotThrow(() -> {
            QAResponse response = client.qa(request);
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
}