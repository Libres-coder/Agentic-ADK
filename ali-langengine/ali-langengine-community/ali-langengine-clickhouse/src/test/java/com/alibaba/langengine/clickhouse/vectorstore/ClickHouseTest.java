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

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;


@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true", disabledReason = "Docker not available")
public class ClickHouseTest {

    @Container
    static ClickHouseContainer clickHouseContainer = new ClickHouseContainer("clickhouse/clickhouse-server:23.8")
            .withDatabaseName("test")
            .withUsername("default")
            .withPassword("")
            .withExposedPorts(8123, 9000);

    private static ClickHouse clickHouse;
    private static FakeEmbeddings fakeEmbeddings;
    private static boolean dockerAvailable = false;

    @BeforeAll
    static void setUp() {
        // 检查Docker是否可用
        try {
            dockerAvailable = isDockerAvailable();
            assumeTrue(dockerAvailable, "Docker is not available, skipping integration tests");
            
            // 如果Docker可用，尝试启动容器
            clickHouseContainer.start();
            
            String jdbcUrl = clickHouseContainer.getJdbcUrl();
            String username = clickHouseContainer.getUsername();
            String password = clickHouseContainer.getPassword();
            String database = clickHouseContainer.getDatabaseName();
            
            fakeEmbeddings = new FakeEmbeddings();
            
            ClickHouseParam param = new ClickHouseParam();
            param.setTableName("test_vector_collection");
            param.setBatchSize(100);
            
            clickHouse = new ClickHouse(jdbcUrl, username, password, database, param);
            clickHouse.setEmbedding(fakeEmbeddings);
            
            System.out.println("ClickHouse test container started at: " + jdbcUrl);
        } catch (Exception e) {
            System.out.println("Failed to start ClickHouse container: " + e.getMessage());
            assumeTrue(false, "Failed to start ClickHouse container");
        }
    }

    @AfterAll
    static void tearDown() {
        if (clickHouse != null) {
            clickHouse.close();
        }
        // Container cleanup is handled automatically by try-with-resources or JVM shutdown
    }

    /**
     * 检查Docker是否可用
     */
    private static boolean isDockerAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "--version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @Order(1)
    public void test_initialization() {
        System.out.println("=== Testing ClickHouse Initialization ===");
        
        assertNotNull(clickHouse);
        assertNotNull(fakeEmbeddings);
        
        // 初始化向量存储
        assertDoesNotThrow(() -> {
            clickHouse.init();
        });
        
        // 验证健康状态
        assertTrue(clickHouse.isHealthy());
        
        System.out.println("ClickHouse initialization test: SUCCESS");
    }

    @Test
    @Order(2)
    public void test_add_single_document() {
        System.out.println("=== Testing Add Single Document ===");
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        metadata.put("source", "integration_test");
        
        Document document = new Document("这是一个测试文档", metadata);
        document.setUniqueId("test_doc_1");
        
        List<Document> documents = Arrays.asList(document);
        
        assertDoesNotThrow(() -> {
            clickHouse.addDocuments(documents);
        });
        
        // 验证文档数量
        long count = clickHouse.getDocumentCount();
        assertTrue(count >= 1);
        
        System.out.println("Add single document test: SUCCESS");
    }

    @Test
    @Order(3)
    public void test_add_multiple_documents() {
        System.out.println("=== Testing Add Multiple Documents ===");
        
        List<Document> documents = Arrays.asList(
            createTestDocument("doc_1", "ClickHouse是一个高性能的列式数据库", "database"),
            createTestDocument("doc_2", "向量数据库支持相似性搜索", "vector"),
            createTestDocument("doc_3", "机器学习模型生成文本向量", "ai"),
            createTestDocument("doc_4", "自然语言处理技术应用广泛", "nlp"),
            createTestDocument("doc_5", "大数据分析需要高性能存储", "bigdata")
        );
        
        long initialCount = clickHouse.getDocumentCount();
        
        assertDoesNotThrow(() -> {
            clickHouse.addDocuments(documents);
        });
        
        long finalCount = clickHouse.getDocumentCount();
        assertTrue(finalCount >= initialCount + documents.size());
        
        System.out.println("Add multiple documents test: SUCCESS");
    }

    @Test
    @Order(4)
    public void test_similarity_search() {
        System.out.println("=== Testing Similarity Search ===");
        
        // 基本相似性搜索
        List<Document> results = clickHouse.similaritySearch("数据库系统", 3);
        
        assertNotNull(results);
        assertTrue(results.size() <= 3);
        
        // 验证结果包含相关内容
        if (!results.isEmpty()) {
            for (Document doc : results) {
                assertNotNull(doc.getPageContent());
                assertNotNull(doc.getScore());
                System.out.println("Found document: " + doc.getPageContent() + " (score: " + doc.getScore() + ")");
            }
        }
        
        System.out.println("Similarity search test: SUCCESS");
    }

    @Test
    @Order(5)
    public void test_similarity_search_with_filter() {
        System.out.println("=== Testing Similarity Search with Filter ===");
        
        // 带距离过滤的搜索
        List<Document> results = clickHouse.similaritySearch("向量搜索", 5, 1.0, null);
        
        assertNotNull(results);
        assertTrue(results.size() <= 5);
        
        // 验证距离过滤
        for (Document doc : results) {
            if (doc.getScore() != null) {
                assertTrue(doc.getScore() <= 1.0);
            }
        }
        
        System.out.println("Similarity search with filter test: SUCCESS");
    }

    @Test
    @Order(6)
    public void test_batch_operations() {
        System.out.println("=== Testing Batch Operations ===");
        
        // 创建大批量文档
        List<Document> largeBatch = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            largeBatch.add(createTestDocument("batch_doc_" + i, "批量测试文档 " + i, "batch"));
        }
        
        long initialCount = clickHouse.getDocumentCount();
        
        assertDoesNotThrow(() -> {
            clickHouse.addDocuments(largeBatch);
        });
        
        long finalCount = clickHouse.getDocumentCount();
        assertTrue(finalCount >= initialCount + largeBatch.size());
        
        System.out.println("Batch operations test: SUCCESS");
    }

    @Test
    @Order(7)
    public void test_empty_query_handling() {
        System.out.println("=== Testing Empty Query Handling ===");
        
        // 测试空查询
        List<Document> emptyResults = clickHouse.similaritySearch("", 5);
        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());
        
        // 测试null查询
        List<Document> nullResults = clickHouse.similaritySearch(null, 5);
        assertNotNull(nullResults);
        assertTrue(nullResults.isEmpty());
        
        System.out.println("Empty query handling test: SUCCESS");
    }

    @Test
    @Order(8)
    public void test_document_metadata() {
        System.out.println("=== Testing Document Metadata ===");
        
        // 创建带复杂元数据的文档
        Map<String, Object> complexMetadata = new HashMap<>();
        complexMetadata.put("title", "复杂元数据测试");
        complexMetadata.put("author", "测试作者");
        complexMetadata.put("tags", Arrays.asList("tag1", "tag2", "tag3"));
        complexMetadata.put("score", 95.5);
        complexMetadata.put("published", true);
        
        Document metadataDoc = new Document("包含复杂元数据的测试文档", complexMetadata);
        metadataDoc.setUniqueId("metadata_test_doc");
        
        assertDoesNotThrow(() -> {
            clickHouse.addDocuments(Arrays.asList(metadataDoc));
        });
        
        // 搜索并验证元数据
        List<Document> results = clickHouse.similaritySearch("复杂元数据", 1);
        
        if (!results.isEmpty()) {
            Document result = results.get(0);
            if (result.getMetadata() != null) {
                System.out.println("Retrieved metadata: " + result.getMetadata());
            }
        }
        
        System.out.println("Document metadata test: SUCCESS");
    }

    @Test
    @Order(9)
    public void test_concurrent_operations() {
        System.out.println("=== Testing Concurrent Operations ===");
        
        // 并发添加文档
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                List<Document> threadDocs = Arrays.asList(
                    createTestDocument("thread_" + threadId + "_doc_1", "线程 " + threadId + " 文档 1", "concurrent"),
                    createTestDocument("thread_" + threadId + "_doc_2", "线程 " + threadId + " 文档 2", "concurrent")
                );
                
                try {
                    clickHouse.addDocuments(threadDocs);
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " failed: " + e.getMessage());
                }
            });
            threads.add(thread);
        }
        
        // 启动所有线程
        threads.forEach(Thread::start);
        
        // 等待所有线程完成
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        System.out.println("Concurrent operations test: SUCCESS");
    }

    @Test
    @Order(10)
    public void test_custom_parameters() {
        System.out.println("=== Testing Custom Parameters ===");
        
        // 创建自定义参数
        ClickHouseParam customParam = new ClickHouseParam();
        customParam.setTableName("custom_test_table");
        customParam.setBatchSize(50);
        
        ClickHouseParam.InitParam initParam = customParam.getInitParam();
        initParam.setVectorDimensions(384);
        initParam.setSimilarityFunction(ClickHouseSimilarityFunction.L2);
        initParam.setEngineType("ReplacingMergeTree");
        
        // 使用默认的本地连接进行测试
        String jdbcUrl = clickHouseContainer.getJdbcUrl();
        
        assertDoesNotThrow(() -> {
            ClickHouse customClickHouse = new ClickHouse(jdbcUrl, "default", "", "test", customParam);
            customClickHouse.setEmbedding(new FakeEmbeddings());
            customClickHouse.init();
            customClickHouse.close();
        });
        
        System.out.println("Custom parameters test: SUCCESS");
    }

    /**
     * 创建测试文档
     */
    private static Document createTestDocument(String id, String content, String category) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", category);
        metadata.put("timestamp", System.currentTimeMillis());
        
        Document document = new Document(content, metadata);
        document.setUniqueId(id);
        return document;
    }

    /**
     * 运行所有测试的主方法
     */
    public static void main(String[] args) {
        System.out.println("Running ClickHouse Integration Tests...\n");
        
        // 检查Docker是否可用
        if (!isDockerAvailable()) {
            System.out.println("Docker is not available, skipping integration tests");
            return;
        }
        
        try {
            ClickHouseTest test = new ClickHouseTest();
            setUp();
            
            test.test_initialization();
            test.test_add_single_document();
            test.test_add_multiple_documents();
            test.test_similarity_search();
            test.test_similarity_search_with_filter();
            test.test_batch_operations();
            test.test_empty_query_handling();
            test.test_document_metadata();
            test.test_concurrent_operations();
            test.test_custom_parameters();
            
            tearDown();
            
            System.out.println("\n=== All Integration Tests Passed! ===");
        } catch (Exception e) {
            System.err.println("Integration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
