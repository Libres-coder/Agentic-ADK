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
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("JanusGraph集成测试")
@Disabled("需要Docker环境和实际的JanusGraph容器")
class JanusGraphIntegrationTest {

    // 注意：这个测试类被禁用，因为它需要真实的容器环境
    // 在实际的CI/CD环境中，可以启用这些测试

    private JanusGraph janusGraph;
    private MockEmbeddings mockEmbedding;
    
    // 如果启用TestContainers，可以使用如下配置：
    // @Container
    // static final GenericContainer<?> janusgraph = new GenericContainer<>("janusgraph/janusgraph:latest")
    //         .withExposedPorts(8182)
    //         .withStartupTimeout(Duration.ofMinutes(5));

    @BeforeEach
    void setUp() {
        mockEmbedding = new MockEmbeddings();
        
        // 配置使用内存后端的JanusGraph
        JanusGraphParam param = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("inmemory")
                .build())
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vertexLabel("IntegrationTestDoc")
                .vectorDimension(5)
                .build())
            .indexConfig(JanusGraphParam.IndexConfig.builder()
                .indexBackend("lucene")
                .enableMixedIndex(false)
                .build())
            .build();
        
        janusGraph = new JanusGraph(mockEmbedding, param);
    }

    @AfterEach
    void tearDown() {
        if (janusGraph != null) {
            janusGraph.close();
        }
    }

    @Test
    @DisplayName("完整工作流集成测试")
    void testCompleteWorkflow() {
        // 1. 添加文档
        List<Document> documents = createIntegrationTestDocuments();
        janusGraph.addDocuments(documents);
        
        // 2. 搜索文档
        List<Document> searchResults = janusGraph.similaritySearch("artificial intelligence", 3);
        assertNotNull(searchResults);
        assertFalse(searchResults.isEmpty());
        
        // 3. 验证搜索结果
        for (Document doc : searchResults) {
            assertNotNull(doc.getPageContent());
            assertNotNull(doc.getMetadata());
            assertTrue(doc.getMetadata().containsKey("score"));
        }
        
        // 4. 删除文档
        if (!searchResults.isEmpty()) {
            String docId = (String) searchResults.get(0).getMetadata().get("id");
            boolean deleted = janusGraph.deleteDocument(docId);
            assertTrue(deleted);
        }
        
        // 5. 验证删除
        List<Document> afterDeletion = janusGraph.similaritySearch("artificial intelligence", 3);
        assertTrue(afterDeletion.size() < searchResults.size() || afterDeletion.isEmpty());
    }

    @Test
    @DisplayName("大数据集性能测试")
    void testLargeDatasetPerformance() {
        int documentCount = 1000;
        List<Document> largeDataset = createLargeDataset(documentCount);
        
        // 测试批量添加性能
        long startTime = System.currentTimeMillis();
        janusGraph.addDocuments(largeDataset);
        long addTime = System.currentTimeMillis() - startTime;
        
        // 验证添加时间在合理范围内（假设不超过30秒）
        assertTrue(addTime < 30000, "Adding " + documentCount + " documents took too long: " + addTime + "ms");
        
        // 测试搜索性能
        startTime = System.currentTimeMillis();
        List<Document> results = janusGraph.similaritySearch("performance test", 10);
        long searchTime = System.currentTimeMillis() - startTime;
        
        // 验证搜索时间在合理范围内（假设不超过5秒）
        assertTrue(searchTime < 5000, "Search took too long: " + searchTime + "ms");
        assertNotNull(results);
    }

    @Test
    @DisplayName("并发操作测试")
    void testConcurrentOperations() throws InterruptedException {
        List<Document> documents = createIntegrationTestDocuments();
        janusGraph.addDocuments(documents);
        
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        
        // 创建并发搜索线程
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    String query = "concurrent test " + threadId + " " + j;
                    List<Document> results = janusGraph.similaritySearch(query, 3);
                    assertNotNull(results);
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join(TimeUnit.SECONDS.toMillis(30));
        }
        
        // 验证没有线程超时
        for (Thread thread : threads) {
            assertFalse(thread.isAlive(), "Thread should have completed");
        }
    }

    @Test
    @DisplayName("不同相似度函数对比测试")
    void testSimilarityFunctionComparison() {
        List<Document> documents = createIntegrationTestDocuments();
        janusGraph.addDocuments(documents);
        
        String query = "machine learning algorithms";
        
        // 测试不同相似度函数
        JanusGraphSimilarityFunction[] functions = {
            JanusGraphSimilarityFunction.COSINE,
            JanusGraphSimilarityFunction.EUCLIDEAN,
            JanusGraphSimilarityFunction.MANHATTAN,
            JanusGraphSimilarityFunction.DOT_PRODUCT
        };
        
        for (JanusGraphSimilarityFunction function : functions) {
            janusGraph.updateSimilarityFunction(function);
            
            List<Document> results = janusGraph.similaritySearch(query, 3);
            assertNotNull(results, "Results should not be null for " + function.getName());
            
            // 验证结果包含相似度分数
            for (Document doc : results) {
                assertTrue(doc.getMetadata().containsKey("score"), 
                    "Score should be present for " + function.getName());
                Object score = doc.getMetadata().get("score");
                assertNotNull(score, "Score should not be null for " + function.getName());
            }
        }
    }

    @Test
    @DisplayName("错误恢复测试")
    void testErrorRecovery() {
        // 测试在发生错误后系统是否能够恢复
        
        // 1. 正常操作
        List<Document> documents = createIntegrationTestDocuments();
        janusGraph.addDocuments(documents);
        
        // 2. 尝试执行可能失败的操作
        assertDoesNotThrow(() -> {
            janusGraph.similaritySearch("", 5); // 空查询
        });
        
        assertDoesNotThrow(() -> {
            janusGraph.deleteDocument("non-existent-id"); // 删除不存在的文档
        });
        
        // 3. 验证系统仍然正常工作
        List<Document> results = janusGraph.similaritySearch("recovery test", 3);
        assertNotNull(results);
        
        // 4. 健康检查
        assertTrue(janusGraph.healthCheck());
    }

    @Test
    @DisplayName("数据一致性测试")
    void testDataConsistency() {
        List<Document> documents = createIntegrationTestDocuments();
        
        // 添加文档
        janusGraph.addDocuments(documents);
        
        // 搜索所有文档
        List<Document> allResults = janusGraph.similaritySearch("test document", 100);
        int initialCount = allResults.size();
        assertTrue(initialCount >= documents.size());
        
        // 删除一个文档
        String docToDelete = (String) documents.get(0).getMetadata().get("id");
        boolean deleted = janusGraph.deleteDocument(docToDelete);
        assertTrue(deleted);
        
        // 验证文档数量减少
        List<Document> afterDeletion = janusGraph.similaritySearch("test document", 100);
        assertEquals(initialCount - 1, afterDeletion.size());
        
        // 验证删除的文档不在结果中
        for (Document doc : afterDeletion) {
            assertNotEquals(docToDelete, doc.getMetadata().get("id"));
        }
    }

    @Test
    @DisplayName("资源使用监控测试")
    void testResourceUsageMonitoring() {
        Runtime runtime = Runtime.getRuntime();
        
        // 记录初始内存使用
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行大量操作
        for (int i = 0; i < 10; i++) {
            List<Document> batch = createLargeDataset(100);
            janusGraph.addDocuments(batch);
            
            // 执行搜索
            janusGraph.similaritySearch("resource test " + i, 10);
        }
        
        // 强制垃圾回收
        System.gc();
        
        // 记录最终内存使用
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // 验证内存增长在合理范围内（小于200MB）
        assertTrue(memoryIncrease < 200 * 1024 * 1024, 
            "Memory usage increased too much: " + (memoryIncrease / 1024 / 1024) + " MB");
    }

    // 辅助方法和类

    private List<Document> createIntegrationTestDocuments() {
        Document doc1 = new Document();
        doc1.setPageContent("Artificial intelligence and machine learning are transforming technology.");
        doc1.setMetadata(Map.of("id", "integration_doc1", "category", "AI", "type", "article"));

        Document doc2 = new Document();
        doc2.setPageContent("Natural language processing enables computers to understand human language.");
        doc2.setMetadata(Map.of("id", "integration_doc2", "category", "NLP", "type", "research"));

        Document doc3 = new Document();
        doc3.setPageContent("Graph databases are excellent for storing and querying connected data.");
        doc3.setMetadata(Map.of("id", "integration_doc3", "category", "Database", "type", "tutorial"));

        Document doc4 = new Document();
        doc4.setPageContent("Vector embeddings represent semantic meaning in mathematical form.");
        doc4.setMetadata(Map.of("id", "integration_doc4", "category", "Embeddings", "type", "explanation"));

        return Arrays.asList(doc1, doc2, doc3, doc4);
    }

    private List<Document> createLargeDataset(int size) {
        return java.util.stream.IntStream.range(0, size)
            .mapToObj(i -> {
                Document doc = new Document();
                doc.setPageContent(String.format(
                    "This is document number %d about various topics including AI, ML, NLP, and databases. " +
                    "It contains relevant information for testing purposes and performance evaluation.", i));
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("id", "large_dataset_doc_" + i);
                metadata.put("index", i);
                metadata.put("category", "large_test");
                metadata.put("batch", i / 100);
                doc.setMetadata(metadata);
                
                return doc;
            })
            .toList();
    }

    /**
     * Mock Embeddings实现用于测试
     */
    private static class MockEmbeddings extends Embeddings {
        
        @Override
        public String getModelType() {
            return "mock-embedding-model";
        }
        
        @Override
        public List<String> embedQuery(String text, int recommend) {
            // 简单的模拟嵌入：基于文本哈希生成向量
            int hash = text.hashCode();
            return Arrays.asList(
                String.valueOf((double) ((hash >> 24) & 0xFF) / 255.0),
                String.valueOf((double) ((hash >> 16) & 0xFF) / 255.0),
                String.valueOf((double) ((hash >> 8) & 0xFF) / 255.0),
                String.valueOf((double) (hash & 0xFF) / 255.0),
                String.valueOf((double) Math.abs(hash % 100) / 100.0)
            );
        }

        @Override
        public List<Document> embedDocument(List<Document> documents) {
            // 为文档添加嵌入向量
            for (Document doc : documents) {
                List<String> vector = embedQuery(doc.getPageContent(), 5);
                Map<String, Object> metadata = new HashMap<>();
                if (doc.getMetadata() != null) {
                    metadata.putAll(doc.getMetadata());
                }
                metadata.put("embedding", vector);
                doc.setMetadata(metadata);
            }
            return documents;
        }
    }
}
