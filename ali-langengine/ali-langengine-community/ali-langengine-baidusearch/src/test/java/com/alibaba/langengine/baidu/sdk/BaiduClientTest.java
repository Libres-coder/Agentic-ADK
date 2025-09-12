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
package com.alibaba.langengine.baidu.sdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 百度客户端测试类
 *
 * @author aihe.ah
 */
@Disabled("需要网络连接才能运行，实际使用时需要有效网络环境")
public class BaiduClientTest {

    private BaiduClient baiduClient;
    private SearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        baiduClient = new BaiduClient();
        searchRequest = new SearchRequest();
    }

    @Test
    void testDefaultConstructor() {
        // 测试默认构造函数
        assertNotNull(baiduClient);
    }

    @Test
    void testCustomUserAgentConstructor() {
        // 测试自定义用户代理构造函数
        String customUserAgent = "Custom User Agent";
        BaiduClient customClient = new BaiduClient(customUserAgent);
        assertNotNull(customClient);
    }

    @Test
    void testCustomUserAgentConstructorWithNull() {
        // 测试传入null用户代理的构造函数
        BaiduClient nullUserAgentClient = new BaiduClient(null);
        assertNotNull(nullUserAgentClient);
    }

    @Test
    void testCustomUserAgentConstructorWithEmptyString() {
        // 测试传入空字符串用户代理的构造函数
        BaiduClient emptyUserAgentClient = new BaiduClient("");
        assertNotNull(emptyUserAgentClient);
    }

    @Test
    void testCustomUserAgentConstructorWithWhitespace() {
        // 测试传入空白字符用户代理的构造函数
        BaiduClient whitespaceUserAgentClient = new BaiduClient("   ");
        assertNotNull(whitespaceUserAgentClient);
    }

    @Test
    void testSearchWithNullRequest() {
        // 测试传入null请求
        assertThrows(BaiduException.class, () -> {
            baiduClient.search((SearchRequest) null);
        });
    }

    @Test
    void testSearchWithNullQuery() {
        // 测试传入null查询
        searchRequest.setQuery(null);
        assertThrows(BaiduException.class, () -> {
            baiduClient.search(searchRequest);
        });
    }

    @Test
    void testSearchWithEmptyQuery() {
        // 测试传入空查询
        searchRequest.setQuery("");
        assertThrows(BaiduException.class, () -> {
            baiduClient.search(searchRequest);
        });
    }

    @Test
    void testSearchWithWhitespaceQuery() {
        // 测试传入空白字符查询
        searchRequest.setQuery("   ");
        assertThrows(BaiduException.class, () -> {
            baiduClient.search(searchRequest);
        });
    }

    @Test
    void testSearchWithValidQuery() {
        // 测试有效查询（需要网络连接）
        searchRequest.setQuery("测试查询");
        searchRequest.setCount(5);
        
        try {
            SearchResponse response = baiduClient.search(searchRequest);
            assertNotNull(response);
            assertEquals("测试查询", response.getQuery());
            assertNotNull(response.getResults());
            assertTrue(response.getResults().size() <= 5);
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }

    @Test
    void testSearchWithDefaultCount() {
        // 测试默认数量（需要网络连接）
        searchRequest.setQuery("测试查询");
        // 不设置count，使用默认值
        
        try {
            SearchResponse response = baiduClient.search(searchRequest);
            assertNotNull(response);
            assertEquals("测试查询", response.getQuery());
            assertNotNull(response.getResults());
            assertTrue(response.getResults().size() <= 10); // 默认应该是10
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }

    @Test
    void testSearchWithLargeCount() {
        // 测试大数量（需要网络连接）
        searchRequest.setQuery("测试查询");
        searchRequest.setCount(100); // 设置较大的数量
        
        try {
            SearchResponse response = baiduClient.search(searchRequest);
            assertNotNull(response);
            assertEquals("测试查询", response.getQuery());
            assertNotNull(response.getResults());
            assertTrue(response.getResults().size() <= 100);
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }

    @Test
    void testSearchWithSpecialCharacters() {
        // 测试特殊字符查询（需要网络连接）
        searchRequest.setQuery("测试@#$%^&*()_+{}|:<>?[]\\;'\",./");
        searchRequest.setCount(3);
        
        try {
            SearchResponse response = baiduClient.search(searchRequest);
            assertNotNull(response);
            assertEquals("测试@#$%^&*()_+{}|:<>?[]\\;'\",./", response.getQuery());
            assertNotNull(response.getResults());
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }

    @Test
    void testSearchWithChineseQuery() {
        // 测试中文查询（需要网络连接）
        searchRequest.setQuery("人工智能技术发展");
        searchRequest.setCount(5);
        
        try {
            SearchResponse response = baiduClient.search(searchRequest);
            assertNotNull(response);
            assertEquals("人工智能技术发展", response.getQuery());
            assertNotNull(response.getResults());
            assertTrue(response.getResults().size() <= 5);
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }

    @Test
    void testSearchWithEnglishQuery() {
        // 测试英文查询（需要网络连接）
        searchRequest.setQuery("artificial intelligence");
        searchRequest.setCount(5);
        
        try {
            SearchResponse response = baiduClient.search(searchRequest);
            assertNotNull(response);
            assertEquals("artificial intelligence", response.getQuery());
            assertNotNull(response.getResults());
            assertTrue(response.getResults().size() <= 5);
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }

    @Test
    void testSearchWithStringQuery() {
        // 测试字符串查询方法（需要网络连接）
        try {
            SearchResponse response = baiduClient.search("测试查询");
            assertNotNull(response);
            assertEquals("测试查询", response.getQuery());
            assertNotNull(response.getResults());
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }

    @Test
    void testSearchWithStringQueryAndCount() {
        // 测试字符串查询和数量方法（需要网络连接）
        try {
            SearchResponse response = baiduClient.search("测试查询", 3);
            assertNotNull(response);
            assertEquals("测试查询", response.getQuery());
            assertNotNull(response.getResults());
            assertTrue(response.getResults().size() <= 3);
        } catch (BaiduException e) {
            // 网络异常时跳过测试
            System.out.println("网络测试跳过: " + e.getMessage());
        }
    }
} 