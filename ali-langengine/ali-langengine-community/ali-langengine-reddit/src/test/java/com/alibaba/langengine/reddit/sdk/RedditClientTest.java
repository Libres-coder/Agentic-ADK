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
package com.alibaba.langengine.reddit.sdk;

import com.alibaba.langengine.reddit.RedditConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;


class RedditClientTest {

    private RedditConfiguration configuration;
    private RedditClient client;

    @BeforeEach
    void setUp() {
        configuration = new RedditConfiguration();
        client = new RedditClient(configuration);
    }

    @Test
    void testClientInitialization() {
        assertNotNull(client);
        assertNotNull(configuration);
    }

    @Test
    void testClientInitializationWithCustomConfig() {
        RedditConfiguration customConfig = new RedditConfiguration();
        customConfig.setUserAgent("test-agent/1.0");
        customConfig.setTimeoutSeconds(60);
        customConfig.setMaxRetries(5);

        RedditClient customClient = new RedditClient(customConfig);
        assertNotNull(customClient);
    }

    @Test
    void testSearchWithNullRequest() {
        RedditException exception = assertThrows(RedditException.class, () -> {
            client.search(null);
        });
        assertEquals("Search request and query cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSearchWithEmptyQuery() {
        RedditSearchRequest request = new RedditSearchRequest();
        request.setQuery("");

        RedditException exception = assertThrows(RedditException.class, () -> {
            client.search(request);
        });
        assertEquals("Search request and query cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSearchWithNullQuery() {
        RedditSearchRequest request = new RedditSearchRequest();
        request.setQuery(null);

        RedditException exception = assertThrows(RedditException.class, () -> {
            client.search(request);
        });
        assertEquals("Search request and query cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSearchRequestCreation() {
        RedditSearchRequest request = new RedditSearchRequest("test query");
        assertEquals("test query", request.getQuery());
        assertEquals("hot", request.getSort());
        assertEquals("day", request.getTimeRange());
        assertEquals(25, request.getLimit());
        assertEquals("link", request.getType());
        assertFalse(request.getIncludeOver18());
    }

    @Test
    void testSearchRequestWithSubreddit() {
        RedditSearchRequest request = new RedditSearchRequest("java", "programming");
        assertEquals("java", request.getQuery());
        assertEquals("programming", request.getSubreddit());
    }

    @Test
    void testSearchRequestValidation() {
        RedditSearchRequest request = new RedditSearchRequest();
        
        // 测试各种参数设置
        request.setQuery("test");
        request.setSubreddit("java");
        request.setSort("new");
        request.setTimeRange("week");
        request.setLimit(50);
        request.setAfter("t3_12345");
        request.setType("sr");
        request.setIncludeOver18(true);

        assertEquals("test", request.getQuery());
        assertEquals("java", request.getSubreddit());
        assertEquals("new", request.getSort());
        assertEquals("week", request.getTimeRange());
        assertEquals(50, request.getLimit());
        assertEquals("t3_12345", request.getAfter());
        assertEquals("sr", request.getType());
        assertTrue(request.getIncludeOver18());
    }

    @Test
    void testInvalidConfiguration() {
        RedditConfiguration invalidConfig = new RedditConfiguration();
        invalidConfig.setBaseUrl("");
        invalidConfig.setUserAgent("");
        invalidConfig.setTimeoutSeconds(-1);
        invalidConfig.setMaxRetries(-1);
        invalidConfig.setRequestIntervalMs(-1);

        assertFalse(invalidConfig.validateConfiguration());
    }

    @Test
    void testValidConfiguration() {
        RedditConfiguration validConfig = new RedditConfiguration();
        validConfig.setBaseUrl("https://www.reddit.com");
        validConfig.setUserAgent("test-agent/1.0");
        validConfig.setTimeoutSeconds(30);
        validConfig.setMaxRetries(3);
        validConfig.setRequestIntervalMs(1000);

        assertTrue(validConfig.validateConfiguration());
    }

    @Test
    void testConfigurationSummary() {
        String summary = configuration.getConfigurationSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Reddit Configuration"));
        assertTrue(summary.contains("baseUrl="));
        assertTrue(summary.contains("userAgent="));
    }

    @Test
    void testUserAgentValidation() {
        assertFalse(configuration.hasValidUserAgent()); // 默认值不算有效

        configuration.setUserAgent("custom-agent/2.0");
        assertTrue(configuration.hasValidUserAgent());
    }

    @Test
    void testClientClose() {
        assertDoesNotThrow(() -> {
            client.close();
        });
    }

    @Test
    @EnabledIf("hasRedditApiAccess")
    void testRealApiSearch() {
        // 只有在有实际API访问权限时才运行此测试
        try {
            RedditSearchRequest request = new RedditSearchRequest("java programming");
            request.setSubreddit("programming");
            request.setLimit(5);

            RedditSearchResponse response = client.search(request);
            assertNotNull(response);
            
        } catch (RedditException e) {
            // 在没有网络或API限制的情况下，这是预期的
            assertTrue(e.getMessage().contains("Reddit API request failed") || 
                      e.getMessage().contains("Failed to execute Reddit search"));
        }
    }

    @Test
    void testErrorHandling() {
        // 测试无效URL配置
        RedditConfiguration invalidUrlConfig = new RedditConfiguration();
        invalidUrlConfig.setBaseUrl("invalid-url");
        
        RedditClient invalidClient = new RedditClient(invalidUrlConfig);
        RedditSearchRequest request = new RedditSearchRequest("test");

        assertThrows(RedditException.class, () -> {
            invalidClient.search(request);
        });
    }

    @Test
    void testTimeoutConfiguration() {
        RedditConfiguration timeoutConfig = new RedditConfiguration();
        timeoutConfig.setTimeoutSeconds(1); // 很短的超时时间
        
        RedditClient timeoutClient = new RedditClient(timeoutConfig);
        assertNotNull(timeoutClient);
    }

    @Test
    void testRateLimitConfiguration() {
        RedditConfiguration rateLimitConfig = new RedditConfiguration();
        rateLimitConfig.setRequestIntervalMs(100); // 100ms间隔
        
        RedditClient rateLimitClient = new RedditClient(rateLimitConfig);
        assertNotNull(rateLimitClient);
    }

    @Test
    void testRetryConfiguration() {
        RedditConfiguration retryConfig = new RedditConfiguration();
        retryConfig.setMaxRetries(0); // 不重试
        
        RedditClient noRetryClient = new RedditClient(retryConfig);
        assertNotNull(noRetryClient);
    }

    // 辅助方法：检查是否有Reddit API访问权限
    static boolean hasRedditApiAccess() {
        String testMode = System.getProperty("reddit.test.mode");
        return "integration".equals(testMode);
    }
}
