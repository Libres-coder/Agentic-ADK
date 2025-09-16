/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed und    private HugeGraph hugeGraph;
    private FakeEmbeddings embeddings;the Apache License, Version 2.0 (the "License");
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
package com.alibaba.langengine.hugegraph.integration;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.hugegraph.vectorstore.HugeGraph;
import com.alibaba.langengine.hugegraph.vectorstore.HugeGraphParam;
import com.alibaba.langengine.hugegraph.vectorstore.HugeGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HugeGraph集成测试
 * 
 * 这个测试类需要真正的HugeGraph实例来运行
 * 可以通过以下方式启用测试：
 * 1. 设置系统属性：-Dhugegraph.integration.test=true
 * 2. 设置环境变量：HUGEGRAPH_INTEGRATION_TEST=true
 * 
 * 同时需要配置HugeGraph连接信息：
 * - HUGEGRAPH_HOST (默认: localhost)
 * - HUGEGRAPH_PORT (默认: 8080)
 * - HUGEGRAPH_GRAPH (默认: hugegraph)
 * - HUGEGRAPH_USERNAME (默认: admin)
 * - HUGEGRAPH_PASSWORD (默认: admin)
 */
@EnabledIfSystemProperty(named = "hugegraph.integration.test", matches = "true")
@EnabledIfEnvironmentVariable(named = "HUGEGRAPH_INTEGRATION_TEST", matches = "true")
public class HugeGraphIntegrationTest {

    private HugeGraphParam hugeGraphParam;
    private HugeGraphService hugeGraphService;
    private HugeGraph hugeGraph;
    private Embeddings embeddings;

    @BeforeEach
    public void setUp() {
        // 从环境变量或系统属性获取配置
        String host = getProperty("HUGEGRAPH_HOST", "hugegraph.host", "localhost");
        int port = Integer.parseInt(getProperty("HUGEGRAPH_PORT", "hugegraph.port", "8080"));
        String graph = getProperty("HUGEGRAPH_GRAPH", "hugegraph.graph", "hugegraph");
        String username = getProperty("HUGEGRAPH_USERNAME", "hugegraph.username", "admin");
        String password = getProperty("HUGEGRAPH_PASSWORD", "hugegraph.password", "admin");

        hugeGraphParam = HugeGraphParam.builder()
                .serverConfig(HugeGraphParam.ServerConfig.builder()
                        .host(host)
                        .port(port)
                        .graph(graph)
                        .username(username)
                        .password(password)
                        .build())
                .vectorConfig(HugeGraphParam.VectorConfig.builder()
                        .vertexLabel("integration_test_doc")
                        .vectorDimension(768)  // FakeEmbeddings使用768维
                        .build())
                .performanceConfig(HugeGraphParam.PerformanceConfig.builder()
                        .enableVectorIndex(false)  // 集成测试中禁用向量索引
                        .batchQuerySize(100)
                        .build())
                .initParam(HugeGraphParam.InitParam.builder()
                        .createSchemaOnInit(true)
                        .createIndexOnInit(true)
                        .build())
                .build();

        embeddings = new FakeEmbeddings();
        hugeGraph = new HugeGraph(embeddings, hugeGraphParam);
    }

    /**
     * 测试Schema初始化
     */
    @Test
    public void testSchemaInitialization() {
        assertDoesNotThrow(() -> {
            hugeGraphService.initializeSchema(embeddings);
        });
    }

    /**
     * 测试单文档添加
     */
    @Test
    public void testAddSingleDocument() {
        // 初始化Schema
        hugeGraphService.initializeSchema(embeddings);

        // 创建测试文档
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "integration_test");
        metadata.put("category", "test_category");
        
        Document doc = new Document("This is a test document for integration testing.", metadata);
        doc.setUniqueId("integration_test_doc_1");

        // 添加文档
        assertDoesNotThrow(() -> {
            hugeGraph.addDocuments(Arrays.asList(doc));
        });
    }

    /**
     * 测试批量文档添加
     */
    @Test
    public void testAddBatchDocuments() {
        // 初始化Schema
        hugeGraphService.initializeSchema(embeddings);

        // 创建多个测试文档
        List<Document> documents = Arrays.asList(
                createTestDocument("integration_test_doc_batch_1", "First batch document about machine learning.", "ml"),
                createTestDocument("integration_test_doc_batch_2", "Second batch document about artificial intelligence.", "ai"),
                createTestDocument("integration_test_doc_batch_3", "Third batch document about natural language processing.", "nlp")
        );

        // 批量添加文档
        assertDoesNotThrow(() -> {
            hugeGraph.addDocuments(documents);
        });
    }

    /**
     * 测试相似度搜索
     */
    @Test
    public void testSimilaritySearch() {
        // 初始化Schema
        hugeGraphService.initializeSchema(embeddings);

        // 先添加一些测试文档
        List<Document> documents = Arrays.asList(
                createTestDocument("search_test_doc_1", "Machine learning is a subset of artificial intelligence.", "ml"),
                createTestDocument("search_test_doc_2", "Natural language processing deals with human language.", "nlp"),
                createTestDocument("search_test_doc_3", "Deep learning uses neural networks with multiple layers.", "dl")
        );
        
        hugeGraph.addDocuments(documents);

        // 等待一小段时间确保数据写入
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 执行相似度搜索
        List<Document> results = hugeGraph.similaritySearch("artificial intelligence machine learning", 2);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.size() <= 2);

        // 验证搜索结果包含距离信息
        for (Document result : results) {
            assertNotNull(result.getMetadata());
            assertTrue(result.getMetadata().containsKey("distance"));
        }
    }

    /**
     * 测试带距离阈值的相似度搜索
     */
    @Test
    public void testSimilaritySearchWithDistanceThreshold() {
        // 初始化Schema
        hugeGraphService.initializeSchema(embeddings);

        // 添加测试文档
        List<Document> documents = Arrays.asList(
                createTestDocument("threshold_test_doc_1", "Artificial intelligence and machine learning.", "ai"),
                createTestDocument("threshold_test_doc_2", "Cooking recipes and kitchen tools.", "cooking")
        );
        
        hugeGraph.addDocuments(documents);

        // 等待数据写入
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 使用严格的距离阈值搜索
        List<Document> results = hugeGraphService.similaritySearch("machine learning AI", embeddings, 5, 0.5, null);

        assertNotNull(results);
        // 验证所有结果都满足距离阈值
        for (Document result : results) {
            Double distance = (Double) result.getMetadata().get("distance");
            assertNotNull(distance);
            assertTrue(distance <= 0.5, "Distance " + distance + " should be <= 0.5");
        }
    }

    /**
     * 测试参数绑定安全性（确保没有Gremlin注入）
     */
    @Test
    public void testGremlinInjectionPrevention() {
        // 初始化Schema
        hugeGraphService.initializeSchema(embeddings);

        // 创建包含特殊字符的文档（可能导致注入的字符）
        String maliciousContent = "Test document with 'single quotes' and \"double quotes\" and \n newlines and \\backslashes";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", "Test with 'quotes' and \"escape\" chars");
        
        Document doc = new Document(maliciousContent, metadata);
        doc.setUniqueId("injection_test_doc");

        // 应该能安全处理特殊字符而不抛出异常
        assertDoesNotThrow(() -> {
            hugeGraph.addDocuments(Arrays.asList(doc));
        });
    }

    /**
     * 测试资源清理
     */
    @Test
    public void testResourceCleanup() {
        assertDoesNotThrow(() -> {
            if (hugeGraph != null) {
                hugeGraph.close();
            }
            if (hugeGraphService != null) {
                hugeGraphService.close();
            }
        });
    }

    /**
     * 创建测试文档的辅助方法
     */
    private Document createTestDocument(String id, String content, String category) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "integration_test");
        metadata.put("category", category);
        metadata.put("test_timestamp", System.currentTimeMillis());
        
        Document doc = new Document(content, metadata);
        doc.setUniqueId(id);
        return doc;
    }

    /**
     * 获取配置属性的辅助方法（优先环境变量，然后系统属性，最后默认值）
     */
    private String getProperty(String envVar, String sysProp, String defaultValue) {
        String value = System.getenv(envVar);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(sysProp, defaultValue);
        }
        return value;
    }
}
