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
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索响应类测试
 *
 * @author aihe.ah
 */
public class SearchResponseTest {

    private SearchResponse searchResponse;

    @BeforeEach
    void setUp() {
        searchResponse = new SearchResponse();
    }

    @Test
    void testDefaultValues() {
        // 测试默认值
        assertNull(searchResponse.getQuery());
        assertNull(searchResponse.getResults());
    }

    @Test
    void testSetAndGetQuery() {
        String query = "测试查询";
        searchResponse.setQuery(query);
        assertEquals(query, searchResponse.getQuery());
    }

    @Test
    void testSetAndGetResults() {
        List<SearchResult> results = new ArrayList<>();
        SearchResult result1 = new SearchResult();
        result1.setTitle("测试标题1");
        result1.setUrl("http://test1.com");
        result1.setDescription("测试描述1");
        results.add(result1);

        SearchResult result2 = new SearchResult();
        result2.setTitle("测试标题2");
        result2.setUrl("http://test2.com");
        result2.setDescription("测试描述2");
        results.add(result2);

        searchResponse.setResults(results);
        
        assertNotNull(searchResponse.getResults());
        assertEquals(2, searchResponse.getResults().size());
        assertEquals("测试标题1", searchResponse.getResults().get(0).getTitle());
        assertEquals("测试标题2", searchResponse.getResults().get(1).getTitle());
    }

    @Test
    void testSetAndGetQueryWithNull() {
        searchResponse.setQuery(null);
        assertNull(searchResponse.getQuery());
    }

    @Test
    void testSetAndGetResultsWithNull() {
        searchResponse.setResults(null);
        assertNull(searchResponse.getResults());
    }

    @Test
    void testSetAndGetQueryWithEmptyString() {
        searchResponse.setQuery("");
        assertEquals("", searchResponse.getQuery());
    }

    @Test
    void testSetAndGetResultsWithEmptyList() {
        List<SearchResult> emptyResults = new ArrayList<>();
        searchResponse.setResults(emptyResults);
        assertNotNull(searchResponse.getResults());
        assertEquals(0, searchResponse.getResults().size());
    }
} 