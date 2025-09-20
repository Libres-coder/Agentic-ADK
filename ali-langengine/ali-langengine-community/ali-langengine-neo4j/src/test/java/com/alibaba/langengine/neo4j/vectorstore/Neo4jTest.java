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
package com.alibaba.langengine.neo4j.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.Neo4jContainer;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Neo4jTest {

    private static boolean dockerAvailable = false;

    static {
        try {
            // 检查Docker是否可用
            ProcessBuilder pb = new ProcessBuilder("docker", "--version");
            Process process = pb.start();
            dockerAvailable = process.waitFor() == 0;
        } catch (Exception e) {
            dockerAvailable = false;
        }
    }

    private static Neo4j neo4jVectorStore;
    private static FakeEmbeddings fakeEmbeddings;

    @BeforeAll
    static void setUp() {
        Assumptions.assumeTrue(dockerAvailable, "Docker is not available, skipping integration tests");

        // 如果Docker可用，尝试启动容器
        try {
            Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5.15")
                    .withAdminPassword("testpassword")
                    .withEnv("NEO4J_PLUGINS", "[\"apoc\"]");
            neo4jContainer.start();

            String uri = neo4jContainer.getBoltUrl();
            String username = "neo4j";
            String password = "testpassword";

            fakeEmbeddings = new FakeEmbeddings();
            neo4jVectorStore = new Neo4j(uri, username, password, "neo4j", new Neo4jParam());
            neo4jVectorStore.setEmbedding(fakeEmbeddings);

            System.out.println("Neo4j container started at: " + uri);
        } catch (Exception e) {
            System.out.println("Failed to start Neo4j container: " + e.getMessage());
            Assumptions.assumeTrue(false, "Failed to start Neo4j container");
        }
    }

    @AfterAll
    static void tearDown() {
        if (neo4jVectorStore != null) {
            neo4jVectorStore.close();
        }
        // Container cleanup is handled automatically by try-with-resources or JVM shutdown
    }

    @Test
    @Order(1)
    public void test_neo4j_service_connection() {
        System.out.println("=== Testing Neo4j Service Connection ===");
        
        assertTrue(neo4jVectorStore.isHealthy(), "Neo4j connection should be healthy");
        System.out.println("Connection test: SUCCESS");
    }

    @Test
    @Order(2)
    public void test_neo4j_initialization() {
        System.out.println("=== Testing Neo4j Initialization ===");
        
        assertDoesNotThrow(() -> {
            neo4jVectorStore.init();
        }, "Neo4j initialization should not throw exception");
        
        System.out.println("Initialization test: SUCCESS");
    }

    @Test
    @Order(3)
    public void test_add_single_document() {
        System.out.println("=== Testing Add Single Document ===");
        
        Document document = new Document();
        document.setPageContent("This is a test document for Neo4j vector store.");
        document.setUniqueId("test-doc-1");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("type", "unit_test");
        document.setMetadata(metadata);

        List<Document> documents = Arrays.asList(document);
        
        assertDoesNotThrow(() -> {
            neo4jVectorStore.addDocuments(documents);
        }, "Adding single document should not throw exception");
        
        // 验证文档数量
        long count = neo4jVectorStore.getDocumentCount();
        assertTrue(count >= 1, "Document count should be at least 1 after adding document");
        
        System.out.println("Add single document test: SUCCESS, Document count: " + count);
    }

    @Test
    @Order(4)
    public void test_add_multiple_documents() {
        System.out.println("=== Testing Add Multiple Documents ===");
        
        List<Document> documents = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Document document = new Document();
            document.setPageContent("Test document number " + i + " for Neo4j vector store testing.");
            document.setUniqueId("test-doc-multi-" + i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "test_batch");
            metadata.put("index", i);
            metadata.put("type", "batch_test");
            document.setMetadata(metadata);
            
            documents.add(document);
        }
        
        long countBefore = neo4jVectorStore.getDocumentCount();
        
        assertDoesNotThrow(() -> {
            neo4jVectorStore.addDocuments(documents);
        }, "Adding multiple documents should not throw exception");
        
        long countAfter = neo4jVectorStore.getDocumentCount();
        assertTrue(countAfter >= countBefore + 5, "Document count should increase by 5");
        
        System.out.println("Add multiple documents test: SUCCESS, Documents added: " + (countAfter - countBefore));
    }

    @Test
    @Order(5)
    public void test_similarity_search() {
        System.out.println("=== Testing Similarity Search ===");
        
        String query = "test document";
        int k = 3;
        
        List<Document> results = assertDoesNotThrow(() -> {
            return neo4jVectorStore.similaritySearch(query, k, null, null);
        }, "Similarity search should not throw exception");
        
        assertNotNull(results, "Search results should not be null");
        assertTrue(results.size() <= k, "Results size should not exceed k");
        
        // 验证结果内容
        for (Document doc : results) {
            assertNotNull(doc.getPageContent(), "Document content should not be null");
            assertNotNull(doc.getUniqueId(), "Document ID should not be null");
        }
        
        System.out.println("Similarity search test: SUCCESS, Found " + results.size() + " documents");
        results.forEach(doc -> System.out.println("  - " + doc.getUniqueId() + ": " + doc.getPageContent().substring(0, Math.min(50, doc.getPageContent().length()))));
    }

    @Test
    @Order(6)
    public void test_similarity_search_with_distance_filter() {
        System.out.println("=== Testing Similarity Search with Distance Filter ===");
        
        String query = "test document";
        int k = 10;
        Double maxDistance = 0.8;
        
        List<Document> results = assertDoesNotThrow(() -> {
            return neo4jVectorStore.similaritySearch(query, k, maxDistance, null);
        }, "Similarity search with distance filter should not throw exception");
        
        assertNotNull(results, "Search results should not be null");
        
        // 验证距离过滤
        for (Document doc : results) {
            if (doc.getScore() != null) {
                assertTrue(doc.getScore() <= maxDistance, 
                    "Document score should be within distance threshold: " + doc.getScore());
            }
        }
        
        System.out.println("Distance filter test: SUCCESS, Found " + results.size() + " documents within distance " + maxDistance);
    }

    @Test
    @Order(7)
    public void test_empty_documents_handling() {
        System.out.println("=== Testing Empty Documents Handling ===");
        
        // 测试空列表
        assertDoesNotThrow(() -> {
            neo4jVectorStore.addDocuments(new ArrayList<>());
        }, "Adding empty document list should not throw exception");
        
        // 测试null
        assertDoesNotThrow(() -> {
            neo4jVectorStore.addDocuments(null);
        }, "Adding null document list should not throw exception");
        
        System.out.println("Empty documents handling test: SUCCESS");
    }

    @Test
    @Order(8)
    public void test_document_with_metadata() {
        System.out.println("=== Testing Document with Complex Metadata ===");
        
        Document document = new Document();
        document.setPageContent("Document with complex metadata for testing purposes.");
        document.setUniqueId("test-metadata-doc");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "Test Document");
        metadata.put("author", "Test Author");
        metadata.put("tags", Arrays.asList("test", "neo4j", "vector"));
        metadata.put("score", 95.5);
        metadata.put("published", true);
        document.setMetadata(metadata);
        
        assertDoesNotThrow(() -> {
            neo4jVectorStore.addDocuments(Arrays.asList(document));
        }, "Adding document with complex metadata should not throw exception");
        
        // 搜索并验证元数据
        List<Document> results = neo4jVectorStore.similaritySearch("complex metadata", 1, null, null);
        assertFalse(results.isEmpty(), "Should find the document with metadata");
        
        Document foundDoc = results.get(0);
        assertNotNull(foundDoc.getMetadata(), "Found document should have metadata");
        
        System.out.println("Complex metadata test: SUCCESS");
    }

    @Test
    @Order(9)
    public void test_large_batch_operations() {
        System.out.println("=== Testing Large Batch Operations ===");
        
        List<Document> largeBatch = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Document document = new Document();
            document.setPageContent("Large batch test document number " + i + ". This document is part of a large batch insertion test.");
            document.setUniqueId("large-batch-" + i);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("batch", "large");
            metadata.put("index", i);
            document.setMetadata(metadata);
            
            largeBatch.add(document);
        }
        
        long countBefore = neo4jVectorStore.getDocumentCount();
        
        assertDoesNotThrow(() -> {
            neo4jVectorStore.addDocuments(largeBatch);
        }, "Large batch insertion should not throw exception");
        
        long countAfter = neo4jVectorStore.getDocumentCount();
        assertTrue(countAfter >= countBefore + 50, "Document count should increase by 50");
        
        System.out.println("Large batch test: SUCCESS, Added " + (countAfter - countBefore) + " documents");
    }

    @Test
    @Order(10)
    public void test_vector_index_operations() {
        System.out.println("=== Testing Vector Index Operations ===");
        
        // 测试索引存在性检查
        boolean indexExists = neo4jVectorStore.getNeo4jService().vectorIndexExists();
        System.out.println("Vector index exists: " + indexExists);
        
        // 如果索引不存在，初始化应该创建它
        if (!indexExists) {
            assertDoesNotThrow(() -> {
                neo4jVectorStore.init();
            }, "Index creation should not throw exception");
            
            assertTrue(neo4jVectorStore.getNeo4jService().vectorIndexExists(), 
                "Vector index should exist after initialization");
        }
        
        System.out.println("Vector index operations test: SUCCESS");
    }

    @Test
    @Order(11)
    public void test_error_handling() {
        System.out.println("=== Testing Error Handling ===");

        // 测试无效查询
        List<Document> results = neo4jVectorStore.similaritySearch("", 5, null, null);
        assertNotNull(results, "Results should not be null even for empty query");

        // 测试负数k值
        results = neo4jVectorStore.similaritySearch("test", -1, null, null);
        assertNotNull(results, "Results should not be null even for negative k");

        System.out.println("Error handling test: SUCCESS");
    }

    @Test
    @Order(12)
    public void test_document_update() {
        System.out.println("=== Testing Document Update ===");

        String docId = "update-test-doc";

        // 添加初始文档
        Document originalDoc = new Document();
        originalDoc.setPageContent("Original content for update test.");
        originalDoc.setUniqueId(docId);
        originalDoc.setMetadata(Map.of("version", 1));

        neo4jVectorStore.addDocuments(Arrays.asList(originalDoc));

        // 更新文档（相同ID）
        Document updatedDoc = new Document();
        updatedDoc.setPageContent("Updated content for update test.");
        updatedDoc.setUniqueId(docId);
        updatedDoc.setMetadata(Map.of("version", 2));

        assertDoesNotThrow(() -> {
            neo4jVectorStore.addDocuments(Arrays.asList(updatedDoc));
        }, "Document update should not throw exception");

        System.out.println("Document update test: SUCCESS");
    }

    @Test
    @Order(13)
    public void test_clear_documents() {
        System.out.println("=== Testing Clear Documents ===");

        long countBefore = neo4jVectorStore.getDocumentCount();
        assertTrue(countBefore > 0, "Should have documents before clearing");

        assertDoesNotThrow(() -> {
            neo4jVectorStore.clearDocuments();
        }, "Clear documents should not throw exception");

        long countAfter = neo4jVectorStore.getDocumentCount();
        assertEquals(0, countAfter, "Document count should be 0 after clearing");

        System.out.println("Clear documents test: SUCCESS, Cleared " + countBefore + " documents");
    }

    @Test
    @Order(14)
    public void test_custom_parameters() {
        System.out.println("=== Testing Custom Parameters ===");

        // 创建自定义参数的Neo4j实例
        Neo4jParam customParam = new Neo4jParam();
        customParam.setNodeLabel("CustomDocument");
        customParam.setVectorIndexName("custom_vector_index");
        customParam.getInitParam().setVectorDimensions(768);
        customParam.getInitParam().setSimilarityFunction(Neo4jSimilarityFunction.EUCLIDEAN);

        // 使用默认的本地连接进行测试
        String uri = "bolt://localhost:7687";
        Neo4j customNeo4j = new Neo4j(uri, "neo4j", "testpassword", "neo4j", customParam);
        customNeo4j.setEmbedding(fakeEmbeddings);

        assertDoesNotThrow(() -> {
            customNeo4j.init();
        }, "Custom parameter initialization should not throw exception");

        assertTrue(customNeo4j.isHealthy(), "Custom Neo4j instance should be healthy");

        customNeo4j.close();
        System.out.println("Custom parameters test: SUCCESS");
    }

    @Test
    @Order(15)
    public void test_concurrent_operations() {
        System.out.println("=== Testing Concurrent Operations ===");

        // 重新初始化以确保有干净的状态
        neo4jVectorStore.init();

        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // 并发添加文档
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    List<Document> docs = new ArrayList<>();
                    for (int j = 1; j <= 5; j++) {
                        Document doc = new Document();
                        doc.setPageContent("Concurrent test document " + j + " from thread " + threadId);
                        doc.setUniqueId("concurrent-" + threadId + "-" + j);
                        doc.setMetadata(Map.of("thread", threadId, "index", j));
                        docs.add(doc);
                    }
                    neo4jVectorStore.addDocuments(docs);
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertTrue(exceptions.isEmpty(), "No exceptions should occur during concurrent operations");

        long finalCount = neo4jVectorStore.getDocumentCount();
        assertTrue(finalCount >= 15, "Should have at least 15 documents after concurrent operations");

        System.out.println("Concurrent operations test: SUCCESS, Final document count: " + finalCount);
    }

    /**
     * 手动测试方法 - 可以单独运行来测试Neo4j连接
     */
    public static void main(String[] args) {
        System.out.println("=== Manual Neo4j Connection Test ===");
        System.out.println("请确保Neo4j服务正在运行:");
        System.out.println("1. Docker: docker run -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/password neo4j:5.15");
        System.out.println("2. 或使用Neo4j Desktop");
        System.out.println("3. 访问 http://localhost:7474 查看Neo4j Browser");

        try {
            Neo4j neo4j = new Neo4j("bolt://localhost:7687", "neo4j", "password");
            neo4j.setEmbedding(new FakeEmbeddings());

            System.out.println("Connection test: " + (neo4j.isHealthy() ? "SUCCESS" : "FAILED"));

            neo4j.close();
        } catch (Exception e) {
            System.out.println("Connection test: FAILED - " + e.getMessage());
        }
    }
}
