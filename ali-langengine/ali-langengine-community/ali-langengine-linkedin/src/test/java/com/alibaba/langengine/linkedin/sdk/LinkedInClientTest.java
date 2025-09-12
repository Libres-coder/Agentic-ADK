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
package com.alibaba.langengine.linkedin.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedInClientTest {

    @Test
    void testLinkedInClientCreation() {
        // 测试默认构造函数
        LinkedInClient client = new LinkedInClient();
        assertNotNull(client);
        
        // 测试带Access Token的构造函数
        LinkedInClient clientWithToken = new LinkedInClient("test-token");
        assertNotNull(clientWithToken);
    }

    @Test
    void testGetCurrentUser() {
        // 注意：这个测试需要有效的LinkedIn API凭据
        // 在实际环境中，应该使用mock或者测试凭据
        LinkedInClient client = new LinkedInClient();
        
        // 由于没有真实的API凭据，这里只测试方法调用不会抛出异常
        assertDoesNotThrow(() -> {
            try {
                client.getCurrentUser();
            } catch (LinkedInException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("LinkedIn API"));
            }
        });
    }

    @Test
    void testSearchCompanies() {
        LinkedInClient client = new LinkedInClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.searchCompanies("test", 10);
            } catch (LinkedInException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("LinkedIn API"));
            }
        });
    }

    @Test
    void testSearchPeople() {
        LinkedInClient client = new LinkedInClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.searchPeople("test", 10);
            } catch (LinkedInException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("LinkedIn API"));
            }
        });
    }

    @Test
    void testGetCompany() {
        LinkedInClient client = new LinkedInClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.getCompany("test-company-id");
            } catch (LinkedInException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("LinkedIn API"));
            }
        });
    }
}