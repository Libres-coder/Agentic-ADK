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
package com.alibaba.langengine.clickhouse.vectorstore;

import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClickHouseParamTest {

    @Test
    @Order(1)
    public void test_default_parameters() {
        System.out.println("=== Testing Default Parameters ===");
        
        ClickHouseParam param = new ClickHouseParam();
        
        // 测试默认字段名
        assertEquals("langengine_clickhouse_collection", param.getTableName());
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("metadata", param.getFieldNameMetadata());
        assertEquals("embedding", param.getFieldNameEmbedding());
        assertEquals("score", param.getFieldNameScore());
        
        // 测试默认配置
        assertEquals(1000, param.getBatchSize());
        assertEquals(30000, param.getConnectionTimeout());
        assertEquals(60000, param.getQueryTimeout());
        assertEquals(10, param.getMaxPoolSize());
        
        // 测试初始化参数默认值
        ClickHouseParam.InitParam initParam = param.getInitParam();
        assertNotNull(initParam);
        assertEquals(1536, initParam.getVectorDimensions());
        assertEquals(ClickHouseSimilarityFunction.COSINE, initParam.getSimilarityFunction());
        assertEquals("MergeTree", initParam.getEngineType());
        assertEquals("content_id", initParam.getOrderBy());
        assertEquals("", initParam.getPartitionBy());
        assertEquals(8192, initParam.getIndexGranularity());
        assertTrue(initParam.isCreateVectorIndex());
        assertEquals("annoy", initParam.getVectorIndexType());
        assertTrue(initParam.isUseUniqueIdAsPrimaryKey());
        assertEquals(65536, initParam.getPageContentMaxLength());
        assertEquals(32768, initParam.getMetadataMaxLength());
        
        System.out.println("Default parameters test: SUCCESS");
    }

    @Test
    @Order(2)
    public void test_custom_parameters() {
        System.out.println("=== Testing Custom Parameters ===");
        
        ClickHouseParam param = new ClickHouseParam();
        
        // 自定义基本参数
        param.setTableName("custom_table");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNameMetadata("custom_meta");
        param.setFieldNameEmbedding("custom_vector");
        param.setFieldNameScore("custom_score");
        param.setBatchSize(500);
        param.setConnectionTimeout(60000);
        param.setQueryTimeout(120000);
        param.setMaxPoolSize(20);
        
        // 验证自定义参数
        assertEquals("custom_table", param.getTableName());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_meta", param.getFieldNameMetadata());
        assertEquals("custom_vector", param.getFieldNameEmbedding());
        assertEquals("custom_score", param.getFieldNameScore());
        assertEquals(500, param.getBatchSize());
        assertEquals(60000, param.getConnectionTimeout());
        assertEquals(120000, param.getQueryTimeout());
        assertEquals(20, param.getMaxPoolSize());
        
        // 自定义初始化参数
        ClickHouseParam.InitParam initParam = param.getInitParam();
        initParam.setVectorDimensions(768);
        initParam.setSimilarityFunction(ClickHouseSimilarityFunction.L2);
        initParam.setEngineType("ReplacingMergeTree");
        initParam.setOrderBy("custom_id, timestamp");
        initParam.setPartitionBy("toYYYYMM(timestamp)");
        initParam.setIndexGranularity(4096);
        initParam.setCreateVectorIndex(false);
        initParam.setVectorIndexType("hnsw");
        initParam.setUseUniqueIdAsPrimaryKey(false);
        initParam.setPageContentMaxLength(32768);
        initParam.setMetadataMaxLength(16384);
        
        // 验证自定义初始化参数
        assertEquals(768, initParam.getVectorDimensions());
        assertEquals(ClickHouseSimilarityFunction.L2, initParam.getSimilarityFunction());
        assertEquals("ReplacingMergeTree", initParam.getEngineType());
        assertEquals("custom_id, timestamp", initParam.getOrderBy());
        assertEquals("toYYYYMM(timestamp)", initParam.getPartitionBy());
        assertEquals(4096, initParam.getIndexGranularity());
        assertFalse(initParam.isCreateVectorIndex());
        assertEquals("hnsw", initParam.getVectorIndexType());
        assertFalse(initParam.isUseUniqueIdAsPrimaryKey());
        assertEquals(32768, initParam.getPageContentMaxLength());
        assertEquals(16384, initParam.getMetadataMaxLength());
        
        System.out.println("Custom parameters test: SUCCESS");
    }

    @Test
    @Order(3)
    public void test_vector_index_parameters() {
        System.out.println("=== Testing Vector Index Parameters ===");
        
        ClickHouseParam param = new ClickHouseParam();
        ClickHouseParam.InitParam initParam = param.getInitParam();
        
        // 测试默认向量索引参数
        Map<String, Object> defaultIndexParams = initParam.getVectorIndexParams();
        assertNotNull(defaultIndexParams);
        assertEquals(100, defaultIndexParams.get("num_trees"));
        
        // 测试自定义向量索引参数
        Map<String, Object> customIndexParams = new HashMap<>();
        customIndexParams.put("num_trees", 200);
        customIndexParams.put("search_k", 1000);
        customIndexParams.put("build_policy", "auto");
        
        initParam.setVectorIndexParams(customIndexParams);
        
        assertEquals(200, initParam.getVectorIndexParams().get("num_trees"));
        assertEquals(1000, initParam.getVectorIndexParams().get("search_k"));
        assertEquals("auto", initParam.getVectorIndexParams().get("build_policy"));
        
        System.out.println("Vector index parameters test: SUCCESS");
    }

    @Test
    @Order(4)
    public void test_similarity_function_enum() {
        System.out.println("=== Testing Similarity Function Enum ===");
        
        // 测试枚举值
        assertEquals("cosineDistance", ClickHouseSimilarityFunction.COSINE.getFunctionName());
        assertEquals("L2Distance", ClickHouseSimilarityFunction.L2.getFunctionName());
        assertEquals("L1Distance", ClickHouseSimilarityFunction.L1.getFunctionName());
        assertEquals("LinfDistance", ClickHouseSimilarityFunction.LINF.getFunctionName());
        assertEquals("dotProduct", ClickHouseSimilarityFunction.DOT_PRODUCT.getFunctionName());
        
        // 测试fromValue方法
        assertEquals(ClickHouseSimilarityFunction.COSINE, ClickHouseSimilarityFunction.fromValue("cosineDistance"));
        assertEquals(ClickHouseSimilarityFunction.L2, ClickHouseSimilarityFunction.fromValue("L2Distance"));
        assertEquals(ClickHouseSimilarityFunction.L1, ClickHouseSimilarityFunction.fromValue("l1"));
        assertEquals(ClickHouseSimilarityFunction.LINF, ClickHouseSimilarityFunction.fromValue("LINF"));
        assertEquals(ClickHouseSimilarityFunction.DOT_PRODUCT, ClickHouseSimilarityFunction.fromValue("dot_product"));
        
        // 测试null值返回默认值
        assertEquals(ClickHouseSimilarityFunction.COSINE, ClickHouseSimilarityFunction.fromValue(null));
        
        // 测试无效值抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            ClickHouseSimilarityFunction.fromValue("invalid_function");
        });
        
        // 测试距离函数判断
        assertTrue(ClickHouseSimilarityFunction.COSINE.isDistanceFunction());
        assertTrue(ClickHouseSimilarityFunction.L2.isDistanceFunction());
        assertTrue(ClickHouseSimilarityFunction.L1.isDistanceFunction());
        assertTrue(ClickHouseSimilarityFunction.LINF.isDistanceFunction());
        assertFalse(ClickHouseSimilarityFunction.DOT_PRODUCT.isDistanceFunction());
        
        // 测试toString方法
        assertEquals("cosineDistance", ClickHouseSimilarityFunction.COSINE.toString());
        assertEquals("dotProduct", ClickHouseSimilarityFunction.DOT_PRODUCT.toString());
        
        System.out.println("Similarity function enum test: SUCCESS");
    }

    @Test
    @Order(5)
    public void test_parameter_validation() {
        System.out.println("=== Testing Parameter Validation ===");
        
        ClickHouseParam param = new ClickHouseParam();
        ClickHouseParam.InitParam initParam = param.getInitParam();
        
        // 测试边界值
        initParam.setVectorDimensions(1);
        assertEquals(1, initParam.getVectorDimensions());
        
        initParam.setVectorDimensions(10000);
        assertEquals(10000, initParam.getVectorDimensions());
        
        // 测试批量大小
        param.setBatchSize(1);
        assertEquals(1, param.getBatchSize());
        
        param.setBatchSize(10000);
        assertEquals(10000, param.getBatchSize());
        
        // 测试超时时间
        param.setConnectionTimeout(1000);
        assertEquals(1000, param.getConnectionTimeout());
        
        param.setQueryTimeout(300000);
        assertEquals(300000, param.getQueryTimeout());
        
        // 测试字符串字段长度
        initParam.setPageContentMaxLength(1024);
        assertEquals(1024, initParam.getPageContentMaxLength());
        
        initParam.setMetadataMaxLength(2048);
        assertEquals(2048, initParam.getMetadataMaxLength());
        
        System.out.println("Parameter validation test: SUCCESS");
    }

    /**
     * 运行所有测试的主方法
     */
    public static void main(String[] args) {
        System.out.println("Running ClickHouse Parameter Tests...\n");
        
        try {
            ClickHouseParamTest test = new ClickHouseParamTest();
            test.test_default_parameters();
            test.test_custom_parameters();
            test.test_vector_index_parameters();
            test.test_similarity_function_enum();
            test.test_parameter_validation();
            
            System.out.println("\n=== All Tests Passed! ===");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
