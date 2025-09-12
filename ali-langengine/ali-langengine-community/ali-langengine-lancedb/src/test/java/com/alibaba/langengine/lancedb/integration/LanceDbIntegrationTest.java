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
package com.alibaba.langengine.lancedb.integration;

import com.alibaba.langengine.core.docloader.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.lancedb.LanceDbConfiguration;
import com.alibaba.langengine.lancedb.LanceDbParam;
import com.alibaba.langengine.lancedb.LanceDbVectorStore;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("LanceDB集成测试")
@Testcontainers
@Disabled("需要Docker环境，在CI/CD中启用")
class LanceDbIntegrationTest {

    @Container
    static GenericContainer<?> lancedbContainer = new GenericContainer<>("lancedb/lancedb:latest")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)))
            .withEnv("LANCEDB_API_KEY", "test-key")
            .withEnv("LANCEDB_LOG_LEVEL", "INFO");

    private LanceDbVectorStore vectorStore;
    private String baseUrl;
    private String tableName;

    @BeforeAll
    static void setUpContainer() {
        lancedbContainer.start();
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + lancedbContainer.getMappedPort(8080);
        tableName = "test_table_" + System.currentTimeMillis();

        LanceDbConfiguration configuration = LanceDbConfiguration.builder()
                .baseUrl(baseUrl)
                .apiKey("test-key")
                .connectTimeoutMs(30000)
                .readTimeoutMs(60000)
                .build();

        LanceDbParam param = LanceDbParam.builder()
                .tableName(tableName)
                .dimension(3)
                .metric("cosine")
                .build();

        vectorStore = new LanceDbVectorStore(configuration, param);
        vectorStore.setEmbeddings(new TestEmbeddings());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (vectorStore != null) {
            try {
                vectorStore.clear();
            } catch (Exception e) {
                // 忽略清理错误
            }
            vectorStore.close();
        }
    }

    @Test
    @DisplayName("测试基本文档添加和搜索")
    void testBasicAddAndSearch() throws Exception {
        // 准备测试文档
        List<Document> documents = Arrays.asList(
                new Document("The cat sat on the mat", Map.of("category", "animals")),
                new Document("Dogs are loyal animals", Map.of("category", "animals")),
                new Document("Python is a programming language", Map.of("category", "technology")),
                new Document("Java is used for enterprise applications", Map.of("category", "technology"))
        );

        // 添加文档
        List<String> ids = vectorStore.addDocuments(documents);
        assertNotNull(ids);
        assertEquals(4, ids.size());

        // 等待索引更新
        Thread.sleep(1000);

        // 搜索相似文档
        List<Document> results = vectorStore.similaritySearch("cat", 2);
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.get(0).getPageContent().contains("cat") || 
                  results.get(0).getPageContent().contains("animals"));
    }

    @Test
    @DisplayName("测试带分数的相似性搜索")
    void testSimilaritySearchWithScore() throws Exception {
        List<Document> documents = Arrays.asList(
                new Document("Machine learning is powerful"),
                new Document("Deep learning networks"),
                new Document("Artificial intelligence revolution")
        );

        vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        List<VectorStore.DocumentScore> results = vectorStore.similaritySearchWithScore("AI learning", 3);
        assertNotNull(results);
        assertTrue(results.size() > 0);

        // 验证分数排序（降序）
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i-1).getScore() >= results.get(i).getScore());
        }
    }

    @Test
    @DisplayName("测试向量搜索")
    void testVectorSearch() throws Exception {
        List<Document> documents = Arrays.asList(
                new Document("Vector search test document"),
                new Document("Another test document for vectors")
        );

        vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        // 使用预定义向量搜索
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        List<Document> results = vectorStore.similaritySearchByVector(queryVector, 1);

        assertNotNull(results);
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("测试过滤搜索")
    void testFilteredSearch() throws Exception {
        List<Document> documents = Arrays.asList(
                new Document("Technology article 1", Map.of("category", "tech", "priority", "high")),
                new Document("Technology article 2", Map.of("category", "tech", "priority", "low")),
                new Document("Sports article", Map.of("category", "sports", "priority", "high"))
        );

        vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        // 按类别过滤
        Map<String, Object> filter = Map.of("category", "tech");
        List<Document> results = vectorStore.similaritySearchWithFilter("technology", 5, filter);

        assertNotNull(results);
        assertTrue(results.size() >= 2);
        
        // 验证结果都属于tech类别
        for (Document doc : results) {
            assertEquals("tech", doc.getMetadata().get("category"));
        }
    }

    @Test
    @DisplayName("测试批量操作")
    void testBatchOperations() throws Exception {
        // 创建大量文档
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            documents.add(new Document("Document number " + i, 
                    Map.of("index", i, "batch", "test")));
        }

        // 批量添加
        vectorStore.setBatchSize(25);
        List<String> ids = vectorStore.addDocuments(documents);

        assertNotNull(ids);
        assertEquals(100, ids.size());

        Thread.sleep(2000); // 等待批量处理完成

        // 验证文档数量
        long count = vectorStore.getDocumentCount();
        assertEquals(100, count);
    }

    @Test
    @DisplayName("测试删除操作")
    void testDeleteOperations() throws Exception {
        List<Document> documents = Arrays.asList(
                new Document("Document to keep"),
                new Document("Document to delete 1"),
                new Document("Document to delete 2")
        );

        List<String> ids = vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        // 删除部分文档
        List<String> idsToDelete = Arrays.asList(ids.get(1), ids.get(2));
        boolean deleted = vectorStore.deleteDocuments(idsToDelete);

        assertTrue(deleted);
        Thread.sleep(1000);

        // 验证剩余文档数量
        long count = vectorStore.getDocumentCount();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("测试异步操作")
    void testAsyncOperations() throws Exception {
        List<Document> documents = Arrays.asList(
                new Document("Async test document 1"),
                new Document("Async test document 2")
        );

        vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        // 异步搜索
        CompletableFuture<List<Document>> future = vectorStore.similaritySearchAsync("async", 2);
        
        assertNotNull(future);
        List<Document> results = future.get(10, TimeUnit.SECONDS);
        
        assertNotNull(results);
    }

    @Test
    @DisplayName("测试最大边际相关性搜索")
    void testMaxMarginalRelevanceSearch() throws Exception {
        List<Document> documents = Arrays.asList(
                new Document("Machine learning algorithms"),
                new Document("Deep learning neural networks"),
                new Document("Machine learning models"), // 相似内容
                new Document("Artificial intelligence applications"),
                new Document("Computer vision techniques")
        );

        vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        // MMR搜索
        List<Document> results = vectorStore.maxMarginalRelevanceSearch("machine learning", 3, 5, 0.7);

        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.size() <= 3);
    }

    @Test
    @DisplayName("测试元数据更新")
    void testMetadataUpdate() throws Exception {
        List<Document> documents = Arrays.asList(
                new Document("Test document", Map.of("version", 1, "status", "draft"))
        );

        List<String> ids = vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        // 更新元数据
        Map<String, Object> newMetadata = Map.of("version", 2, "status", "published", "updated_at", System.currentTimeMillis());
        boolean updated = vectorStore.updateMetadata(ids.get(0), newMetadata);

        assertTrue(updated);
        Thread.sleep(500);

        // 验证更新
        List<Document> results = vectorStore.similaritySearch("Test document", 1);
        Document updatedDoc = results.get(0);
        assertEquals(2, updatedDoc.getMetadata().get("version"));
        assertEquals("published", updatedDoc.getMetadata().get("status"));
    }

    @Test
    @DisplayName("测试连接和健康检查")
    void testConnectionAndHealth() throws Exception {
        boolean connected = vectorStore.testConnection();
        assertTrue(connected);

        Map<String, Object> stats = vectorStore.getStatistics();
        assertNotNull(stats);
    }

    @Test
    @DisplayName("测试缓存功能")
    void testCaching() throws Exception {
        vectorStore.enableCache(true);

        List<Document> documents = Arrays.asList(
                new Document("Cached search test document")
        );

        vectorStore.addDocuments(documents);
        Thread.sleep(1000);

        String query = "cached search test";

        // 第一次搜索
        long start1 = System.currentTimeMillis();
        List<Document> results1 = vectorStore.similaritySearch(query, 1);
        long end1 = System.currentTimeMillis();

        // 第二次搜索（应该使用缓存）
        long start2 = System.currentTimeMillis();
        List<Document> results2 = vectorStore.similaritySearch(query, 1);
        long end2 = System.currentTimeMillis();

        assertEquals(results1.size(), results2.size());
        assertEquals(results1.get(0).getPageContent(), results2.get(0).getPageContent());

        // 缓存的查询应该更快（虽然这个测试可能不稳定）
        assertTrue((end2 - start2) <= (end1 - start1) + 50); // 允许一些误差
    }

    @Test
    @DisplayName("测试大规模数据处理")
    void testLargeScaleData() throws Exception {
        int documentCount = 1000;
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < documentCount; i++) {
            Map<String, Object> metadata = Map.of(
                    "id", i,
                    "category", "category_" + (i % 10),
                    "priority", i % 3 == 0 ? "high" : "normal"
            );
            documents.add(new Document("Large scale test document number " + i + " with content", metadata));
        }

        // 批量处理
        vectorStore.setBatchSize(100);
        long startTime = System.currentTimeMillis();
        List<String> ids = vectorStore.addDocuments(documents);
        long endTime = System.currentTimeMillis();

        assertEquals(documentCount, ids.size());
        System.out.println("Added " + documentCount + " documents in " + (endTime - startTime) + " ms");

        Thread.sleep(5000); // 等待索引完成

        // 验证数据完整性
        long count = vectorStore.getDocumentCount();
        assertEquals(documentCount, count);

        // 搜索测试
        List<Document> searchResults = vectorStore.similaritySearch("test document", 10);
        assertNotNull(searchResults);
        assertTrue(searchResults.size() > 0);
    }

    @Test
    @DisplayName("测试并发操作")
    void testConcurrentOperations() throws Exception {
        int threadCount = 5;
        int documentsPerThread = 20;
        
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            futures[t] = CompletableFuture.runAsync(() -> {
                try {
                    List<Document> documents = new ArrayList<>();
                    for (int i = 0; i < documentsPerThread; i++) {
                        documents.add(new Document("Thread " + threadId + " document " + i,
                                Map.of("thread_id", threadId, "doc_id", i)));
                    }
                    vectorStore.addDocuments(documents);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // 等待所有线程完成
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
        Thread.sleep(2000);

        // 验证总数据量
        long totalCount = vectorStore.getDocumentCount();
        assertEquals(threadCount * documentsPerThread, totalCount);
    }

    /**
     * 测试用的简单嵌入实现
     */
    private static class TestEmbeddings implements Embeddings {
        @Override
        public List<Double> embedQuery(String text) {
            // 简单的基于文本哈希的向量生成
            int hash = text.hashCode();
            return Arrays.asList(
                    (double) ((hash & 0xFF) / 255.0),
                    (double) (((hash >> 8) & 0xFF) / 255.0),
                    (double) (((hash >> 16) & 0xFF) / 255.0)
            );
        }

        @Override
        public List<List<Double>> embedDocuments(List<String> texts) {
            return texts.stream()
                    .map(this::embedQuery)
                    .toList();
        }
    }
}
