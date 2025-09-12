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
package com.alibaba.langengine.arangodb.integration;

import com.alibaba.langengine.arangodb.vectorstore.ArangoDBVectorStore;
import com.alibaba.langengine.arangodb.vectorstore.ArangoDBService;
import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.vectorstore.ArangoDBParam;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("ArangoDB简化集成测试")
public class ArangoDBIntegrationTest {

    private ArangoDBParam param;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        ArangoDBConfiguration configuration = new ArangoDBConfiguration();
        configuration.setHost("localhost");
        configuration.setPort(8529);
        configuration.setUsername("root");
        configuration.setPassword("test123");
        configuration.setDatabase("testdb");
        
        // 创建参数
        param = new ArangoDBParam();
        param.getInitParam().setDimension(384);
        param.getInitParam().setDefaultTopK(10);
        param.setFieldNamePageContent("page_content");
        param.setFieldNameUniqueId("unique_id");
    }

    @Test
    @DisplayName("测试向量存储配置验证")
    void testVectorStoreConfiguration() {
        // 验证配置
        assertNotNull(param);
        assertNotNull(param.getInitParam());
        
        // 验证配置属性
        assertEquals(384, param.getInitParam().getDimension());
        assertEquals(10, param.getInitParam().getDefaultTopK());
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("unique_id", param.getFieldNameUniqueId());
    }

    @Test
    @DisplayName("测试文档创建")
    void testDocumentCreation() {
        // 准备测试数据
        List<Document> documents = Arrays.asList(
            createTestDocument("doc1", "这是第一个测试文档"),
            createTestDocument("doc2", "这是第二个测试文档"),
            createTestDocument("doc3", "这是第三个测试文档")
        );
        
        // 验证文档创建
        assertNotNull(documents);
        assertEquals(3, documents.size());
        assertEquals("doc1", documents.get(0).getUniqueId());
        assertEquals("这是第一个测试文档", documents.get(0).getPageContent());
        assertEquals("doc2", documents.get(1).getUniqueId());
        assertEquals("这是第二个测试文档", documents.get(1).getPageContent());
    }

    @Test
    @DisplayName("测试向量创建")
    void testVectorCreation() {
        // 创建模拟向量
        String mockEmbedding = createMockEmbedding();
        
        // 验证向量格式
        assertNotNull(mockEmbedding);
        assertTrue(mockEmbedding.startsWith("["));
        assertTrue(mockEmbedding.endsWith("]"));
        assertTrue(mockEmbedding.contains(","));
    }

    /**
     * 创建测试文档
     */
    private Document createTestDocument(String id, String content) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setPageContent(content);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("type", "unit_test");
        metadata.put("timestamp", System.currentTimeMillis());
        doc.setMetadata(metadata);
        
        return doc;
    }

    /**
     * 创建模拟嵌入向量
     */
    private String createMockEmbedding() {
        List<Double> vector = new ArrayList<>();
        Random random = new Random(42);
        
        for (int i = 0; i < 384; i++) {
            vector.add(random.nextGaussian() * 0.1);
        }
        
        // 简单的JSON序列化
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format("%.6f", vector.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
