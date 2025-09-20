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
package com.alibaba.langgengine.kagi.sdk;

import com.alibaba.langgengine.kagi.KagiConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the KagiClient class.
 */
class KagiClientTest {

    @Test
    void testKagiClientConstruction() {
        // Test default constructor
        assertDoesNotThrow(() -> new KagiClient());
        
        // Test constructor with API key
        assertDoesNotThrow(() -> new KagiClient("test-api-key"));
    }

    @Test
    void testSimpleSearch() {
        KagiClient client = new KagiClient();
        // Test with configured API key
        try {
            SearchResponse response = client.search("artificial intelligence");
            assertNotNull(response);
            System.out.println("Search response received: " + response);
        } catch (KagiException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("API request failed: 401")) {
                System.out.println("Search API access not available with current API key: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test
    void testSearchWithRequestObject() {
        KagiClient client = new KagiClient();
        SearchRequest request = new SearchRequest();
        request.setQuery("machine learning");
        request.setLimit(5);
        
        // Test with configured API key
        try {
            SearchResponse response = client.search(request);
            assertNotNull(response);
            System.out.println("Search with request object response: " + response);
        } catch (KagiException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("API request failed: 401")) {
                System.out.println("Search API access not available with current API key: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }


    @Test
    void testSearchWithCustomApiKey() {
        // Test with API key from configuration
        String testApiKey = KagiConfiguration.KAGI_API_KEY;
        if (testApiKey != null && !testApiKey.isEmpty() && !testApiKey.equals("xxx")) {
            KagiClient client = new KagiClient(testApiKey);
            try {
                SearchResponse response = client.search("java programming");
                assertNotNull(response);
                System.out.println("Search with custom API key response: " + response);
            } catch (KagiException e) {
                if (e.getMessage().contains("401") || e.getMessage().contains("API request failed: 401")) {
                    System.out.println("Search API access not available with current API key: " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            System.out.println("API key not configured, skipping search test");
        }
    }

    @Test
    void testSearchWithNullQuery() {
        KagiClient client = new KagiClient("test-key");
        SearchRequest request = new SearchRequest();
        request.setQuery(null);
        
        // This should handle null query gracefully
        assertDoesNotThrow(() -> {
            try {
                client.search(request);
            } catch (KagiException e) {
                // Expected behavior for null query
                assertTrue(e.getMessage().contains("API request failed") || 
                          e.getMessage().contains("Error occurred during API call"));
            }
        });
    }

    @Test
    void testSearchWithEmptyApiKey() {
        KagiClient client = new KagiClient("");
        
        assertThrows(KagiException.class, () -> {
            client.search("test query");
        }, "Should throw KagiException when API key is empty");
    }

    @Test
    void testSearchWithNullApiKey() {
        KagiClient client = new KagiClient(null);
        
        assertThrows(KagiException.class, () -> {
            client.search("test query");
        }, "Should throw KagiException when API key is null");
    }

}