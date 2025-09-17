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
package com.alibaba.langengine.dgraph.vectorstore;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class DgraphParamTest {

    @Test
    void testDefaultConstructor() {
        // When
        DgraphParam param = new DgraphParam();

        // Then
        assertEquals("vector_embedding", param.getVectorFieldName());
        assertEquals("content", param.getContentFieldName());
        assertEquals("metadata", param.getMetadataFieldName());
        assertEquals("uid", param.getIdFieldName());
        assertEquals(1536, param.getVectorDimension());
        assertEquals("cosine", param.getSimilarityAlgorithm());
        assertEquals(10, param.getSearchLimit());
        assertEquals(100, param.getBatchSize());
        assertEquals(0.5f, param.getSimilarityThreshold());
        assertNotNull(param.getSearchParams());
        assertTrue(param.getSearchParams().isEmpty());
        assertNotNull(param.getPredicateMapping());
        assertFalse(param.getPredicateMapping().isEmpty());
        assertTrue(param.isTransactionEnabled());
        assertEquals(30000, param.getQueryTimeoutMs());
    }

    @Test
    void testDefaultPredicateMappingInitialization() {
        // When
        DgraphParam param = new DgraphParam();

        // Then
        Map<String, String> predicateMapping = param.getPredicateMapping();
        assertEquals("vector_embedding", predicateMapping.get("vector"));
        assertEquals("content", predicateMapping.get("content"));
        assertEquals("metadata", predicateMapping.get("metadata"));
        assertEquals("uid", predicateMapping.get("id"));
    }

    @Test
    void testBuilderPattern() {
        // Given
        Map<String, Object> customSearchParams = new HashMap<>();
        customSearchParams.put("max_results", 50);
        customSearchParams.put("include_scores", true);

        // When
        DgraphParam param = new DgraphParam.Builder()
                .vectorFieldName("custom_vector")
                .contentFieldName("custom_content")
                .metadataFieldName("custom_metadata")
                .vectorDimension(768)
                .similarityAlgorithm("euclidean")
                .searchLimit(20)
                .batchSize(50)
                .similarityThreshold(0.7f)
                .searchParams(customSearchParams)
                .transactionEnabled(false)
                .queryTimeoutMs(15000)
                .build();

        // Then
        assertEquals("custom_vector", param.getVectorFieldName());
        assertEquals("custom_content", param.getContentFieldName());
        assertEquals("custom_metadata", param.getMetadataFieldName());
        assertEquals(768, param.getVectorDimension());
        assertEquals("euclidean", param.getSimilarityAlgorithm());
        assertEquals(20, param.getSearchLimit());
        assertEquals(50, param.getBatchSize());
        assertEquals(0.7f, param.getSimilarityThreshold());
        assertEquals(customSearchParams, param.getSearchParams());
        assertFalse(param.isTransactionEnabled());
        assertEquals(15000, param.getQueryTimeoutMs());
    }

    @Test
    void testBuilderChaining() {
        // When
        DgraphParam param = new DgraphParam.Builder()
                .vectorDimension(1024)
                .similarityAlgorithm("dotproduct")
                .searchLimit(15)
                .build();

        // Then
        assertEquals(1024, param.getVectorDimension());
        assertEquals("dotproduct", param.getSimilarityAlgorithm());
        assertEquals(15, param.getSearchLimit());
        // 其他字段应该保持默认值
        assertEquals("vector_embedding", param.getVectorFieldName());
        assertEquals("content", param.getContentFieldName());
        assertEquals(100, param.getBatchSize());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        DgraphParam param = new DgraphParam();

        // When
        param.setVectorFieldName("test_vector");
        param.setContentFieldName("test_content");
        param.setMetadataFieldName("test_metadata");
        param.setIdFieldName("test_id");
        param.setVectorDimension(512);
        param.setSimilarityAlgorithm("manhattan");
        param.setSearchLimit(25);
        param.setBatchSize(75);
        param.setSimilarityThreshold(0.8f);
        param.setTransactionEnabled(false);
        param.setQueryTimeoutMs(20000);

        Map<String, Object> newSearchParams = new HashMap<>();
        newSearchParams.put("custom_param", "value");
        param.setSearchParams(newSearchParams);

        Map<String, String> newPredicateMapping = new HashMap<>();
        newPredicateMapping.put("custom", "mapping");
        param.setPredicateMapping(newPredicateMapping);

        // Then
        assertEquals("test_vector", param.getVectorFieldName());
        assertEquals("test_content", param.getContentFieldName());
        assertEquals("test_metadata", param.getMetadataFieldName());
        assertEquals("test_id", param.getIdFieldName());
        assertEquals(512, param.getVectorDimension());
        assertEquals("manhattan", param.getSimilarityAlgorithm());
        assertEquals(25, param.getSearchLimit());
        assertEquals(75, param.getBatchSize());
        assertEquals(0.8f, param.getSimilarityThreshold());
        assertEquals(newSearchParams, param.getSearchParams());
        assertEquals(newPredicateMapping, param.getPredicateMapping());
        assertFalse(param.isTransactionEnabled());
        assertEquals(20000, param.getQueryTimeoutMs());
    }

    @Test
    void testBuilderImmutability() {
        // Given
        DgraphParam.Builder builder = new DgraphParam.Builder();

        // When
        DgraphParam param1 = builder.vectorDimension(256).build();
        DgraphParam param2 = builder.vectorDimension(512).build();

        // Then
        // 验证每次build都会创建新的独立实例
        assertNotSame(param1, param2, "build()应该返回不同的实例");
        
        // 由于Builder是可变的，第二次修改会影响后续build的结果
        // 但已经build的实例应该保持独立
        assertEquals(256, param1.getVectorDimension()); // param1应该保持第一次build时的值
        assertEquals(512, param2.getVectorDimension()); // param2应该是第二次build时的值
    }

    @Test
    void testEdgeCases() {
        // Test with zero and negative values
        DgraphParam param = new DgraphParam.Builder()
                .vectorDimension(0)
                .searchLimit(-1)
                .batchSize(0)
                .similarityThreshold(-0.5f)
                .queryTimeoutMs(0)
                .build();

        assertEquals(0, param.getVectorDimension());
        assertEquals(-1, param.getSearchLimit());
        assertEquals(0, param.getBatchSize());
        assertEquals(-0.5f, param.getSimilarityThreshold());
        assertEquals(0, param.getQueryTimeoutMs());
    }

    @Test
    void testNullSearchParams() {
        // Given
        DgraphParam param = new DgraphParam.Builder()
                .searchParams(null)
                .build();

        // Then
        assertNull(param.getSearchParams());
    }

    @Test
    void testEmptyStrings() {
        // Given
        DgraphParam param = new DgraphParam.Builder()
                .vectorFieldName("")
                .contentFieldName("")
                .metadataFieldName("")
                .similarityAlgorithm("")
                .build();

        // Then
        assertEquals("", param.getVectorFieldName());
        assertEquals("", param.getContentFieldName());
        assertEquals("", param.getMetadataFieldName());
        assertEquals("", param.getSimilarityAlgorithm());
    }

    @Test
    void testBuilderReusability() {
        // Given
        DgraphParam.Builder builder = new DgraphParam.Builder()
                .vectorDimension(384)
                .similarityAlgorithm("cosine");

        // When
        DgraphParam param1 = builder.searchLimit(5).build();
        DgraphParam param2 = builder.searchLimit(10).build();

        // Then
        // 验证 builder 可以重用，且每次build都创建独立实例
        assertNotSame(param1, param2, "每次build应该返回不同的实例");
        
        assertEquals(384, param1.getVectorDimension());
        assertEquals(384, param2.getVectorDimension());
        assertEquals("cosine", param1.getSimilarityAlgorithm());
        assertEquals("cosine", param2.getSimilarityAlgorithm());
        
        // 关键测试：验证每个实例保持构建时的状态
        assertEquals(5, param1.getSearchLimit(), "param1应该保持第一次build时的searchLimit值");
        assertEquals(10, param2.getSearchLimit(), "param2应该有第二次build时的searchLimit值");
    }
}
