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
package com.alibaba.langengine.stackoverflow;

import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchRequest;
import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchResult;
import com.alibaba.langengine.stackoverflow.service.StackOverflowApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class StackOverflowLLMTest {
    
    @Mock
    private StackOverflowApiService mockApiService;
    
    @Mock
    private Consumer<String> mockConsumer;
    
    private StackOverflowLLM stackOverflowLLM;
    
    @BeforeEach
    void setUp() {
        stackOverflowLLM = new StackOverflowLLM(mockApiService);
    }
    
    @Test
    void testBasicSearch() throws Exception {
        // Arrange
        String query = "How to use Java streams";
        List<StackOverflowSearchResult> mockResults = createMockSearchResults();
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(mockResults);
        
        // Act
        String result = stackOverflowLLM.run(query, null, mockConsumer, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Stack Overflow技术问答搜索结果"));
        assertTrue(result.contains("Java streams"));
        assertTrue(result.contains("找到 2 个相关问题"));
        verify(mockApiService).searchQuestions(any(StackOverflowSearchRequest.class));
        verify(mockConsumer).accept(result);
    }
    
    @Test
    void testSearchWithTags() throws Exception {
        // Arrange
        String query = "How to use streams [java] [stream]";
        List<StackOverflowSearchResult> mockResults = createMockSearchResults();
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(mockResults);
        
        // Act
        String result = stackOverflowLLM.run(query, null, null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("How to use streams"));
        verify(mockApiService).searchQuestions(argThat(request -> 
                request.getTags() != null && 
                request.getTags().contains("java") && 
                request.getTags().contains("stream")));
    }
    
    @Test
    void testSearchWithExtraAttributes() throws Exception {
        // Arrange
        String query = "Java concurrency";
        Map<String, Object> extraAttributes = new HashMap<>();
        extraAttributes.put("tags", Arrays.asList("java", "concurrency"));
        extraAttributes.put("maxResults", 5);
        extraAttributes.put("sort", "votes");
        extraAttributes.put("minScore", 10);
        extraAttributes.put("answeredOnly", true);
        
        List<StackOverflowSearchResult> mockResults = createMockSearchResults();
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(mockResults);
        
        // Act
        String result = stackOverflowLLM.run(query, null, null, extraAttributes);
        
        // Assert
        assertNotNull(result);
        
        // Capture the request to verify its properties
        ArgumentCaptor<StackOverflowSearchRequest> requestCaptor = ArgumentCaptor.forClass(StackOverflowSearchRequest.class);
        verify(mockApiService).searchQuestions(requestCaptor.capture());
        
        StackOverflowSearchRequest capturedRequest = requestCaptor.getValue();
        assertEquals(Integer.valueOf(5), capturedRequest.getPageSize());
        assertEquals("votes", capturedRequest.getSort());
        assertEquals(Integer.valueOf(10), capturedRequest.getMinScore());
        assertTrue(capturedRequest.getAnsweredOnly());
    }
    
    @Test
    void testSearchWithEmptyResults() throws Exception {
        // Arrange
        String query = "very specific technical query that has no results";
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(Collections.emptyList());
        
        // Act
        String result = stackOverflowLLM.run(query, null, null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("未找到相关的Stack Overflow问答"));
        verify(mockApiService).searchQuestions(any(StackOverflowSearchRequest.class));
    }
    
    @Test
    void testSearchWithException() throws Exception {
        // Arrange
        String query = "test query";
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenThrow(new RuntimeException("API error"));
        
        // Act
        String result = stackOverflowLLM.run(query, null, mockConsumer, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Stack Overflow搜索失败"));
        assertTrue(result.contains("API error"));
        verify(mockConsumer).accept(result);
    }
    
    @Test
    void testFluentApiWithTags() throws Exception {
        // Arrange
        List<StackOverflowSearchResult> mockResults = createMockSearchResults();
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(mockResults);
        
        // Act
        StackOverflowLLM llm = stackOverflowLLM
                .withTags("java", "spring")
                .withMaxResults(5)
                .withSort("votes")
                .withMinScore(5)
                .onlyAnswered()
                .onlyAccepted();
        
        String result = llm.run("Spring Boot configuration", null, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(Arrays.asList("java", "spring"), llm.getTags());
        assertEquals(Integer.valueOf(5), llm.getMaxResults());
        assertEquals("votes", llm.getSortOrder());
        assertEquals(Integer.valueOf(5), llm.getMinScore());
        assertTrue(llm.getAnsweredOnly());
        assertTrue(llm.getAcceptedAnswerOnly());
    }
    
    @Test
    void testWithCustomSite() throws Exception {
        // Arrange
        List<StackOverflowSearchResult> mockResults = createMockSearchResults();
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(mockResults);
        
        // Act
        StackOverflowLLM llm = stackOverflowLLM.withSite("superuser");
        String result = llm.run("Windows troubleshooting", null, null, null);
        
        // Assert
        assertNotNull(result);
        assertEquals("superuser", llm.getCustomSite());
        
        ArgumentCaptor<StackOverflowSearchRequest> requestCaptor = ArgumentCaptor.forClass(StackOverflowSearchRequest.class);
        verify(mockApiService).searchQuestions(requestCaptor.capture());
        
        StackOverflowSearchRequest capturedRequest = requestCaptor.getValue();
        assertEquals("superuser", capturedRequest.getSite());
    }
    
    @Test
    void testConfigurationDefaults() {
        // Assert default values
        assertEquals("stackoverflow", stackOverflowLLM.getSite());
        assertEquals(Integer.valueOf(10), stackOverflowLLM.getMaxResults());
        assertEquals("votes", stackOverflowLLM.getSortOrder());
        assertEquals(Integer.valueOf(0), stackOverflowLLM.getMinScore());
        assertFalse(stackOverflowLLM.getAnsweredOnly());
        assertFalse(stackOverflowLLM.getAcceptedAnswerOnly());
        assertTrue(stackOverflowLLM.getIncludeBody());
        assertTrue(stackOverflowLLM.getIncludeAnswers());
    }
    
    @Test
    void testTagsExtractionFromQuery() throws Exception {
        // Arrange
        String query = "How to handle exceptions [java] [exception-handling] in Spring Boot [spring-boot]";
        List<StackOverflowSearchResult> mockResults = createMockSearchResults();
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(mockResults);
        
        // Act
        stackOverflowLLM.run(query, null, null, null);
        
        // Assert
        ArgumentCaptor<StackOverflowSearchRequest> requestCaptor = ArgumentCaptor.forClass(StackOverflowSearchRequest.class);
        verify(mockApiService).searchQuestions(requestCaptor.capture());
        
        StackOverflowSearchRequest capturedRequest = requestCaptor.getValue();
        List<String> tags = capturedRequest.getTags();
        assertNotNull(tags);
        assertTrue(tags.contains("java"));
        assertTrue(tags.contains("exception-handling"));
        assertTrue(tags.contains("spring-boot"));
        assertFalse(capturedRequest.getQuery().contains("[java]"));
        assertFalse(capturedRequest.getQuery().contains("[exception-handling]"));
        assertFalse(capturedRequest.getQuery().contains("[spring-boot]"));
    }
    
    @Test
    void testResultFormatting() throws Exception {
        // Arrange
        String query = "test query";
        List<StackOverflowSearchResult> mockResults = createDetailedMockSearchResults();
        
        when(mockApiService.searchQuestions(any(StackOverflowSearchRequest.class)))
                .thenReturn(mockResults);
        
        when(mockApiService.getRemainingQuota()).thenReturn(9500);
        
        // Act
        String result = stackOverflowLLM.run(query, null, null, null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("=== Stack Overflow技术问答搜索结果 ==="));
        assertTrue(result.contains("【问题 1】"));
        assertTrue(result.contains("标题: How to use Java 8 streams"));
        assertTrue(result.contains("评分: 150"));
        assertTrue(result.contains("答案数: 5"));
        assertTrue(result.contains("浏览数: 10000"));
        assertTrue(result.contains("✓已回答"));
        assertTrue(result.contains("✓已采纳"));
        assertTrue(result.contains("标签: [java] [stream] [java-8]"));
        assertTrue(result.contains("摘要: This is a detailed explanation"));
        assertTrue(result.contains("最佳答案: You can use stream operations"));
        assertTrue(result.contains("=== 使用提示 ==="));
        assertTrue(result.contains("当前API配额剩余: 9500"));
    }
    
    private List<StackOverflowSearchResult> createMockSearchResults() {
        List<StackOverflowSearchResult> results = new ArrayList<>();
        
        StackOverflowSearchResult result1 = new StackOverflowSearchResult();
        result1.setQuestionId(123456L);
        result1.setTitle("How to use Java 8 streams");
        result1.setLink("https://stackoverflow.com/questions/123456");
        result1.setScore(150);
        result1.setAnswerCount(5);
        result1.setViewCount(10000);
        result1.setIsAnswered(true);
        result1.setAcceptedAnswerId(789012L);
        result1.setTags(Arrays.asList("java", "stream", "java-8"));
        results.add(result1);
        
        StackOverflowSearchResult result2 = new StackOverflowSearchResult();
        result2.setQuestionId(654321L);
        result2.setTitle("Java stream filter and map operations");
        result2.setLink("https://stackoverflow.com/questions/654321");
        result2.setScore(85);
        result2.setAnswerCount(3);
        result2.setViewCount(5000);
        result2.setIsAnswered(true);
        result2.setTags(Arrays.asList("java", "stream", "filter", "map"));
        results.add(result2);
        
        return results;
    }
    
    private List<StackOverflowSearchResult> createDetailedMockSearchResults() {
        List<StackOverflowSearchResult> results = new ArrayList<>();
        
        StackOverflowSearchResult result = new StackOverflowSearchResult();
        result.setQuestionId(123456L);
        result.setTitle("How to use Java 8 streams");
        result.setLink("https://stackoverflow.com/questions/123456");
        result.setScore(150);
        result.setAnswerCount(5);
        result.setViewCount(10000);
        result.setIsAnswered(true);
        result.setAcceptedAnswerId(789012L);
        result.setTags(Arrays.asList("java", "stream", "java-8"));
        result.setBody("This is a detailed explanation of how to use Java 8 streams effectively. " +
                "Java 8 introduced a powerful new API for processing collections of data in a functional style.");
        
        StackOverflowSearchResult.Answer answer = new StackOverflowSearchResult.Answer();
        answer.setAnswerId(789012L);
        answer.setBody("You can use stream operations like filter(), map(), collect() to process data efficiently. " +
                "Here's a comprehensive example showing different stream operations and best practices.");
        answer.setScore(200);
        answer.setIsAccepted(true);
        result.setBestAnswer(answer);
        
        results.add(result);
        return results;
    }
}
