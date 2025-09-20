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
import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索请求类测试
 *
 * @author aihe.ah
 */
public class SearchRequestTest {

    private SearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        searchRequest = new SearchRequest();
    }

    @Test
    void testDefaultValues() {
        // 测试默认值
        assertNull(searchRequest.getQuery());
        assertNull(searchRequest.getCount());
    }

    @Test
    void testSetAndGetQuery() {
        String query = "测试查询";
        searchRequest.setQuery(query);
        assertEquals(query, searchRequest.getQuery());
    }

    @Test
    void testSetAndGetCount() {
        Integer count = 20;
        searchRequest.setCount(count);
        assertEquals(count, searchRequest.getCount());
    }

    @Test
    void testSetAndGetCountWithNull() {
        searchRequest.setCount(null);
        assertNull(searchRequest.getCount());
    }

    @Test
    void testSetAndGetQueryWithNull() {
        searchRequest.setQuery(null);
        assertNull(searchRequest.getQuery());
    }

    @Test
    void testSetAndGetQueryWithEmptyString() {
        searchRequest.setQuery("");
        assertEquals("", searchRequest.getQuery());
    }

    @Test
    void testSetAndGetQueryWithWhitespace() {
        searchRequest.setQuery("   ");
        assertEquals("   ", searchRequest.getQuery());
    }
} 