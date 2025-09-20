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
package com.alibaba.langengine.janusgraph.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@DisplayName("JanusGraph向量存储测试")
class JanusGraphTest {

    @Mock
    private Embeddings mockEmbedding;

    private JanusGraph janusGraph;
    private JanusGraphParam testParam;
    private Path tempDirectory;
    private AutoCloseable mockCloseable;

    @BeforeEach
    void setUp() throws IOException {
                MockitoAnnotations.initMocks(this);
        
        // 创建临时目录
        tempDirectory = Files.createTempDirectory("janusgraph-test");
        
        // 配置测试参数
        testParam = createTestConfig();
        
        // 模拟Embedding返回
        when(mockEmbedding.embedQuery(anyString(), anyInt()))
            .thenReturn(Arrays.asList("0.1", "0.2", "0.3", "0.4", "0.5"));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (janusGraph != null) {
            janusGraph.close();
        }
        if (mockCloseable != null) {
            mockCloseable.close();
        }
        // 清理临时目录
        deleteDirectory(tempDirectory.toFile());
    }

    private JanusGraphParam createTestConfig() {
        return JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("berkeleyje")
                .storageDirectory(tempDirectory.toString())
                .build())
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vertexLabel("TestDocument")
                .vectorDimension(5)
                .similarityThreshold(0.7)
                .build())
            .connectionConfig(JanusGraphParam.ConnectionConfig.builder()
                .maxConnectionPoolSize(5)
                .build())
            .indexConfig(JanusGraphParam.IndexConfig.builder()
                .indexBackend("lucene")
                .enableMixedIndex(false)
                .build())
            .batchConfig(JanusGraphParam.BatchConfig.builder()
                .batchSize(10)
                .build())
            .initParam(JanusGraphParam.InitParam.builder()
                .createSchemaOnInit(true)
                .build())
            .build();
    }

    @Test
    @DisplayName("测试基本构造函数")
    void testBasicConstructor() {
        assertDoesNotThrow(() -> {
            JanusGraph vectorStore = new JanusGraph(mockEmbedding);
            assertNotNull(vectorStore);
            assertNotNull(vectorStore.getEmbedding());
            assertEquals(mockEmbedding, vectorStore.getEmbedding());
        });
    }

    @Test
    @DisplayName("测试带参数的构造函数")
    @Disabled("需要JanusGraph运行环境")
    void testParameterizedConstructor() {
        assertDoesNotThrow(() -> {
            janusGraph = new JanusGraph(mockEmbedding, testParam);
            assertNotNull(janusGraph);
            assertEquals(mockEmbedding, janusGraph.getEmbedding());
            assertEquals(testParam, janusGraph.getJanusGraphParam());
        });
    }

    @Test
    @DisplayName("测试完整构造函数")
    @Disabled("需要JanusGraph运行环境")
    void testFullConstructor() {
        assertDoesNotThrow(() -> {
            janusGraph = new JanusGraph(mockEmbedding, testParam, JanusGraphSimilarityFunction.COSINE);
            assertNotNull(janusGraph);
            assertEquals(JanusGraphSimilarityFunction.COSINE, janusGraph.getSimilarityFunction());
        });
    }

    @Test
    @DisplayName("测试静态工厂方法 - 本地实例")
    @Disabled("需要JanusGraph运行环境")
    void testCreateLocalInstance() {
        String storagePath = tempDirectory.toString();
        
        assertDoesNotThrow(() -> {
            janusGraph = JanusGraph.createLocalInstance(mockEmbedding, storagePath);
            assertNotNull(janusGraph);
            assertEquals("berkeleyje", janusGraph.getJanusGraphParam().getGraphConfig().getStorageBackend());
            assertEquals("lucene", janusGraph.getJanusGraphParam().getIndexConfig().getIndexBackend());
        });
    }

    @Test
    @DisplayName("测试静态工厂方法 - Cassandra实例")
    @Disabled("需要Cassandra环境")
    void testCreateCassandraInstance() {
        assertDoesNotThrow(() -> {
            janusGraph = JanusGraph.createCassandraInstance(
                mockEmbedding, "localhost", 9042, "test_keyspace");
            assertNotNull(janusGraph);
            assertEquals("cassandra", janusGraph.getJanusGraphParam().getGraphConfig().getStorageBackend());
            assertEquals("elasticsearch", janusGraph.getJanusGraphParam().getIndexConfig().getIndexBackend());
        });
    }

    @Test
    @DisplayName("测试添加单个文档")
    @Disabled("需要JanusGraph运行环境")
    void testAddSingleDocument() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        Document document = createTestDocument("Test content", "doc1");
        
        assertDoesNotThrow(() -> {
            janusGraph.addDocuments(Arrays.asList(document));
        });
    }

    @Test
    @DisplayName("测试添加多个文档")
    @Disabled("需要JanusGraph运行环境")
    void testAddMultipleDocuments() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> documents = createTestDocuments();
        
        assertDoesNotThrow(() -> {
            janusGraph.addDocuments(documents);
        });
    }

    @Test
    @DisplayName("测试添加文本列表")
    @Disabled("需要JanusGraph运行环境")
    void testAddTexts() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<String> texts = Arrays.asList(
            "First text about AI",
            "Second text about machine learning",
            "Third text about data science"
        );
        
        assertDoesNotThrow(() -> {
            janusGraph.addTexts(texts);
        });
    }

    @Test
    @DisplayName("测试相似性搜索")
    @Disabled("需要JanusGraph运行环境")
    void testSimilaritySearch() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        // 添加测试文档
        List<Document> documents = createTestDocuments();
        janusGraph.addDocuments(documents);
        
        // 执行搜索
        assertDoesNotThrow(() -> {
            List<Document> results = janusGraph.similaritySearch("machine learning", 2);
            assertNotNull(results);
            assertTrue(results.size() <= 2);
        });
    }

    @Test
    @DisplayName("测试带距离阈值的相似性搜索")
    @Disabled("需要JanusGraph运行环境")
    void testSimilaritySearchWithDistance() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> documents = createTestDocuments();
        janusGraph.addDocuments(documents);
        
        assertDoesNotThrow(() -> {
            List<Document> results = janusGraph.similaritySearch("test query", 5, 0.5);
            assertNotNull(results);
        });
    }

    @Test
    @DisplayName("测试带类型的相似性搜索")
    @Disabled("需要JanusGraph运行环境")
    void testSimilaritySearchWithType() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> documents = createTestDocuments();
        janusGraph.addDocuments(documents);
        
        assertDoesNotThrow(() -> {
            List<Document> results = janusGraph.similaritySearch("test query", 3, 1);
            assertNotNull(results);
        });
    }

    @Test
    @DisplayName("测试删除单个文档")
    @Disabled("需要JanusGraph运行环境")
    void testDeleteSingleDocument() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> documents = createTestDocuments();
        janusGraph.addDocuments(documents);
        
        assertDoesNotThrow(() -> {
            boolean deleted = janusGraph.deleteDocument("doc1");
            assertTrue(deleted);
        });
        
        // 测试删除不存在的文档
        assertDoesNotThrow(() -> {
            boolean deleted = janusGraph.deleteDocument("non-existent");
            assertFalse(deleted);
        });
    }

    @Test
    @DisplayName("测试批量删除文档")
    @Disabled("需要JanusGraph运行环境")
    void testDeleteMultipleDocuments() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> documents = createTestDocuments();
        janusGraph.addDocuments(documents);
        
        List<String> idsToDelete = Arrays.asList("doc1", "doc2", "non-existent");
        
        assertDoesNotThrow(() -> {
            int deletedCount = janusGraph.deleteDocuments(idsToDelete);
            assertEquals(2, deletedCount); // 只有doc1和doc2存在
        });
    }

    @Test
    @DisplayName("测试健康检查")
    @Disabled("需要JanusGraph运行环境")
    void testHealthCheck() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        assertDoesNotThrow(() -> {
            boolean healthy = janusGraph.healthCheck();
            assertTrue(healthy);
        });
    }

    @Test
    @DisplayName("测试获取存储统计")
    @Disabled("需要JanusGraph运行环境")
    void testGetStorageStats() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        assertDoesNotThrow(() -> {
            Map<String, Object> stats = janusGraph.getStorageStats();
            assertNotNull(stats);
            assertNotNull(stats.get("storage_backend"));
            assertNotNull(stats.get("index_backend"));
            assertNotNull(stats.get("vertex_label"));
            assertNotNull(stats.get("vector_dimension"));
        });
    }

    @Test
    @DisplayName("测试更新相似度函数")
    @Disabled("需要JanusGraph运行环境")
    void testUpdateSimilarityFunction() {
        janusGraph = new JanusGraph(mockEmbedding, testParam, JanusGraphSimilarityFunction.COSINE);
        
        assertDoesNotThrow(() -> {
            janusGraph.updateSimilarityFunction(JanusGraphSimilarityFunction.EUCLIDEAN);
            assertEquals(JanusGraphSimilarityFunction.EUCLIDEAN, janusGraph.getSimilarityFunction());
        });
        
        // 测试null值
        assertDoesNotThrow(() -> {
            janusGraph.updateSimilarityFunction(null);
            assertEquals(JanusGraphSimilarityFunction.EUCLIDEAN, janusGraph.getSimilarityFunction());
        });
    }

    @Test
    @DisplayName("测试获取配置JSON")
    @Disabled("需要JanusGraph运行环境")
    void testGetConfigurationJson() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        assertDoesNotThrow(() -> {
            String configJson = janusGraph.getConfigurationJson();
            assertNotNull(configJson);
            assertFalse(configJson.isEmpty());
            assertNotEquals("{}", configJson);
        });
    }

    @Test
    @DisplayName("测试AsRetriever功能")
    @Disabled("需要JanusGraph运行环境")
    void testAsRetriever() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        assertDoesNotThrow(() -> {
            Object retriever = janusGraph.asRetriever();
            assertNotNull(retriever);
        });
    }

    @Test
    @DisplayName("测试错误处理 - 空查询")
    @Disabled("需要JanusGraph运行环境")
    void testErrorHandlingEmptyQuery() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> results = janusGraph.similaritySearch("", 5);
        assertTrue(results.isEmpty());
        
        results = janusGraph.similaritySearch(null, 5);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("测试错误处理 - 无效K值")
    @Disabled("需要JanusGraph运行环境")
    void testErrorHandlingInvalidK() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> results = janusGraph.similaritySearch("test", 0);
        assertTrue(results.isEmpty());
        
        results = janusGraph.similaritySearch("test", -1);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("测试错误处理 - 空文档列表")
    @Disabled("需要JanusGraph运行环境")
    void testErrorHandlingEmptyDocuments() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        assertDoesNotThrow(() -> {
            janusGraph.addDocuments(Arrays.asList());
        });
        
        assertDoesNotThrow(() -> {
            janusGraph.addDocuments(null);
        });
    }

    @Test
    @DisplayName("测试配置验证")
    void testConfigurationValidation() {
        // 测试null embedding
        assertThrows(RuntimeException.class, () -> {
            new JanusGraph(null, testParam);
        });
        
        // 测试无效向量维度
        JanusGraphParam invalidParam = JanusGraphParam.builder()
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vectorDimension(0)
                .build())
            .build();
            
        assertThrows(RuntimeException.class, () -> {
            new JanusGraph(mockEmbedding, invalidParam);
        });
    }

    @Test
    @DisplayName("测试资源管理")
    @Disabled("需要JanusGraph运行环境")
    void testResourceManagement() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        assertDoesNotThrow(() -> {
            janusGraph.close();
        });
        
        // 测试重复关闭
        assertDoesNotThrow(() -> {
            janusGraph.close();
        });
    }

    @Test
    @DisplayName("测试并发访问")
    @Disabled("需要JanusGraph运行环境")
    void testConcurrentAccess() {
        janusGraph = new JanusGraph(mockEmbedding, testParam);
        
        List<Document> documents = createLargeTestDocumentSet(50);
        
        // 并发添加文档
        assertDoesNotThrow(() -> {
            documents.parallelStream().forEach(doc -> {
                janusGraph.addDocuments(Arrays.asList(doc));
            });
        });
        
        // 并发搜索
        assertDoesNotThrow(() -> {
            Arrays.asList("query1", "query2", "query3").parallelStream().forEach(query -> {
                janusGraph.similaritySearch(query, 5);
            });
        });
    }

    @Test
    @DisplayName("测试内存泄漏")
    void testMemoryLeaks() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 创建和销毁多个实例
        for (int i = 0; i < 10; i++) {
            try {
                JanusGraph vectorStore = new JanusGraph(mockEmbedding, testParam);
                vectorStore.close();
            } catch (Exception e) {
                // 忽略初始化错误，专注于内存测试
            }
        }
        
        System.gc();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // 内存增长应该在合理范围内（小于100MB）
        assertTrue(memoryIncrease < 100 * 1024 * 1024, 
            "Memory leak detected: " + (memoryIncrease / 1024 / 1024) + " MB");
    }

    // 辅助方法

    private Document createTestDocument(String content, String id) {
        Document document = new Document();
        document.setPageContent(content);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", id);
        metadata.put("category", "test");
        document.setMetadata(metadata);
        return document;
    }

    private List<Document> createTestDocuments() {
        Document doc1 = createTestDocument("This is about machine learning and AI.", "doc1");
        Document doc2 = createTestDocument("Natural language processing is important.", "doc2");
        Document doc3 = createTestDocument("Graph databases store connected data.", "doc3");
        
        return Arrays.asList(doc1, doc2, doc3);
    }

    private List<Document> createLargeTestDocumentSet(int size) {
        return java.util.stream.IntStream.range(0, size)
            .mapToObj(i -> createTestDocument("Content for document " + i, "doc" + i))
            .toList();
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
