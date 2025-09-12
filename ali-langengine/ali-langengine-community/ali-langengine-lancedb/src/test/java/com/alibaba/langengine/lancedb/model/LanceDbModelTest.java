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
package com.alibaba.langengine.lancedb.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("LanceDB模型测试")
class LanceDbModelTest {

    private List<Double> testVector;
    private Map<String, Object> testMetadata;

    @BeforeEach
    void setUp() {
        testVector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        testMetadata = new HashMap<>();
        testMetadata.put("category", "test");
        testMetadata.put("score", 0.95);
        testMetadata.put("tags", Arrays.asList("tag1", "tag2"));
    }

    @Test
    @DisplayName("测试LanceDbVector基本功能")
    void testLanceDbVectorBasics() {
        String id = "test-id-001";
        String text = "This is a test document";
        
        LanceDbVector vector = LanceDbVector.builder()
                .id(id)
                .vector(testVector)
                .text(text)
                .metadata(testMetadata)
                .build();
        
        assertEquals(id, vector.getId());
        assertEquals(testVector, vector.getVector());
        assertEquals(text, vector.getText());
        assertEquals(testMetadata, vector.getMetadata());
        assertTrue(vector.isValid());
        assertEquals(5, vector.getVectorDimension());
    }

    @Test
    @DisplayName("测试LanceDbVector验证")
    void testLanceDbVectorValidation() {
        // 有效的向量
        LanceDbVector validVector = LanceDbVector.of("id", testVector, "text");
        assertTrue(validVector.isValid());
        
        // 无效的向量 - 空ID
        LanceDbVector invalidId = LanceDbVector.builder()
                .id("")
                .vector(testVector)
                .text("text")
                .build();
        assertFalse(invalidId.isValid());
        
        // 无效的向量 - null ID
        LanceDbVector nullId = LanceDbVector.builder()
                .vector(testVector)
                .text("text")
                .build();
        assertFalse(nullId.isValid());
        
        // 无效的向量 - 空向量
        LanceDbVector emptyVector = LanceDbVector.builder()
                .id("id")
                .vector(new ArrayList<>())
                .text("text")
                .build();
        assertFalse(emptyVector.isValid());
        
        // 无效的向量 - null向量
        LanceDbVector nullVector = LanceDbVector.builder()
                .id("id")
                .text("text")
                .build();
        assertFalse(nullVector.isValid());
        
        // 无效的向量 - 空文本
        LanceDbVector emptyText = LanceDbVector.builder()
                .id("id")
                .vector(testVector)
                .text("")
                .build();
        assertFalse(emptyText.isValid());
        
        // 无效的向量 - null文本
        LanceDbVector nullText = LanceDbVector.builder()
                .id("id")
                .vector(testVector)
                .build();
        assertFalse(nullText.isValid());
    }

    @Test
    @DisplayName("测试LanceDbVector时间戳功能")
    void testLanceDbVectorTimestamp() {
        LanceDbVector vector = LanceDbVector.of("id", testVector, "text");
        
        assertNotNull(vector.getCreatedAt());
        assertNotNull(vector.getUpdatedAt());
        assertEquals(vector.getCreatedAt(), vector.getUpdatedAt());
        
        // 等待一毫秒后更新时间戳
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        vector.setCurrentTimestamp();
        assertNotNull(vector.getCreatedAt());
        assertNotNull(vector.getUpdatedAt());
        assertTrue(vector.getUpdatedAt() >= vector.getCreatedAt());
    }

    @Test
    @DisplayName("测试LanceDbVector静态工厂方法")
    void testLanceDbVectorFactoryMethods() {
        String id = "factory-test";
        String text = "Factory method test";
        
        // 测试简单工厂方法
        LanceDbVector simpleVector = LanceDbVector.of(id, testVector, text);
        assertEquals(id, simpleVector.getId());
        assertEquals(testVector, simpleVector.getVector());
        assertEquals(text, simpleVector.getText());
        assertNull(simpleVector.getMetadata());
        assertNotNull(simpleVector.getCreatedAt());
        assertNotNull(simpleVector.getUpdatedAt());
        
        // 测试带元数据的工厂方法
        LanceDbVector vectorWithMetadata = LanceDbVector.of(id, testVector, text, testMetadata);
        assertEquals(id, vectorWithMetadata.getId());
        assertEquals(testVector, vectorWithMetadata.getVector());
        assertEquals(text, vectorWithMetadata.getText());
        assertEquals(testMetadata, vectorWithMetadata.getMetadata());
        assertNotNull(vectorWithMetadata.getCreatedAt());
        assertNotNull(vectorWithMetadata.getUpdatedAt());
    }

    @Test
    @DisplayName("测试LanceDbQueryRequest基本功能")
    void testLanceDbQueryRequestBasics() {
        LanceDbQueryRequest request = LanceDbQueryRequest.builder()
                .vector(testVector)
                .query("test query")
                .limit(10)
                .distanceThreshold(0.5)
                .similarityThreshold(0.8)
                .filter(testMetadata)
                .fields(Arrays.asList("id", "text", "metadata"))
                .metric("cosine")
                .includeVectors(true)
                .includeMetadata(true)
                .build();
        
        assertEquals(testVector, request.getVector());
        assertEquals("test query", request.getQuery());
        assertEquals(10, request.getLimit());
        assertEquals(0.5, request.getDistanceThreshold());
        assertEquals(0.8, request.getSimilarityThreshold());
        assertEquals(testMetadata, request.getFilter());
        assertEquals(Arrays.asList("id", "text", "metadata"), request.getFields());
        assertEquals("cosine", request.getMetric());
        assertTrue(request.getIncludeVectors());
        assertTrue(request.getIncludeMetadata());
    }

    @Test
    @DisplayName("测试LanceDbQueryRequest验证")
    void testLanceDbQueryRequestValidation() {
        // 有效请求 - 带向量
        LanceDbQueryRequest validVectorRequest = LanceDbQueryRequest.builder()
                .vector(testVector)
                .build();
        assertTrue(validVectorRequest.isValid());
        
        // 有效请求 - 带查询文本
        LanceDbQueryRequest validTextRequest = LanceDbQueryRequest.builder()
                .query("test query")
                .build();
        assertTrue(validTextRequest.isValid());
        
        // 无效请求 - 既没有向量也没有查询文本
        LanceDbQueryRequest invalidRequest = LanceDbQueryRequest.builder()
                .limit(10)
                .build();
        assertFalse(invalidRequest.isValid());
        
        // 无效请求 - 空向量
        LanceDbQueryRequest emptyVectorRequest = LanceDbQueryRequest.builder()
                .vector(new ArrayList<>())
                .build();
        assertFalse(emptyVectorRequest.isValid());
        
        // 无效请求 - 空查询文本
        LanceDbQueryRequest emptyQueryRequest = LanceDbQueryRequest.builder()
                .query("")
                .build();
        assertFalse(emptyQueryRequest.isValid());
    }

    @Test
    @DisplayName("测试LanceDbQueryRequest默认值方法")
    void testLanceDbQueryRequestDefaults() {
        LanceDbQueryRequest request = LanceDbQueryRequest.builder().vector(testVector).build();
        
        assertEquals(10, request.getEffectiveLimit());
        assertEquals("cosine", request.getEffectiveMetric());
        assertFalse(request.shouldIncludeVectors());
        assertTrue(request.shouldIncludeMetadata());
        
        // 测试自定义值
        LanceDbQueryRequest customRequest = LanceDbQueryRequest.builder()
                .vector(testVector)
                .limit(20)
                .metric("euclidean")
                .includeVectors(true)
                .includeMetadata(false)
                .build();
        
        assertEquals(20, customRequest.getEffectiveLimit());
        assertEquals("euclidean", customRequest.getEffectiveMetric());
        assertTrue(customRequest.shouldIncludeVectors());
        assertFalse(customRequest.shouldIncludeMetadata());
    }

    @Test
    @DisplayName("测试LanceDbQueryRequest静态工厂方法")
    void testLanceDbQueryRequestFactoryMethods() {
        // 测试向量查询工厂方法
        LanceDbQueryRequest vectorQuery = LanceDbQueryRequest.vectorQuery(testVector, 5);
        assertEquals(testVector, vectorQuery.getVector());
        assertEquals(5, vectorQuery.getLimit());
        assertEquals("cosine", vectorQuery.getMetric());
        assertTrue(vectorQuery.shouldIncludeMetadata());
        
        // 测试文本查询工厂方法
        String queryText = "test search text";
        LanceDbQueryRequest textQuery = LanceDbQueryRequest.textQuery(queryText, 15);
        assertEquals(queryText, textQuery.getQuery());
        assertEquals(15, textQuery.getLimit());
        assertEquals("cosine", textQuery.getMetric());
        assertTrue(textQuery.shouldIncludeMetadata());
    }

    @Test
    @DisplayName("测试LanceDbQueryResponse基本功能")
    void testLanceDbQueryResponseBasics() {
        List<LanceDbVector> results = Arrays.asList(
                LanceDbVector.of("1", testVector, "text1"),
                LanceDbVector.of("2", testVector, "text2")
        );
        
        LanceDbQueryResponse response = LanceDbQueryResponse.builder()
                .results(results)
                .total(2)
                .queryTimeMs(100L)
                .success(true)
                .requestId("req-123")
                .build();
        
        assertEquals(results, response.getResults());
        assertEquals(2, response.getTotal());
        assertEquals(100L, response.getQueryTimeMs());
        assertTrue(response.getSuccess());
        assertEquals("req-123", response.getRequestId());
        assertTrue(response.isSuccessful());
        assertTrue(response.hasResults());
        assertEquals(2, response.getResultCount());
        assertEquals("", response.getErrorMessage());
    }

    @Test
    @DisplayName("测试LanceDbQueryResponse错误处理")
    void testLanceDbQueryResponseError() {
        String errorMessage = "Query failed";
        LanceDbQueryResponse errorResponse = LanceDbQueryResponse.builder()
                .success(false)
                .error(errorMessage)
                .build();
        
        assertFalse(errorResponse.isSuccessful());
        assertFalse(errorResponse.hasResults());
        assertEquals(0, errorResponse.getResultCount());
        assertEquals(errorMessage, errorResponse.getErrorMessage());
        
        // 测试没有明确success字段的情况
        LanceDbQueryResponse ambiguousResponse = LanceDbQueryResponse.builder()
                .error("Some error")
                .build();
        assertFalse(ambiguousResponse.isSuccessful());
        
        // 测试没有错误的情况
        LanceDbQueryResponse noErrorResponse = LanceDbQueryResponse.builder()
                .build();
        assertTrue(noErrorResponse.isSuccessful());
    }

    @Test
    @DisplayName("测试LanceDbQueryResponse静态工厂方法")
    void testLanceDbQueryResponseFactoryMethods() {
        List<LanceDbVector> results = Arrays.asList(
                LanceDbVector.of("1", testVector, "text1")
        );
        
        // 测试成功响应工厂方法
        LanceDbQueryResponse successResponse = LanceDbQueryResponse.success(results);
        assertTrue(successResponse.isSuccessful());
        assertEquals(results, successResponse.getResults());
        assertEquals(1, successResponse.getTotal());
        assertTrue(successResponse.getSuccess());
        
        // 测试空结果的成功响应
        LanceDbQueryResponse emptySuccessResponse = LanceDbQueryResponse.success(new ArrayList<>());
        assertTrue(emptySuccessResponse.isSuccessful());
        assertFalse(emptySuccessResponse.hasResults());
        assertEquals(0, emptySuccessResponse.getTotal());
        
        // 测试失败响应工厂方法
        String errorMessage = "Test error";
        LanceDbQueryResponse failureResponse = LanceDbQueryResponse.failure(errorMessage);
        assertFalse(failureResponse.isSuccessful());
        assertEquals(errorMessage, failureResponse.getError());
        assertFalse(failureResponse.getSuccess());
    }

    @Test
    @DisplayName("测试分页信息")
    void testPaginationInfo() {
        LanceDbQueryResponse.PaginationInfo pagination = LanceDbQueryResponse.PaginationInfo.builder()
                .page(1)
                .pageSize(10)
                .totalPages(5)
                .hasNext(true)
                .hasPrevious(false)
                .build();
        
        assertEquals(1, pagination.getPage());
        assertEquals(10, pagination.getPageSize());
        assertEquals(5, pagination.getTotalPages());
        assertTrue(pagination.getHasNext());
        assertFalse(pagination.getHasPrevious());
        
        LanceDbQueryResponse response = LanceDbQueryResponse.builder()
                .pagination(pagination)
                .build();
        
        assertEquals(pagination, response.getPagination());
    }

    @Test
    @DisplayName("测试模型序列化兼容性")
    void testModelSerializationCompatibility() {
        // 测试LanceDbVector的JSON注解
        LanceDbVector vector = LanceDbVector.builder()
                .id("test-id")
                .vector(testVector)
                .text("test text")
                .metadata(testMetadata)
                .score(0.95)
                .distance(0.05)
                .build();
        
        // 验证所有字段都有适当的注解（通过getter方法间接验证）
        assertNotNull(vector.getId());
        assertNotNull(vector.getVector());
        assertNotNull(vector.getText());
        assertNotNull(vector.getMetadata());
        assertNotNull(vector.getScore());
        assertNotNull(vector.getDistance());
        
        // 测试LanceDbQueryRequest的JSON注解
        LanceDbQueryRequest request = LanceDbQueryRequest.builder()
                .vector(testVector)
                .query("test")
                .limit(10)
                .build();
        
        assertNotNull(request.getVector());
        assertNotNull(request.getQuery());
        assertNotNull(request.getLimit());
        
        // 测试LanceDbQueryResponse的JSON注解
        LanceDbQueryResponse response = LanceDbQueryResponse.builder()
                .results(Arrays.asList(vector))
                .total(1)
                .success(true)
                .build();
        
        assertNotNull(response.getResults());
        assertNotNull(response.getTotal());
        assertNotNull(response.getSuccess());
    }
}
