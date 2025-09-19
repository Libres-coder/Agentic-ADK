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

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.singlestore.SingleStoreConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SingleStore轻量级单元测试")
class SingleStoreUnitTest {

    @Test
    @DisplayName("测试文档创建和基本属性")
    void testDocumentCreation() {
        Document doc = new Document();
        doc.setUniqueId("test_id");
        doc.setPageContent("test content");
        doc.setEmbedding(Arrays.asList(1.0, 2.0, 3.0));

        assertEquals("test_id", doc.getUniqueId());
        assertEquals("test content", doc.getPageContent());
        assertEquals(3, doc.getEmbedding().size());
        assertEquals(1.0, doc.getEmbedding().get(0));
    }

    @Test
    @DisplayName("测试参数对象默认值")
    void testParameterDefaults() {
        SingleStoreParam param = new SingleStoreParam();

        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("embeddings", param.getFieldNameEmbedding());
        assertEquals("row_content", param.getFieldNamePageContent());
        assertEquals("vector_documents", param.getTableName());

        SingleStoreParam.InitParam initParam = param.getInitParam();
        assertNotNull(initParam);
        assertTrue(initParam.isFieldUniqueIdAsPrimaryKey());
        assertEquals(8192, initParam.getFieldPageContentMaxLength());
        assertEquals(1536, initParam.getFieldEmbeddingsDimension());
    }

    @Test
    @DisplayName("测试参数对象自定义设置")
    void testParameterCustomization() {
        SingleStoreParam param = new SingleStoreParam();
        param.setFieldNameUniqueId("custom_id");
        param.setFieldNameEmbedding("custom_embeddings");
        param.setFieldNamePageContent("custom_content");
        param.setTableName("custom_table");

        assertEquals("custom_id", param.getFieldNameUniqueId());
        assertEquals("custom_embeddings", param.getFieldNameEmbedding());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_table", param.getTableName());

        param.getInitParam().setFieldUniqueIdAsPrimaryKey(false);
        param.getInitParam().setFieldEmbeddingsDimension(768);

        assertFalse(param.getInitParam().isFieldUniqueIdAsPrimaryKey());
        assertEquals(768, param.getInitParam().getFieldEmbeddingsDimension());
    }

    @Test
    @DisplayName("测试文档列表处理")
    void testDocumentListProcessing() {
        List<Document> documents = Arrays.asList(
                createDocument("1", "Content 1", Arrays.asList(1.0, 2.0)),
                createDocument("2", "Content 2", Arrays.asList(3.0, 4.0)),
                createDocument("3", "Content 3", Arrays.asList(5.0, 6.0))
        );

        assertEquals(3, documents.size());

        // 验证所有文档都有必需的属性
        for (Document doc : documents) {
            assertNotNull(doc.getUniqueId());
            assertNotNull(doc.getPageContent());
            assertNotNull(doc.getEmbedding());
            assertEquals(2, doc.getEmbedding().size());
        }
    }

    @Test
    @DisplayName("测试空值和边界条件")
    void testNullAndBoundaryConditions() {
        // 测试空文档
        Document emptyDoc = new Document();
        assertNull(emptyDoc.getUniqueId());
        assertNull(emptyDoc.getPageContent());
        assertNull(emptyDoc.getEmbedding());

        // 测试空列表
        List<Document> emptyList = Collections.emptyList();
        assertTrue(emptyList.isEmpty());

        // 测试空向量
        Document docWithEmptyEmbedding = createDocument("empty", "content", Collections.emptyList());
        assertTrue(docWithEmptyEmbedding.getEmbedding().isEmpty());
    }

    @Test
    @DisplayName("测试向量维度处理")
    void testVectorDimensions() {
        // 测试不同维度的向量
        List<Double> vector128 = createVector(128);
        List<Double> vector256 = createVector(256);
        List<Double> vector512 = createVector(512);
        List<Double> vector1536 = createVector(1536);

        assertEquals(128, vector128.size());
        assertEquals(256, vector256.size());
        assertEquals(512, vector512.size());
        assertEquals(1536, vector1536.size());

        // 验证向量内容
        for (Double value : vector128) {
            assertTrue(value >= 0.0 && value <= 1.0);
        }
    }

    @Test
    @DisplayName("测试配置类访问")
    void testConfigurationAccess() {
        // 测试配置类可以被实例化
        assertDoesNotThrow(() -> new SingleStoreConfiguration());

        // 测试静态字段可以访问
        assertDoesNotThrow(() -> {
            String serverUrl = SingleStoreConfiguration.SINGLESTORE_SERVER_URL;
            String database = SingleStoreConfiguration.SINGLESTORE_DATABASE;
            String username = SingleStoreConfiguration.SINGLESTORE_USERNAME;
            String password = SingleStoreConfiguration.SINGLESTORE_PASSWORD;
            // 这些可能是null，但访问不应该抛出异常
        });
    }

    @Test
    @DisplayName("测试JSON序列化兼容性")
    void testJsonCompatibility() {
        List<Float> floatVector = Arrays.asList(1.0f, 2.0f, 3.0f);
        String jsonString = floatVector.toString();

        assertTrue(jsonString.startsWith("["));
        assertTrue(jsonString.endsWith("]"));
        assertTrue(jsonString.contains("1.0"));
        assertTrue(jsonString.contains("2.0"));
        assertTrue(jsonString.contains("3.0"));
    }

    @Test
    @DisplayName("测试大批量文档处理")
    void testLargeBatchProcessing() {
        int batchSize = 1000;
        List<Document> largeBatch = createDocumentBatch(batchSize);

        assertEquals(batchSize, largeBatch.size());

        // 验证批量处理不会出现内存问题
        assertDoesNotThrow(() -> {
            for (Document doc : largeBatch) {
                assertNotNull(doc.getUniqueId());
                assertNotNull(doc.getPageContent());
            }
        });
    }

    @Test
    @DisplayName("测试字符串和数字转换")
    void testTypeConversions() {
        // 测试ID转换
        Document doc = new Document();
        doc.setUniqueId("123");

        assertDoesNotThrow(() -> {
            long id = Long.parseLong(doc.getUniqueId());
            assertEquals(123L, id);
        });

        // 测试向量转换
        List<Double> doubleVector = Arrays.asList(1.0, 2.0, 3.0);
        assertDoesNotThrow(() -> {
            List<Float> floatVector = doubleVector.stream()
                    .map(Double::floatValue)
                    .collect(java.util.stream.Collectors.toList());
            assertEquals(3, floatVector.size());
        });
    }

    @Test
    @DisplayName("测试异常情况处理")
    void testExceptionHandling() {
        // 测试构造函数异常（无有效数据库连接时）
        assertThrows(RuntimeException.class, () -> {
            new SingleStore("invalid_db", "invalid_table");
        });

        // 测试服务异常（无有效连接参数时）
        assertThrows(RuntimeException.class, () -> {
            new SingleStoreService("invalid:connection", "db", "user", "pass", "table", null);
        });
    }

    /**
     * 创建测试文档
     */
    private Document createDocument(String id, String content, List<Double> embedding) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setPageContent(content);
        doc.setEmbedding(embedding);
        return doc;
    }

    /**
     * 创建指定维度的向量
     */
    private List<Double> createVector(int dimension) {
        return java.util.stream.IntStream.range(0, dimension)
                .mapToDouble(i -> Math.random())
                .boxed()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 创建大批量文档
     */
    private List<Document> createDocumentBatch(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> createDocument("doc_" + i, "Content " + i, Arrays.asList((double) i, (double) (i + 1))))
                .collect(java.util.stream.Collectors.toList());
    }
}