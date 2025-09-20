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
package com.alibaba.langengine.stackoverflow.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class StackOverflowModelTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testStackOverflowSearchResultSerialization() throws Exception {
        // Arrange
        StackOverflowSearchResult result = new StackOverflowSearchResult();
        result.setQuestionId(123456L);
        result.setTitle("How to use Java streams");
        result.setLink("https://stackoverflow.com/questions/123456");
        result.setScore(150);
        result.setAnswerCount(5);
        result.setViewCount(10000);
        result.setIsAnswered(true);
        result.setAcceptedAnswerId(789012L);
        result.setCreationDate(1640995200L); // 2022-01-01
        result.setLastActivityDate(1672531200L); // 2023-01-01
        result.setTags(Arrays.asList("java", "stream", "java-8"));
        result.setRelevanceScore(0.95);
        
        // Create owner
        StackOverflowSearchResult.Owner owner = new StackOverflowSearchResult.Owner();
        owner.setUserId(456789L);
        owner.setDisplayName("JavaExpert");
        owner.setReputation(15000);
        owner.setProfileImage("https://example.com/avatar.jpg");
        owner.setLink("https://stackoverflow.com/users/456789");
        result.setOwner(owner);
        
        // Create best answer
        StackOverflowSearchResult.Answer answer = new StackOverflowSearchResult.Answer();
        answer.setAnswerId(789012L);
        answer.setBody("You can use stream operations like filter(), map(), collect()");
        answer.setScore(200);
        answer.setIsAccepted(true);
        answer.setCreationDate(1641081600L);
        
        StackOverflowSearchResult.Owner answerOwner = new StackOverflowSearchResult.Owner();
        answerOwner.setUserId(987654L);
        answerOwner.setDisplayName("StreamMaster");
        answerOwner.setReputation(25000);
        answer.setOwner(answerOwner);
        
        result.setBestAnswer(answer);
        
        // Act
        String json = objectMapper.writeValueAsString(result);
        StackOverflowSearchResult deserialized = objectMapper.readValue(json, StackOverflowSearchResult.class);
        
        // Assert
        assertEquals(result.getQuestionId(), deserialized.getQuestionId());
        assertEquals(result.getTitle(), deserialized.getTitle());
        assertEquals(result.getLink(), deserialized.getLink());
        assertEquals(result.getScore(), deserialized.getScore());
        assertEquals(result.getAnswerCount(), deserialized.getAnswerCount());
        assertEquals(result.getViewCount(), deserialized.getViewCount());
        assertEquals(result.getIsAnswered(), deserialized.getIsAnswered());
        assertEquals(result.getAcceptedAnswerId(), deserialized.getAcceptedAnswerId());
        assertEquals(result.getCreationDate(), deserialized.getCreationDate());
        assertEquals(result.getLastActivityDate(), deserialized.getLastActivityDate());
        assertEquals(result.getTags(), deserialized.getTags());
        assertEquals(result.getRelevanceScore(), deserialized.getRelevanceScore());
        
        // Assert owner
        assertNotNull(deserialized.getOwner());
        assertEquals(result.getOwner().getUserId(), deserialized.getOwner().getUserId());
        assertEquals(result.getOwner().getDisplayName(), deserialized.getOwner().getDisplayName());
        assertEquals(result.getOwner().getReputation(), deserialized.getOwner().getReputation());
        
        // Assert answer
        assertNotNull(deserialized.getBestAnswer());
        assertEquals(result.getBestAnswer().getAnswerId(), deserialized.getBestAnswer().getAnswerId());
        assertEquals(result.getBestAnswer().getBody(), deserialized.getBestAnswer().getBody());
        assertEquals(result.getBestAnswer().getScore(), deserialized.getBestAnswer().getScore());
        assertEquals(result.getBestAnswer().getIsAccepted(), deserialized.getBestAnswer().getIsAccepted());
    }
    
    @Test
    void testStackOverflowApiResponseSerialization() throws Exception {
        // Arrange
        StackOverflowApiResponse response = new StackOverflowApiResponse();
        response.setHasMore(true);
        response.setQuotaRemaining(9500);
        response.setQuotaMax(10000);
        response.setTotal(150);
        response.setPage(1);
        response.setPageSize(10);
        
        List<StackOverflowSearchResult> items = Arrays.asList(
                createSimpleSearchResult(1L, "Question 1"),
                createSimpleSearchResult(2L, "Question 2")
        );
        response.setItems(items);
        
        // Act
        String json = objectMapper.writeValueAsString(response);
        StackOverflowApiResponse deserialized = objectMapper.readValue(json, StackOverflowApiResponse.class);
        
        // Assert
        assertEquals(response.getHasMore(), deserialized.getHasMore());
        assertEquals(response.getQuotaRemaining(), deserialized.getQuotaRemaining());
        assertEquals(response.getQuotaMax(), deserialized.getQuotaMax());
        assertEquals(response.getTotal(), deserialized.getTotal());
        assertEquals(response.getPage(), deserialized.getPage());
        assertEquals(response.getPageSize(), deserialized.getPageSize());
        assertEquals(response.getItems().size(), deserialized.getItems().size());
    }
    
    @Test
    void testStackOverflowSearchRequestBuilder() {
        // Act
        StackOverflowSearchRequest request = StackOverflowSearchRequest.builder()
                .query("Java streams")
                .tags(Arrays.asList("java", "stream"))
                .sort("votes")
                .order("desc")
                .pageSize(10)
                .page(1)
                .minScore(5)
                .maxScore(1000)
                .answeredOnly(true)
                .acceptedAnswerOnly(false)
                .site("stackoverflow")
                .fromDate(1640995200L)
                .toDate(1672531200L)
                .includeBody(true)
                .includeAnswers(true)
                .build();
        
        // Assert
        assertEquals("Java streams", request.getQuery());
        assertEquals(Arrays.asList("java", "stream"), request.getTags());
        assertEquals("votes", request.getSort());
        assertEquals("desc", request.getOrder());
        assertEquals(Integer.valueOf(10), request.getPageSize());
        assertEquals(Integer.valueOf(1), request.getPage());
        assertEquals(Integer.valueOf(5), request.getMinScore());
        assertEquals(Integer.valueOf(1000), request.getMaxScore());
        assertTrue(request.getAnsweredOnly());
        assertFalse(request.getAcceptedAnswerOnly());
        assertEquals("stackoverflow", request.getSite());
        assertEquals(Long.valueOf(1640995200L), request.getFromDate());
        assertEquals(Long.valueOf(1672531200L), request.getToDate());
        assertTrue(request.getIncludeBody());
        assertTrue(request.getIncludeAnswers());
    }
    
    @Test
    void testStackOverflowSearchRequestDefaults() {
        // Act
        StackOverflowSearchRequest request = StackOverflowSearchRequest.builder()
                .query("test")
                .build();
        
        // Assert
        assertEquals("test", request.getQuery());
        assertNull(request.getTags());
        assertNull(request.getSort());
        assertNull(request.getOrder());
        assertNull(request.getPageSize());
        assertNull(request.getPage());
        assertNull(request.getMinScore());
        assertNull(request.getMaxScore());
        assertNull(request.getAnsweredOnly());
        assertNull(request.getAcceptedAnswerOnly());
        assertNull(request.getSite());
        assertNull(request.getFromDate());
        assertNull(request.getToDate());
        assertNull(request.getIncludeBody());
        assertNull(request.getIncludeAnswers());
    }
    
    @Test
    void testStackOverflowSearchResultDefaults() {
        // Act
        StackOverflowSearchResult result = new StackOverflowSearchResult();
        
        // Assert
        assertNull(result.getQuestionId());
        assertNull(result.getTitle());
        assertNull(result.getBody());
        assertNull(result.getLink());
        assertNull(result.getScore());
        assertNull(result.getViewCount());
        assertNull(result.getAnswerCount());
        assertNull(result.getIsAnswered());
        assertNull(result.getAcceptedAnswerId());
        assertNull(result.getCreationDate());
        assertNull(result.getLastActivityDate());
        assertNull(result.getTags());
        assertNull(result.getOwner());
        assertNull(result.getBestAnswer());
        assertNull(result.getRelevanceScore());
    }
    
    @Test
    void testApiResponseWithError() throws Exception {
        // Arrange
        StackOverflowApiResponse response = new StackOverflowApiResponse();
        response.setErrorMessage("Invalid API key");
        response.setErrorId(400);
        response.setErrorName("bad_parameter");
        
        // Act
        String json = objectMapper.writeValueAsString(response);
        StackOverflowApiResponse deserialized = objectMapper.readValue(json, StackOverflowApiResponse.class);
        
        // Assert
        assertEquals("Invalid API key", deserialized.getErrorMessage());
        assertEquals(Integer.valueOf(400), deserialized.getErrorId());
        assertEquals("bad_parameter", deserialized.getErrorName());
    }
    
    @Test
    void testJsonIgnoreUnknownProperties() throws Exception {
        // Arrange
        String jsonWithExtraFields = "{\"question_id\":123,\"title\":\"Test\",\"unknown_field\":\"ignored\",\"extra_data\":{\"nested\":\"value\"}}";
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            StackOverflowSearchResult result = objectMapper.readValue(jsonWithExtraFields, StackOverflowSearchResult.class);
            assertEquals(Long.valueOf(123), result.getQuestionId());
            assertEquals("Test", result.getTitle());
        });
    }
    
    @Test
    void testJsonPropertyMapping() throws Exception {
        // Arrange
        String json = "{\"question_id\":123,\"view_count\":1000,\"answer_count\":5,\"is_answered\":true," +
                     "\"accepted_answer_id\":456,\"creation_date\":1640995200,\"last_activity_date\":1672531200}";
        
        // Act
        StackOverflowSearchResult result = objectMapper.readValue(json, StackOverflowSearchResult.class);
        
        // Assert
        assertEquals(Long.valueOf(123), result.getQuestionId());
        assertEquals(Integer.valueOf(1000), result.getViewCount());
        assertEquals(Integer.valueOf(5), result.getAnswerCount());
        assertTrue(result.getIsAnswered());
        assertEquals(Long.valueOf(456), result.getAcceptedAnswerId());
        assertEquals(Long.valueOf(1640995200), result.getCreationDate());
        assertEquals(Long.valueOf(1672531200), result.getLastActivityDate());
    }
    
    private StackOverflowSearchResult createSimpleSearchResult(Long id, String title) {
        StackOverflowSearchResult result = new StackOverflowSearchResult();
        result.setQuestionId(id);
        result.setTitle(title);
        result.setLink("https://stackoverflow.com/questions/" + id);
        result.setScore(10);
        result.setAnswerCount(2);
        result.setViewCount(500);
        result.setIsAnswered(true);
        return result;
    }
}
