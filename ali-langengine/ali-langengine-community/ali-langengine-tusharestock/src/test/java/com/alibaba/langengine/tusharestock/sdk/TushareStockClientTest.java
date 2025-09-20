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
package com.alibaba.langengine.tusharestock.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TushareStockClientTest {
    
    @Test
    void testCreateClientWithDefaultConfig() {
        // Test creating client using the default configuration
        assertDoesNotThrow(() -> {
            TushareStockClient client = new TushareStockClient();
            assertNotNull(client);
            assertNotNull(client.getClient());
            assertNotNull(client.getObjectMapper());
        });
    }
    
    @Test
    void testCreateClientWithCustomConfig() {
        // Test creating client with custom base URL and API token
        assertDoesNotThrow(() -> {
            TushareStockClient client = new TushareStockClient("http://api.tushare.pro", "test-token");
            assertNotNull(client);
            assertEquals("http://api.tushare.pro", client.getBaseUrl());
            assertEquals("test-token", client.getApiToken());
        });
    }
    
    @Test
    void testStockBasic() {
        // Test stock basic functionality using the default API token
        TushareStockClient client = new TushareStockClient();
        assertDoesNotThrow(() -> {
            TushareResponse response = client.stockBasic();
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
    
    @Test
    void testStockBasicWithRequest() {
        // Test stock basic functionality using a custom StockBasicRequest
        TushareStockClient client = new TushareStockClient();
        StockBasicRequest request = new StockBasicRequest();
        request.setExchange("SSE");
        request.setListStatus("L");
        
        assertDoesNotThrow(() -> {
            TushareResponse response = client.stockBasic(request);
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
    
    @Test
    void testDaily() {
        // Test daily functionality using the default API token
        TushareStockClient client = new TushareStockClient();
        assertDoesNotThrow(() -> {
            TushareResponse response = client.daily("000001.SZ");
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
    
    @Test
    void testDailyWithRequest() {
        // Test daily functionality using a custom DailyRequest
        TushareStockClient client = new TushareStockClient();
        DailyRequest request = new DailyRequest();
        request.setTsCode("000001.SZ");
        request.setStartDate("20230101");
        request.setEndDate("20231231");
        
        assertDoesNotThrow(() -> {
            TushareResponse response = client.daily(request);
            assertNotNull(response);
            // Additional assertions can be added to validate the response content
        });
    }
}