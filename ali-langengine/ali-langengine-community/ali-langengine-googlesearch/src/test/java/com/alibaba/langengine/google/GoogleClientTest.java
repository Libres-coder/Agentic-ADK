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
package com.alibaba.langengine.google;

import com.alibaba.langengine.google.sdk.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Google搜索客户端测试类
 */
@DisplayName("Google搜索客户端测试")
class GoogleClientTest {
    
    private GoogleClient googleClient;
    
    @BeforeEach
    void setUp() {
        googleClient = new GoogleClient();
    }
    
    @Test
    @DisplayName("测试创建Google客户端")
    void testCreateGoogleClient() {
        assertNotNull(googleClient);
        
        GoogleClient customClient = new GoogleClient(
            "Custom User Agent",
            60,
            "zh",
            "CN"
        );
        assertNotNull(customClient);
    }
    
    @Test
    @DisplayName("测试搜索请求验证")
    void testSearchRequestValidation() {
        SearchRequest validRequest = new SearchRequest().setQuery("test query");
        assertTrue(validRequest.isValid());
        
        SearchRequest invalidRequest = new SearchRequest().setQuery("");
        assertFalse(invalidRequest.isValid());
        
        SearchRequest nullRequest = new SearchRequest().setQuery(null);
        assertFalse(nullRequest.isValid());
    }
    
    @Test
    @DisplayName("测试搜索请求参数验证")
    void testSearchRequestParameterValidation() {
        SearchRequest request = new SearchRequest()
            .setQuery("test query")
            .setCount(5)
            .setSearchType("web")
            .setLanguage("en");
        
        assertEquals(5, request.getValidCount());
        assertEquals("web", request.getValidSearchType());
        assertEquals("en", request.getValidLanguage());
        
        // 测试边界值
        SearchRequest maxRequest = new SearchRequest()
            .setQuery("test")
            .setCount(200); // 超过最大值
        assertEquals(100, maxRequest.getValidCount());
        
        SearchRequest minRequest = new SearchRequest()
            .setQuery("test")
            .setCount(0); // 低于最小值
        assertEquals(10, minRequest.getValidCount());
    }
    
    @Test
    @DisplayName("测试搜索结果验证")
    void testSearchResultValidation() {
        SearchResult validResult = new SearchResult()
            .setTitle("Test Title")
            .setUrl("https://example.com")
            .setDescription("Test description");
        assertTrue(validResult.isValid());
        
        SearchResult invalidResult = new SearchResult()
            .setTitle("")
            .setUrl("https://example.com");
        assertFalse(invalidResult.isValid());
        
        SearchResult emptyResult = new SearchResult();
        assertFalse(emptyResult.isValid());
    }
    
    @Test
    @DisplayName("测试搜索结果域名解析")
    void testSearchResultDomainExtraction() {
        SearchResult result = new SearchResult()
            .setUrl("https://www.example.com/path?param=value");
        
        String domain = result.getValidDomain();
        assertEquals("www.example.com", domain);
        
        // 测试没有URL的情况
        SearchResult noUrlResult = new SearchResult()
            .setTitle("Test")
            .setDomain("example.org");
        assertEquals("example.org", noUrlResult.getValidDomain());
    }
    
    @Test
    @DisplayName("测试搜索响应创建")
    void testSearchResponseCreation() {
        SearchResponse response = new SearchResponse("test query");
        
        assertEquals("test query", response.getQuery());
        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getResults().isEmpty());
        assertEquals(0, response.getValidResultCount());
    }
    
    @Test
    @DisplayName("测试搜索响应添加结果")
    void testSearchResponseAddResults() {
        SearchResponse response = new SearchResponse("test query");
        
        SearchResult result1 = new SearchResult()
            .setTitle("Result 1")
            .setUrl("https://example1.com");
        
        SearchResult result2 = new SearchResult()
            .setTitle("Result 2")
            .setUrl("https://example2.com");
        
        response.addResult(result1);
        response.addResult(result2);
        
        assertEquals(2, response.getValidResultCount());
        assertTrue(response.hasResults());
        assertEquals("Result 1", response.getFirstResult().getTitle());
        assertEquals("Result 2", response.getLastResult().getTitle());
    }
    
    @Test
    @DisplayName("测试搜索响应错误处理")
    void testSearchResponseErrorHandling() {
        SearchResponse response = new SearchResponse("test query");
        
        response.setError("Test error message");
        assertFalse(response.getSuccess());
        assertEquals("Test error message", response.getErrorMessage());
        assertEquals(500, response.getStatusCode());
        
        response.setError("Custom error", 404);
        assertEquals(404, response.getStatusCode());
    }
    
    @Test
    @DisplayName("测试搜索响应过滤无效结果")
    void testSearchResponseFilterInvalidResults() {
        SearchResponse response = new SearchResponse("test query");
        
        SearchResult validResult = new SearchResult()
            .setTitle("Valid Result")
            .setUrl("https://example.com");
        
        SearchResult invalidResult = new SearchResult()
            .setTitle("")
            .setUrl("https://example.com");
        
        response.addResult(validResult);
        response.addResult(invalidResult);
        
        // 只有有效结果被添加
        assertEquals(1, response.getValidResultCount());
        assertEquals("Valid Result", response.getFirstResult().getTitle());
    }
    
    @Test
    @DisplayName("测试搜索响应空值处理")
    void testSearchResponseNullHandling() {
        SearchResponse response = new SearchResponse("test query");
        
        // 测试空值处理
        assertNotNull(response.getResults());
        // metadata可能为null，这是正常的
        assertEquals(0, response.getValidResultCount());
    }
    
    @Test
    @DisplayName("测试Google常量")
    void testGoogleConstants() {
        assertEquals("https://www.google.com/search", GoogleConstant.BASE_URL);
        assertEquals(30, GoogleConstant.DEFAULT_TIMEOUT_SECONDS);
        assertEquals(10, GoogleConstant.DEFAULT_RESULT_COUNT);
        assertEquals(100, GoogleConstant.MAX_RESULT_COUNT);
        assertEquals(1, GoogleConstant.MIN_RESULT_COUNT);
        
        assertEquals("web", GoogleConstant.SEARCH_TYPE_WEB);
        assertEquals("images", GoogleConstant.SEARCH_TYPE_IMAGES);
        assertEquals("news", GoogleConstant.SEARCH_TYPE_NEWS);
        assertEquals("videos", GoogleConstant.SEARCH_TYPE_VIDEOS);
        
        assertEquals("relevance", GoogleConstant.SORT_RELEVANCE);
        assertEquals("date", GoogleConstant.SORT_DATE);
        assertEquals("rating", GoogleConstant.SORT_RATING);
    }
    
    @Test
    @DisplayName("测试Google异常")
    void testGoogleException() {
        String errorMessage = "Test error message";
        Exception cause = new RuntimeException("Cause exception");
        
        GoogleException exception1 = new GoogleException(errorMessage);
        assertEquals(errorMessage, exception1.getMessage());
        
        GoogleException exception2 = new GoogleException(errorMessage, cause);
        assertEquals(errorMessage, exception2.getMessage());
        assertEquals(cause, exception2.getCause());
        
        GoogleException exception3 = new GoogleException(cause);
        assertEquals(cause, exception3.getCause());
    }
} 