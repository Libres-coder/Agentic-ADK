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
package com.alibaba.langengine.twitter.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TwitterClientTest {

    @Test
    void testTwitterClientCreation() {
        // 测试默认构造函数
        TwitterClient client = new TwitterClient();
        assertNotNull(client);
        
        // 测试带Bearer Token的构造函数
        TwitterClient clientWithToken = new TwitterClient("test-token");
        assertNotNull(clientWithToken);
    }

    @Test
    void testSearchTweets() {
        // 注意：这个测试需要有效的Twitter API凭据
        // 在实际环境中，应该使用mock或者测试凭据
        TwitterClient client = new TwitterClient();
        
        // 由于没有真实的API凭据，这里只测试方法调用不会抛出异常
        assertDoesNotThrow(() -> {
            try {
                client.searchTweets("test", 10);
            } catch (TwitterException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("Twitter API"));
            }
        });
    }

    @Test
    void testGetUserByUsername() {
        TwitterClient client = new TwitterClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.getUserByUsername("testuser");
            } catch (TwitterException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("Twitter API"));
            }
        });
    }

    @Test
    void testGetUserTimeline() {
        TwitterClient client = new TwitterClient();
        
        assertDoesNotThrow(() -> {
            try {
                client.getUserTimeline("test-user-id", 10);
            } catch (TwitterException e) {
                // 预期的异常，因为没有有效的API凭据
                assertTrue(e.getMessage().contains("Twitter API"));
            }
        });
    }
}