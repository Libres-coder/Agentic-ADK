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
package com.alibaba.langengine.influxdb.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("InfluxDB模型测试")
class InfluxDBModelTest {

    @Test
    @DisplayName("测试InfluxDBVector构建")
    void testInfluxDBVectorBuilder() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        Map<String, Object> metadata = Map.of("type", "test", "version", 1);
        Map<String, String> tags = Map.of("category", "docs", "lang", "en");
        Instant timestamp = Instant.now();

        InfluxDBVector influxDBVector = InfluxDBVector.builder()
                .id("test-id")
                .vector(vector)
                .content("Test content")
                .metadata(metadata)
                .tags(tags)
                .measurement("test_measurement")
                .timestamp(timestamp)
                .build();

        assertEquals("test-id", influxDBVector.getId());
        assertEquals(vector, influxDBVector.getVector());
        assertEquals("Test content", influxDBVector.getContent());
        assertEquals(metadata, influxDBVector.getMetadata());
        assertEquals(tags, influxDBVector.getTags());
        assertEquals("test_measurement", influxDBVector.getMeasurement());
        assertEquals(timestamp, influxDBVector.getTimestamp());
    }

    @Test
    @DisplayName("测试InfluxDBVector验证")
    void testInfluxDBVectorValidation() {
        // 测试必需字段
        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBVector.builder().build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBVector.builder().id("").build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBVector.builder().id("test").vector(null).build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBVector.builder().id("test").vector(Collections.emptyList()).build());

        // 测试有效构建
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        InfluxDBVector validVector = InfluxDBVector.builder()
                .id("test-id")
                .vector(vector)
                .content("Test content")
                .build();

        assertNotNull(validVector);
        assertTrue(validVector.isValid());
    }

    @Test
    @DisplayName("测试InfluxDBVector默认值")
    void testInfluxDBVectorDefaults() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        InfluxDBVector influxDBVector = InfluxDBVector.builder()
                .id("test-id")
                .vector(vector)
                .content("Test content")
                .build();

        assertNotNull(influxDBVector.getMetadata());
        assertTrue(influxDBVector.getMetadata().isEmpty());
        assertNotNull(influxDBVector.getTags());
        assertTrue(influxDBVector.getTags().isEmpty());
        assertEquals("vectors", influxDBVector.getMeasurement());
        assertNotNull(influxDBVector.getTimestamp());
    }

    @Test
    @DisplayName("测试InfluxDBQueryRequest构建")
    void testInfluxDBQueryRequestBuilder() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        Map<String, String> tagFilters = Map.of("category", "docs");
        Instant startTime = Instant.now().minusSeconds(3600);
        Instant endTime = Instant.now();

        InfluxDBQueryRequest request = InfluxDBQueryRequest.builder()
                .queryVector(queryVector)
                .limit(10)
                .similarityThreshold(0.8)
                .similarityMetric(InfluxDBQueryRequest.SimilarityMetric.COSINE)
                .measurement("test_measurement")
                .tagFilters(tagFilters)
                .timeRangeStart(startTime)
                .timeRangeEnd(endTime)
                .includeMetadata(true)
                .includeContent(true)
                .build();

        assertEquals(queryVector, request.getQueryVector());
        assertEquals(10, request.getLimit());
        assertEquals(0.8, request.getSimilarityThreshold());
        assertEquals(InfluxDBQueryRequest.SimilarityMetric.COSINE, request.getSimilarityMetric());
        assertEquals("test_measurement", request.getMeasurement());
        assertEquals(tagFilters, request.getTagFilters());
        assertEquals(startTime, request.getTimeRangeStart());
        assertEquals(endTime, request.getTimeRangeEnd());
        assertTrue(request.isIncludeMetadata());
        assertTrue(request.isIncludeContent());
    }

    @Test
    @DisplayName("测试InfluxDBQueryRequest验证")
    void testInfluxDBQueryRequestValidation() {
        // 测试必需字段
        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.builder().build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.builder().queryVector(null).build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.builder().queryVector(Collections.emptyList()).build());

        // 测试无效参数
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        
        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.builder()
                        .queryVector(vector)
                        .limit(0)
                        .build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.builder()
                        .queryVector(vector)
                        .limit(-1)
                        .build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.builder()
                        .queryVector(vector)
                        .similarityThreshold(-0.1)
                        .build());

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.builder()
                        .queryVector(vector)
                        .similarityThreshold(1.1)
                        .build());
    }

    @Test
    @DisplayName("测试InfluxDBQueryRequest默认值")
    void testInfluxDBQueryRequestDefaults() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        InfluxDBQueryRequest request = InfluxDBQueryRequest.builder()
                .queryVector(vector)
                .build();

        assertEquals(10, request.getLimit());
        assertEquals(0.0, request.getSimilarityThreshold());
        assertEquals(InfluxDBQueryRequest.SimilarityMetric.COSINE, request.getSimilarityMetric());
        assertEquals("vectors", request.getMeasurement());
        assertNotNull(request.getTagFilters());
        assertTrue(request.getTagFilters().isEmpty());
        assertNull(request.getTimeRangeStart());
        assertNull(request.getTimeRangeEnd());
        assertTrue(request.isIncludeMetadata());
        assertTrue(request.isIncludeContent());
    }

    @Test
    @DisplayName("测试SimilarityMetric枚举")
    void testSimilarityMetricEnum() {
        assertEquals(4, InfluxDBQueryRequest.SimilarityMetric.values().length);
        
        assertEquals("cosine", InfluxDBQueryRequest.SimilarityMetric.COSINE.getValue());
        assertEquals("dot_product", InfluxDBQueryRequest.SimilarityMetric.DOT_PRODUCT.getValue());
        assertEquals("euclidean", InfluxDBQueryRequest.SimilarityMetric.EUCLIDEAN.getValue());
        assertEquals("manhattan", InfluxDBQueryRequest.SimilarityMetric.MANHATTAN.getValue());

        // 测试fromValue方法
        assertEquals(InfluxDBQueryRequest.SimilarityMetric.COSINE, 
                InfluxDBQueryRequest.SimilarityMetric.fromValue("cosine"));
        assertEquals(InfluxDBQueryRequest.SimilarityMetric.DOT_PRODUCT, 
                InfluxDBQueryRequest.SimilarityMetric.fromValue("dot_product"));
        assertEquals(InfluxDBQueryRequest.SimilarityMetric.EUCLIDEAN, 
                InfluxDBQueryRequest.SimilarityMetric.fromValue("euclidean"));
        assertEquals(InfluxDBQueryRequest.SimilarityMetric.MANHATTAN, 
                InfluxDBQueryRequest.SimilarityMetric.fromValue("manhattan"));

        assertThrows(IllegalArgumentException.class, () -> 
                InfluxDBQueryRequest.SimilarityMetric.fromValue("invalid"));
    }

    @Test
    @DisplayName("测试InfluxDBQueryResponse构建")
    void testInfluxDBQueryResponseBuilder() {
        List<InfluxDBVector> results = createTestVectors(3);
        long queryTime = 150;
        int totalResults = 100;
        boolean hasMore = true;

        InfluxDBQueryResponse response = InfluxDBQueryResponse.builder()
                .results(results)
                .success(true)
                .queryTimeMs(queryTime)
                .totalResults(totalResults)
                .returnedResults(results.size())
                .hasMore(hasMore)
                .build();

        assertEquals(results, response.getResults());
        assertTrue(response.isSuccess());
        assertEquals(queryTime, response.getQueryTimeMs());
        assertEquals(totalResults, response.getTotalResults());
        assertEquals(results.size(), response.getReturnedResults());
        assertTrue(response.isHasMore());
    }

    @Test
    @DisplayName("测试InfluxDBQueryResponse错误响应")
    void testInfluxDBQueryResponseError() {
        String errorMessage = "Query failed";
        String errorCode = "QUERY_ERROR";

        InfluxDBQueryResponse response = InfluxDBQueryResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();

        assertFalse(response.isSuccess());
        assertEquals(errorMessage, response.getErrorMessage());
        assertEquals(errorCode, response.getErrorCode());
        assertNotNull(response.getResults());
        assertTrue(response.getResults().isEmpty());
        assertEquals(0, response.getReturnedResults());
    }

    @Test
    @DisplayName("测试InfluxDBQueryResponse分页信息")
    void testInfluxDBQueryResponsePagination() {
        List<InfluxDBVector> results = createTestVectors(10);
        
        InfluxDBQueryResponse response = InfluxDBQueryResponse.builder()
                .results(results)
                .success(true)
                .totalResults(100)
                .returnedResults(10)
                .hasMore(true)
                .page(1)
                .pageSize(10)
                .build();

        assertEquals(100, response.getTotalResults());
        assertEquals(10, response.getReturnedResults());
        assertTrue(response.isHasMore());
        assertEquals(1, response.getPage());
        assertEquals(10, response.getPageSize());
        assertEquals(10, response.getTotalPages());
    }

    @Test
    @DisplayName("测试模型equals和hashCode")
    void testModelEqualsAndHashCode() {
        List<Double> vector1 = Arrays.asList(0.1, 0.2, 0.3);
        List<Double> vector2 = Arrays.asList(0.1, 0.2, 0.3);
        List<Double> vector3 = Arrays.asList(0.4, 0.5, 0.6);

        InfluxDBVector vector1Obj = InfluxDBVector.builder()
                .id("test-1")
                .vector(vector1)
                .content("Content 1")
                .build();

        InfluxDBVector vector2Obj = InfluxDBVector.builder()
                .id("test-1")
                .vector(vector2)
                .content("Content 1")
                .build();

        InfluxDBVector vector3Obj = InfluxDBVector.builder()
                .id("test-2")
                .vector(vector3)
                .content("Content 2")
                .build();

        assertEquals(vector1Obj, vector2Obj);
        assertEquals(vector1Obj.hashCode(), vector2Obj.hashCode());
        assertNotEquals(vector1Obj, vector3Obj);
        assertNotEquals(vector1Obj.hashCode(), vector3Obj.hashCode());
    }

    @Test
    @DisplayName("测试模型toString")
    void testModelToString() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        InfluxDBVector influxDBVector = InfluxDBVector.builder()
                .id("test-id")
                .vector(vector)
                .content("Test content")
                .build();

        String toString = influxDBVector.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test-id"));
        assertTrue(toString.contains("Test content"));
    }

    @Test
    @DisplayName("测试模型复制")
    void testModelCopy() {
        List<Double> vector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");

        InfluxDBVector original = InfluxDBVector.builder()
                .id("test-id")
                .vector(vector)
                .content("Test content")
                .metadata(metadata)
                .build();

        InfluxDBVector copy = original.copy();

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVector(), copy.getVector());
        assertEquals(original.getContent(), copy.getContent());
        assertEquals(original.getMetadata(), copy.getMetadata());

        // 验证是深拷贝
        copy.getMetadata().put("newKey", "newValue");
        assertNotEquals(original.getMetadata().size(), copy.getMetadata().size());
    }

    @Test
    @DisplayName("测试向量维度计算")
    void testVectorDimensions() {
        List<Double> vector128 = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            vector128.add((double) i);
        }

        List<Double> vector1536 = new ArrayList<>();
        for (int i = 0; i < 1536; i++) {
            vector1536.add((double) i);
        }

        InfluxDBVector vector1 = InfluxDBVector.builder()
                .id("test-128")
                .vector(vector128)
                .content("128-dim vector")
                .build();

        InfluxDBVector vector2 = InfluxDBVector.builder()
                .id("test-1536")
                .vector(vector1536)
                .content("1536-dim vector")
                .build();

        assertEquals(128, vector1.getDimensions());
        assertEquals(1536, vector2.getDimensions());
    }

    /**
     * 创建测试向量列表
     */
    private List<InfluxDBVector> createTestVectors(int count) {
        List<InfluxDBVector> vectors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            List<Double> vector = Arrays.asList(
                    (double) i, (double) i + 0.1, (double) i + 0.2
            );
            
            InfluxDBVector influxDBVector = InfluxDBVector.builder()
                    .id("test-" + i)
                    .vector(vector)
                    .content("Test content " + i)
                    .metadata(Map.of("index", i))
                    .build();
            vectors.add(influxDBVector);
        }
        return vectors;
    }
}
