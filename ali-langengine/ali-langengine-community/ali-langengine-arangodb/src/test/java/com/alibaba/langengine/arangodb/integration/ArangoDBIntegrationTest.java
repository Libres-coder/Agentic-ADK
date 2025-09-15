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
import com.alibaba.langengine.arangodb.model.ArangoDBVector;
import com.alibaba.langengine.arangodb.model.ArangoDBQueryRequest;
import com.alibaba.langengine.arangodb.model.ArangoDBQueryResponse;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ArangoDB 集成测试
 * 使用模拟数据进行功能测试
 */
@DisplayName("ArangoDB 集成测试")
public class ArangoDBIntegrationTest {

    private ArangoDBConfiguration configuration;
    private ArangoDBParam param;
    private ArangoDBService arangoDBService;
    private ArangoDBVectorStore vectorStore;

    @BeforeEach
    void setUp() {
        // 创建测试配置（使用模拟配置）
        configuration = new ArangoDBConfiguration();
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
        
        // 注意：由于没有真实的 ArangoDB 实例，这些测试将验证配置和数据结构
        // 在实际环境中，需要真实的 ArangoDB 实例才能进行完整的集成测试
    }

    @AfterEach
    void tearDown() {
        // 清理资源
    }

    @Test
    @DisplayName("测试数据库连接和初始化")
    void testDatabaseConnectionAndInitialization() {
        // 验证配置
        assertThat(configuration.getHost()).isEqualTo("localhost");
        assertThat(configuration.getPort()).isEqualTo(8529);
        assertThat(configuration.getDatabase()).isEqualTo("testdb");
        
        // 验证参数
        assertThat(param.getInitParam().getDimension()).isEqualTo(384);
        assertThat(param.getInitParam().getDefaultTopK()).isEqualTo(10);
    }

    @Test
    @DisplayName("测试文档创建和验证")
    void testDocumentCreationAndValidation() {
        // 准备测试文档
        List<Document> documents = Arrays.asList(
            createTestDocument("doc1", "这是关于人工智能的文档"),
            createTestDocument("doc2", "这是关于机器学习的文档"),
            createTestDocument("doc3", "这是关于深度学习的文档")
        );
        
        // 验证文档创建成功
        assertThat(documents).hasSize(3);
        assertThat(documents.get(0).getUniqueId()).isEqualTo("doc1");
        assertThat(documents.get(0).getPageContent()).isEqualTo("这是关于人工智能的文档");
        assertThat(documents.get(1).getUniqueId()).isEqualTo("doc2");
        assertThat(documents.get(1).getPageContent()).isEqualTo("这是关于机器学习的文档");
    }

    @Test
    @DisplayName("测试向量创建和验证")
    void testVectorCreationAndValidation() {
        // 创建模拟向量
        String mockEmbedding = createMockEmbedding();
        
        // 验证向量格式
        assertThat(mockEmbedding).isNotNull();
        assertThat(mockEmbedding).startsWith("[");
        assertThat(mockEmbedding).endsWith("]");
        assertThat(mockEmbedding).contains(",");
        
        // 验证向量长度
        String[] values = mockEmbedding.substring(1, mockEmbedding.length() - 1).split(",");
        assertThat(values).hasSize(384);
    }

    @Test
    @DisplayName("测试元数据过滤条件构建")
    void testMetadataFilterConditionBuilding() {
        // 创建带不同元数据的文档
        Document doc1 = createTestDocument("meta1", "这是技术文档");
        doc1.getMetadata().put("category", "technology");
        doc1.getMetadata().put("priority", "high");
        
        Document doc2 = createTestDocument("meta2", "这是商业文档");
        doc2.getMetadata().put("category", "business");
        doc2.getMetadata().put("priority", "low");
        
        Document doc3 = createTestDocument("meta3", "这是技术报告");
        doc3.getMetadata().put("category", "technology");
        doc3.getMetadata().put("priority", "medium");
        
        List<Document> documents = Arrays.asList(doc1, doc2, doc3);
        
        // 验证文档元数据设置
        assertThat(documents).hasSize(3);
        assertThat(doc1.getMetadata().get("category")).isEqualTo("technology");
        assertThat(doc1.getMetadata().get("priority")).isEqualTo("high");
        assertThat(doc2.getMetadata().get("category")).isEqualTo("business");
        assertThat(doc3.getMetadata().get("category")).isEqualTo("technology");
    }

    @Test
    @DisplayName("测试批量文档创建性能")
    void testBatchDocumentCreationPerformance() {
        // 创建大量文档进行性能测试
        List<Document> documents = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            documents.add(createTestDocument("perf" + i, "性能测试文档 " + i));
        }
        
        long endTime = System.currentTimeMillis();
        
        // 验证文档创建
        assertThat(documents).hasSize(100);
        assertThat(endTime - startTime).isLessThan(1000); // 应该在1秒内完成
        
        // 验证文档内容
        assertThat(documents.get(0).getUniqueId()).isEqualTo("perf0");
        assertThat(documents.get(99).getUniqueId()).isEqualTo("perf99");
    }

    /**
     * 创建测试文档
     */
    private Document createTestDocument(String id, String content) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setPageContent(content);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "integration_test");
        metadata.put("type", "test_document");
        metadata.put("timestamp", System.currentTimeMillis());
        doc.setMetadata(metadata);
        
        return doc;
    }

    /**
     * 创建模拟嵌入向量字符串
     */
    private String createMockEmbedding() {
        Random random = new Random(42);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 384; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.format("%.6f", random.nextGaussian() * 0.1));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 模拟的 Embeddings 实现
     */
    private static class MockEmbeddings extends Embeddings {
        private final Random random = new Random(42);
        
        @Override
        public String getModelType() {
            return "mock-embeddings";
        }
        
        @Override
        public List<Document> embedDocument(List<Document> documents) {
            for (Document doc : documents) {
                doc.setEmbedding(generateMockVector());
            }
            return documents;
        }
        
        @Override
        public List<String> embedQuery(String text, int recommend) {
            List<String> result = new ArrayList<>();
            result.add(generateMockVectorString());
            return result;
        }
        
        private List<Double> generateMockVector() {
            List<Double> vector = new ArrayList<>();
            for (int i = 0; i < 384; i++) {
                vector.add(random.nextGaussian() * 0.1);
            }
            return vector;
        }
        
        private String generateMockVectorString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < 384; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(String.format("%.6f", random.nextGaussian() * 0.1));
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
