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
package com.alibaba.langengine.weibo.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeiboClientTest {

    @Test
    void testWeiboClientCreation() {
        // 测试默认构造函数
        WeiboClient client = new WeiboClient();
        assertNotNull(client);
        
        // 测试带Access Token的构造函数
        WeiboClient clientWithToken = new WeiboClient("test-token");
        assertNotNull(clientWithToken);
    }

    @Test
    void testSearchWeibo() {
        // 注意：这个测试需要有效的微博 API凭据
        // 在实际环境中，应该使用mock或者测试凭据
        WeiboClient client = new WeiboClient();
        
        // 由于没有真实的API凭据，这里只测试方法调用不会抛出异常
        assertDoesNotThrow(() -> {
            try {
                client.searchWeibo("test", 10);
            } catch (WeiboException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("Weibo API"));
            }
        });
    }

    @Test
    void testGetUserInfo() {
        WeiboClient client = new WeiboClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.getUserInfo("test-user-id");
            } catch (WeiboException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("Weibo API"));
            }
        });
    }

    @Test
    void testGetUserTimeline() {
        WeiboClient client = new WeiboClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.getUserTimeline("test-user-id", 10);
            } catch (WeiboException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("Weibo API"));
            }
        });
    }

    @Test
    void testGetHotTopics() {
        WeiboClient client = new WeiboClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.getHotTopics(10);
            } catch (WeiboException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("Weibo API"));
            }
        });
    }
}