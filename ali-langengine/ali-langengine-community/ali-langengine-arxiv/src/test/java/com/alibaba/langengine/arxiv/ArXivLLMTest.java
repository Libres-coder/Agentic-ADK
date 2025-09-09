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
package com.alibaba.langengine.arxiv;

import com.alibaba.langengine.arxiv.model.ArXivSearchRequest;
import com.alibaba.langengine.arxiv.model.ArXivSearchResponse;
import com.alibaba.langengine.arxiv.service.ArXivApiService;
import com.alibaba.langengine.arxiv.sdk.ArXivException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class ArXivLLMTest {
    
    @Mock
    private ArXivApiService mockApiService;
    
    private ArXivLLM arxivLLM;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        arxivLLM = new ArXivLLM();
    }
    
    @Test
    void testLLMCreation() {
        assertNotNull(arxivLLM);
        assertNotNull(arxivLLM.getApiService());
        assertNotNull(arxivLLM.getMaxResults());
        assertNotNull(arxivLLM.getSortOrder());
        assertNotNull(arxivLLM.getSortDirection());
    }
    
    @Test
    void testLLMWithCustomService() {
        ArXivLLM customLLM = new ArXivLLM(mockApiService);
        assertNotNull(customLLM);
        assertEquals(mockApiService, customLLM.getApiService());
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSimpleTextQuery() {
        String query = "machine learning";
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run(query, null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
            assertTrue(result.contains("ArXiv"));
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testNaturalLanguageQuery() {
        String query = "search for deep learning papers";
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run(query, null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testChineseQuery() {
        String query = "搜索神经网络论文";
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run(query, null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testJSONQuery() {
        String jsonQuery = "{\"query\": \"neural networks\", \"max_results\": 3, \"categories\": [\"cs.AI\", \"cs.LG\"]}";
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run(jsonQuery, null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testQueryWithExtraAttributes() {
        String query = "artificial intelligence";
        Map<String, Object> extraAttributes = new HashMap<>();
        extraAttributes.put("maxResults", 5);
        extraAttributes.put("category", "cs.AI");
        extraAttributes.put("sortBy", "relevance");
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run(query, null, null, extraAttributes);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testQueryWithConsumer() {
        String query = "computer vision";
        AtomicReference<String> consumerResult = new AtomicReference<>();
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run(query, null, consumerResult::set, null);
            assertNotNull(result);
            assertNotNull(consumerResult.get());
            assertEquals(result, consumerResult.get());
        });
    }
    
    @Test
    void testConfigurationOptions() {
        arxivLLM.setMaxResults(15);
        arxivLLM.setSortOrder("lastUpdatedDate");
        arxivLLM.setSortDirection("ascending");
        arxivLLM.setIncludeAbstract(false);
        arxivLLM.setIncludePdfLinks(false);
        arxivLLM.setCategories(Arrays.asList("cs.AI", "cs.LG"));
        arxivLLM.setAuthors(Arrays.asList("Smith", "Johnson"));
        arxivLLM.setVectorDatabaseEnabled(true);
        
        assertEquals(Integer.valueOf(15), arxivLLM.getMaxResults());
        assertEquals("lastUpdatedDate", arxivLLM.getSortOrder());
        assertEquals("ascending", arxivLLM.getSortDirection());
        assertEquals(Boolean.FALSE, arxivLLM.getIncludeAbstract());
        assertEquals(Boolean.FALSE, arxivLLM.getIncludePdfLinks());
        assertEquals(Arrays.asList("cs.AI", "cs.LG"), arxivLLM.getCategories());
        assertEquals(Arrays.asList("Smith", "Johnson"), arxivLLM.getAuthors());
        assertEquals(Boolean.TRUE, arxivLLM.getVectorDatabaseEnabled());
    }
    
    @Test
    void testMockServiceError() throws ArXivException {
        ArXivLLM mockLLM = new ArXivLLM(mockApiService);
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class))).thenThrow(new ArXivException("Test error"));
        
        String result = mockLLM.run("test query", null, null, null);
        assertNotNull(result);
        assertTrue(result.contains("搜索失败"));
        assertTrue(result.contains("Test error"));
    }
    
    @Test
    void testMockServiceSuccess() throws ArXivException {
        ArXivLLM mockLLM = new ArXivLLM(mockApiService);
        
        ArXivSearchResponse mockResponse = new ArXivSearchResponse();
        mockResponse.setQuery("test query");
        mockResponse.setTotalResults(0);
        mockResponse.setStartIndex(0);
        mockResponse.setItemsPerPage(10);
        mockResponse.setExecutionTimeMs(100L);
        mockResponse.setPapers(new ArrayList<>()); // 设置空的论文列表而不是null
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class))).thenReturn(mockResponse);
        
        String result = mockLLM.run("test query", null, null, null);
        assertNotNull(result);
        assertTrue(result.contains("未找到相关论文"));
    }
    
    @Test
    void testEmptyQuery() {
        String result = arxivLLM.run("", null, null, null);
        assertNotNull(result);
        assertTrue(result.contains("参数错误") || result.contains("搜索失败"));
    }
    
    @Test
    void testNullQuery() {
        String result = arxivLLM.run(null, null, null, null);
        assertNotNull(result);
        assertTrue(result.contains("参数错误") || result.contains("搜索失败"));
    }
    
    @Test
    void testWhitespaceQuery() {
        String result = arxivLLM.run("   ", null, null, null);
        assertNotNull(result);
        assertTrue(result.contains("参数错误") || result.contains("搜索失败"));
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testDateRangeQuery() {
        arxivLLM.setStartDate("2023-01-01");
        arxivLLM.setEndDate("2023-12-31");
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run("machine learning", null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testCategoryFilterQuery() {
        arxivLLM.setCategories(Arrays.asList("cs.AI"));
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run("artificial intelligence", null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testAuthorFilterQuery() {
        arxivLLM.setAuthors(Arrays.asList("Smith"));
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run("machine learning", null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testVectorDatabaseEnabledQuery() {
        arxivLLM.setVectorDatabaseEnabled(true);
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run("semantic search", null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
            assertTrue(result.contains("向量数据库") || result.contains("vector"));
        });
    }
    
    @Test
    void testUnsupportedMethods() {
        // These methods are not implemented for ArXiv LLM
        assertNull(arxivLLM.buildRequest(null, null, null, null));
        assertEquals("", arxivLLM.runRequest(null, null, null, null));
        assertEquals("", arxivLLM.runRequestStream(null, null, null, null));
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testComplexJSONQuery() {
        String complexQuery = "{\"query\": \"transformer attention\", " +
                              "\"max_results\": 5, " +
                              "\"categories\": [\"cs.AI\", \"cs.LG\"], " +
                              "\"sort_by\": \"lastUpdatedDate\", " +
                              "\"sort_order\": \"descending\", " +
                              "\"start_date\": \"2023-01-01\", " +
                              "\"end_date\": \"2023-12-31\"}";
        
        assertDoesNotThrow(() -> {
            String result = arxivLLM.run(complexQuery, null, null, null);
            assertNotNull(result);
            assertFalse(result.trim().isEmpty());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testQueryVariations() {
        String[] queries = {
            "find machine learning papers",
            "look for neural network research",
            "get artificial intelligence articles",
            "搜索深度学习论文",
            "查找计算机视觉研究"
        };
        
        for (String query : queries) {
            assertDoesNotThrow(() -> {
                String result = arxivLLM.run(query, null, null, null);
                assertNotNull(result);
                assertFalse(result.trim().isEmpty());
            }, "Failed for query: " + query);
        }
    }
}
