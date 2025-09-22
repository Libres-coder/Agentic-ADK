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
package com.alibaba.langengine.singlestore.vectorstore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SingleStore参数配置测试")
class SingleStoreParamTest {

    private SingleStoreParam param;

    @BeforeEach
    void setUp() {
        param = new SingleStoreParam();
    }

    @Test
    @DisplayName("测试默认值")
    void testDefaultValues() {
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("embeddings", param.getFieldNameEmbedding());
        assertEquals("row_content", param.getFieldNamePageContent());
        assertEquals("vector_documents", param.getTableName());
        assertNotNull(param.getInitParam());
    }

    @Test
    @DisplayName("测试字段名设置")
    void testFieldNameSetters() {
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNameEmbedding("custom_embeddings");
        param.setFieldNamePageContent("custom_content");
        param.setTableName("custom_table");

        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_embeddings", param.getFieldNameEmbedding());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_table", param.getTableName());
    }

    @Test
    @DisplayName("测试InitParam默认值")
    void testInitParamDefaultValues() {
        SingleStoreParam.InitParam initParam = param.getInitParam();

        assertTrue(initParam.isFieldUniqueIdAsPrimaryKey());
        assertEquals(8192, initParam.getFieldPageContentMaxLength());
        assertEquals(1536, initParam.getFieldEmbeddingsDimension());
        assertEquals("IVF_PQFS", initParam.getVectorIndexType());
        assertEquals("EUCLIDEAN_DISTANCE", initParam.getVectorMetricType());
    }

    @Test
    @DisplayName("测试InitParam设置")
    void testInitParamSetters() {
        SingleStoreParam.InitParam initParam = param.getInitParam();

        initParam.setFieldUniqueIdAsPrimaryKey(false);
        initParam.setFieldPageContentMaxLength(4096);
        initParam.setFieldEmbeddingsDimension(768);
        initParam.setVectorIndexType("FLAT");
        initParam.setVectorMetricType("COSINE_DISTANCE");

        assertFalse(initParam.isFieldUniqueIdAsPrimaryKey());
        assertEquals(4096, initParam.getFieldPageContentMaxLength());
        assertEquals(768, initParam.getFieldEmbeddingsDimension());
        assertEquals("FLAT", initParam.getVectorIndexType());
        assertEquals("COSINE_DISTANCE", initParam.getVectorMetricType());
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        SingleStoreParam.InitParam initParam = param.getInitParam();

        // 测试最小值
        initParam.setFieldPageContentMaxLength(1);
        initParam.setFieldEmbeddingsDimension(1);
        assertEquals(1, initParam.getFieldPageContentMaxLength());
        assertEquals(1, initParam.getFieldEmbeddingsDimension());

        // 测试较大值
        initParam.setFieldPageContentMaxLength(65535);
        initParam.setFieldEmbeddingsDimension(4096);
        assertEquals(65535, initParam.getFieldPageContentMaxLength());
        assertEquals(4096, initParam.getFieldEmbeddingsDimension());

        // 测试零值
        initParam.setFieldEmbeddingsDimension(0);
        assertEquals(0, initParam.getFieldEmbeddingsDimension());
    }

    @Test
    @DisplayName("测试null值处理")
    void testNullValues() {
        // 测试设置null值
        param.setFieldNameUniqueId(null);
        param.setFieldNameEmbedding(null);
        param.setFieldNamePageContent(null);
        param.setTableName(null);

        assertNull(param.getFieldNameUniqueId());
        assertNull(param.getFieldNameEmbedding());
        assertNull(param.getFieldNamePageContent());
        assertNull(param.getTableName());

        // InitParam字符串字段
        SingleStoreParam.InitParam initParam = param.getInitParam();
        initParam.setVectorIndexType(null);
        initParam.setVectorMetricType(null);

        assertNull(initParam.getVectorIndexType());
        assertNull(initParam.getVectorMetricType());
    }

    @Test
    @DisplayName("测试空字符串处理")
    void testEmptyStringValues() {
        param.setFieldNameUniqueId("");
        param.setFieldNameEmbedding("");
        param.setFieldNamePageContent("");
        param.setTableName("");

        assertEquals("", param.getFieldNameUniqueId());
        assertEquals("", param.getFieldNameEmbedding());
        assertEquals("", param.getFieldNamePageContent());
        assertEquals("", param.getTableName());

        // InitParam字符串字段
        SingleStoreParam.InitParam initParam = param.getInitParam();
        initParam.setVectorIndexType("");
        initParam.setVectorMetricType("");

        assertEquals("", initParam.getVectorIndexType());
        assertEquals("", initParam.getVectorMetricType());
    }

    @Test
    @DisplayName("测试InitParam独立性")
    void testInitParamIndependence() {
        SingleStoreParam param1 = new SingleStoreParam();
        SingleStoreParam param2 = new SingleStoreParam();

        // 修改param1的InitParam
        param1.getInitParam().setFieldEmbeddingsDimension(768);
        param1.getInitParam().setFieldUniqueIdAsPrimaryKey(false);

        // 验证param2的InitParam不受影响
        assertEquals(1536, param2.getInitParam().getFieldEmbeddingsDimension());
        assertTrue(param2.getInitParam().isFieldUniqueIdAsPrimaryKey());
    }

    @Test
    @DisplayName("测试自定义InitParam")
    void testCustomInitParam() {
        SingleStoreParam.InitParam customInitParam = new SingleStoreParam.InitParam();
        customInitParam.setFieldUniqueIdAsPrimaryKey(false);
        customInitParam.setFieldEmbeddingsDimension(512);

        param.setInitParam(customInitParam);

        assertEquals(customInitParam, param.getInitParam());
        assertFalse(param.getInitParam().isFieldUniqueIdAsPrimaryKey());
        assertEquals(512, param.getInitParam().getFieldEmbeddingsDimension());
    }

    @Test
    @DisplayName("测试Lombok生成的方法")
    void testLombokGeneratedMethods() {
        SingleStoreParam param1 = new SingleStoreParam();
        SingleStoreParam param2 = new SingleStoreParam();

        // 测试equals
        assertEquals(param1, param2);

        // 修改一个参数后测试不等
        param1.setFieldNameUniqueId("different_id");
        assertNotEquals(param1, param2);

        // 测试hashCode
        SingleStoreParam param3 = new SingleStoreParam();
        assertEquals(param2.hashCode(), param3.hashCode());

        // 测试toString
        String toString = param1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SingleStoreParam"));
    }
}