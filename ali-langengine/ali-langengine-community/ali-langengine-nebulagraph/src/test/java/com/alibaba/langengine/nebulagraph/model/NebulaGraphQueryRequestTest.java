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
package com.alibaba.langengine.nebulagraph.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class NebulaGraphQueryRequestTest {

    @Test
    public void testBuilderPattern() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "test");

        NebulaGraphQueryRequest request = NebulaGraphQueryRequest.builder()
            .queryVector(queryVector)
            .topK(10)
            .similarityThreshold(0.8)
            .distanceFunction(NebulaGraphQueryRequest.DistanceFunction.COSINE)
            .includeVector(true)
            .includeMetadata(true)
            .filter(filter)
            .tagName("TestDocument")
            .spaceName("test_space")
            .build();

        assertNotNull(request);
        assertEquals(queryVector, request.getQueryVector());
        assertEquals(10, request.getTopK());
        assertEquals(0.8, request.getSimilarityThreshold());
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.COSINE, request.getDistanceFunction());
        assertTrue(request.isIncludeVector());
        assertTrue(request.isIncludeMetadata());
        assertEquals(filter, request.getFilter());
        assertEquals("TestDocument", request.getTagName());
        assertEquals("test_space", request.getSpaceName());
    }

    @Test
    public void testDefaultValues() {
        NebulaGraphQueryRequest request = new NebulaGraphQueryRequest();

        assertEquals(10, request.getTopK());
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.COSINE, request.getDistanceFunction());
        assertFalse(request.isIncludeVector());
        assertTrue(request.isIncludeMetadata());
        assertEquals("Document", request.getTagName());
    }

    @Test
    public void testDistanceFunctionEnum() {
        // 测试枚举值
        assertEquals("cosine", NebulaGraphQueryRequest.DistanceFunction.COSINE.getValue());
        assertEquals("l2", NebulaGraphQueryRequest.DistanceFunction.L2.getValue());
        assertEquals("inner_product", NebulaGraphQueryRequest.DistanceFunction.INNER_PRODUCT.getValue());

        // 测试fromString方法
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.COSINE,
            NebulaGraphQueryRequest.DistanceFunction.fromString("cosine"));
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.L2,
            NebulaGraphQueryRequest.DistanceFunction.fromString("l2"));
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.INNER_PRODUCT,
            NebulaGraphQueryRequest.DistanceFunction.fromString("inner_product"));

        // 测试大小写不敏感
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.COSINE,
            NebulaGraphQueryRequest.DistanceFunction.fromString("COSINE"));
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.L2,
            NebulaGraphQueryRequest.DistanceFunction.fromString("L2"));

        // 测试无效值
        assertThrows(IllegalArgumentException.class, () -> {
            NebulaGraphQueryRequest.DistanceFunction.fromString("invalid");
        });
    }

    @Test
    public void testValidation() {
        NebulaGraphQueryRequest request = new NebulaGraphQueryRequest();

        // 测试空查询向量
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        // 设置有效的查询向量
        request.setQueryVector(Arrays.asList(0.1, 0.2, 0.3));
        request.setSpaceName("test_space");

        // 测试无效的topK
        request.setTopK(0);
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setTopK(-1);
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setTopK(10); // 重置

        // 测试无效的相似度阈值
        request.setSimilarityThreshold(-0.1);
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setSimilarityThreshold(1.1);
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setSimilarityThreshold(0.8); // 重置

        // 测试空距离函数
        request.setDistanceFunction(null);
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setDistanceFunction(NebulaGraphQueryRequest.DistanceFunction.COSINE); // 重置

        // 测试空标签名
        request.setTagName(null);
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setTagName("");
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setTagName("   ");
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setTagName("Document"); // 重置

        // 测试空图空间名
        request.setSpaceName(null);
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setSpaceName("");
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setSpaceName("   ");
        assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        request.setSpaceName("test_space"); // 重置

        // 现在应该通过验证
        assertDoesNotThrow(() -> {
            request.validate();
        });
    }

    @Test
    public void testBuildQuery() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "test");
        filter.put("priority", 1);

        NebulaGraphQueryRequest request = NebulaGraphQueryRequest.builder()
            .queryVector(queryVector)
            .topK(5)
            .similarityThreshold(0.7)
            .distanceFunction(NebulaGraphQueryRequest.DistanceFunction.COSINE)
            .includeVector(true)
            .includeMetadata(true)
            .filter(filter)
            .tagName("TestDoc")
            .spaceName("test_space")
            .build();

        String query = request.buildQuery();

        assertNotNull(query);
        assertTrue(query.contains("USE test_space"));
        assertTrue(query.contains("LOOKUP ON TestDoc"));
        assertTrue(query.contains("vector_distance"));
        assertTrue(query.contains("0.1, 0.2, 0.3"));
        assertTrue(query.contains(">= 0.7")); // cosine相似度
        assertTrue(query.contains("category"));
        assertTrue(query.contains("priority"));
        assertTrue(query.contains("YIELD"));
        assertTrue(query.contains("unique_id"));
        assertTrue(query.contains("content"));
        assertTrue(query.contains("metadata"));
        assertTrue(query.contains("vector"));
        assertTrue(query.contains("ORDER BY distance DESC")); // cosine降序
        assertTrue(query.contains("LIMIT 5"));
    }

    @Test
    public void testBuildQueryWithL2Distance() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);

        NebulaGraphQueryRequest request = NebulaGraphQueryRequest.builder()
            .queryVector(queryVector)
            .topK(3)
            .similarityThreshold(0.8)
            .distanceFunction(NebulaGraphQueryRequest.DistanceFunction.L2)
            .includeVector(false)
            .includeMetadata(false)
            .tagName("TestDoc")
            .spaceName("test_space")
            .build();

        String query = request.buildQuery();

        assertNotNull(query);
        assertTrue(query.contains("<= 0.19999999999999996")); // 1.0 - 0.8 for L2
        assertTrue(query.contains("ORDER BY distance ASC")); // L2升序
        assertFalse(query.contains("metadata"));
        assertFalse(query.contains("vector AS vector"));
    }

    @Test
    public void testBuildQueryWithoutFilter() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2);

        NebulaGraphQueryRequest request = NebulaGraphQueryRequest.builder()
            .queryVector(queryVector)
            .topK(10)
            .distanceFunction(NebulaGraphQueryRequest.DistanceFunction.INNER_PRODUCT)
            .tagName("Doc")
            .spaceName("space")
            .build();

        String query = request.buildQuery();

        assertNotNull(query);
        assertFalse(query.contains(" AND "));
        assertTrue(query.contains("ORDER BY distance ASC")); // INNER_PRODUCT升序
    }

    @Test
    public void testBuildQueryWithoutSimilarityThreshold() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2);

        NebulaGraphQueryRequest request = NebulaGraphQueryRequest.builder()
            .queryVector(queryVector)
            .topK(5)
            .similarityThreshold(null)
            .distanceFunction(NebulaGraphQueryRequest.DistanceFunction.COSINE)
            .tagName("Doc")
            .spaceName("space")
            .build();

        String query = request.buildQuery();

        assertNotNull(query);
        assertFalse(query.contains(">="));
        assertFalse(query.contains("<="));
    }

    @Test
    public void testSettersAndGetters() {
        NebulaGraphQueryRequest request = new NebulaGraphQueryRequest();

        List<Double> testVector = Arrays.asList(1.0, 2.0, 3.0);
        request.setQueryVector(testVector);
        assertEquals(testVector, request.getQueryVector());

        request.setTopK(15);
        assertEquals(15, request.getTopK());

        request.setSimilarityThreshold(0.9);
        assertEquals(0.9, request.getSimilarityThreshold());

        request.setDistanceFunction(NebulaGraphQueryRequest.DistanceFunction.L2);
        assertEquals(NebulaGraphQueryRequest.DistanceFunction.L2, request.getDistanceFunction());

        request.setIncludeVector(true);
        assertTrue(request.isIncludeVector());

        request.setIncludeMetadata(false);
        assertFalse(request.isIncludeMetadata());

        Map<String, Object> testFilter = new HashMap<>();
        testFilter.put("test", "value");
        request.setFilter(testFilter);
        assertEquals(testFilter, request.getFilter());

        request.setTagName("CustomTag");
        assertEquals("CustomTag", request.getTagName());

        request.setSpaceName("custom_space");
        assertEquals("custom_space", request.getSpaceName());
    }

    @Test
    public void testComplexFilter() {
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> complexFilter = new HashMap<>();
        complexFilter.put("category", "document");
        complexFilter.put("priority", 5);
        complexFilter.put("active", true);
        complexFilter.put("score", 0.95);

        NebulaGraphQueryRequest request = NebulaGraphQueryRequest.builder()
            .queryVector(queryVector)
            .topK(10)
            .distanceFunction(NebulaGraphQueryRequest.DistanceFunction.COSINE)
            .filter(complexFilter)
            .tagName("ComplexDoc")
            .spaceName("complex_space")
            .build();

        String query = request.buildQuery();

        assertNotNull(query);
        assertTrue(query.contains("category == \"document\""));
        assertTrue(query.contains("priority == 5"));
        assertTrue(query.contains("active == true"));
        assertTrue(query.contains("score == 0.95"));
        assertTrue(query.contains(" AND ")); // 多个过滤条件用AND连接
    }
}