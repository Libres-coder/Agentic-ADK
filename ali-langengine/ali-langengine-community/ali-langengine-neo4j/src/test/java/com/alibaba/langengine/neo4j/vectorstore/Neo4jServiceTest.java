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
public class Neo4jServiceTest {

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

    private static Neo4jService neo4jService;
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
            String database = "neo4j";

            fakeEmbeddings = new FakeEmbeddings();
            neo4jService = new Neo4jService(uri, username, password, database, new Neo4jParam());

            System.out.println("Neo4j Service test container started at: " + uri);
        } catch (Exception e) {
            System.out.println("Failed to start Neo4j container: " + e.getMessage());
            Assumptions.assumeTrue(false, "Failed to start Neo4j container");
        }
    }

    @AfterAll
    static void tearDown() {
        if (neo4jService != null) {
            neo4jService.close();
        }
        // Container cleanup is handled automatically by try-with-resources or JVM shutdown
    }

    @Test
    @Order(1)
    public void test_service_connectivity() {
        System.out.println("=== Testing Service Connectivity ===");
        
        assertDoesNotThrow(() -> {
            neo4jService.verifyConnectivity();
        }, "Service connectivity verification should not throw exception");
        
        assertTrue(neo4jService.isHealthy(), "Service should be healthy");
        
        System.out.println("Service connectivity test: SUCCESS");
    }

    @Test
    @Order(2)
    public void test_service_initialization() {
        System.out.println("=== Testing Service Initialization ===");
        
        assertDoesNotThrow(() -> {
            neo4jService.init(fakeEmbeddings);
        }, "Service initialization should not throw exception");
        
        assertTrue(neo4jService.vectorIndexExists(), "Vector index should exist after initialization");
        
        System.out.println("Service initialization test: SUCCESS");
    }

    @Test
    @Order(3)
    public void test_document_operations() {
        System.out.println("=== Testing Document Operations ===");
        
        // 清空现有文档
        neo4jService.clearDocuments();
        assertEquals(0, neo4jService.getDocumentCount(), "Document count should be 0 after clearing");
        
        // 创建测试文档
        List<Document> documents = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Document doc = new Document();
            doc.setPageContent("Service test document " + i);
            doc.setUniqueId("service-test-" + i);
            doc.setEmbedding(Arrays.asList(0.1 * i, 0.2 * i, 0.3 * i));
            doc.setMetadata(Map.of("index", i, "type", "service_test"));
            documents.add(doc);
        }
        
        // 添加文档
        assertDoesNotThrow(() -> {
            neo4jService.addDocuments(documents);
        }, "Adding documents should not throw exception");
        
        assertEquals(10, neo4jService.getDocumentCount(), "Document count should be 10 after adding");
        
        System.out.println("Document operations test: SUCCESS");
    }

    @Test
    @Order(4)
    public void test_similarity_search_service() {
        System.out.println("=== Testing Similarity Search Service ===");
        
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int k = 5;
        
        List<Document> results = assertDoesNotThrow(() -> {
            return neo4jService.similaritySearch(queryVector, k);
        }, "Similarity search should not throw exception");
        
        assertNotNull(results, "Search results should not be null");
        assertTrue(results.size() <= k, "Results size should not exceed k");
        
        // 验证结果质量
        for (Document doc : results) {
            assertNotNull(doc.getUniqueId(), "Document ID should not be null");
            assertNotNull(doc.getPageContent(), "Document content should not be null");
            assertNotNull(doc.getScore(), "Document score should not be null");
        }
        
        System.out.println("Similarity search service test: SUCCESS, Found " + results.size() + " documents");
    }

    @Test
    @Order(5)
    public void test_vector_index_management() {
        System.out.println("=== Testing Vector Index Management ===");
        
        // 检查索引存在
        assertTrue(neo4jService.vectorIndexExists(), "Vector index should exist");
        
        // 删除索引
        assertDoesNotThrow(() -> {
            neo4jService.dropVectorIndex();
        }, "Dropping vector index should not throw exception");
        
        assertFalse(neo4jService.vectorIndexExists(), "Vector index should not exist after dropping");
        
        // 重新创建索引
        assertDoesNotThrow(() -> {
            neo4jService.init(fakeEmbeddings);
        }, "Recreating vector index should not throw exception");
        
        assertTrue(neo4jService.vectorIndexExists(), "Vector index should exist after recreation");
        
        System.out.println("Vector index management test: SUCCESS");
    }

    @Test
    @Order(6)
    public void test_batch_operations() {
        System.out.println("=== Testing Batch Operations ===");
        
        neo4jService.clearDocuments();
        
        // 创建大批量文档
        List<Document> largeBatch = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Document doc = new Document();
            doc.setPageContent("Batch test document " + i + " with some content for testing batch operations.");
            doc.setUniqueId("batch-" + i);
            doc.setEmbedding(Arrays.asList((double) i * 0.01, (double) i * 0.02, (double) i * 0.03));
            doc.setMetadata(Map.of("batch_id", "large_batch", "index", i));
            largeBatch.add(doc);
        }
        
        long startTime = System.currentTimeMillis();
        
        assertDoesNotThrow(() -> {
            neo4jService.addDocuments(largeBatch);
        }, "Large batch operation should not throw exception");
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals(100, neo4jService.getDocumentCount(), "Should have 100 documents after batch operation");
        
        System.out.println("Batch operations test: SUCCESS, Added 100 documents in " + duration + "ms");
    }

    @Test
    @Order(7)
    public void test_parameter_validation() {
        System.out.println("=== Testing Parameter Validation ===");
        
        // 测试自定义参数
        Neo4jParam customParam = new Neo4jParam();
        customParam.setNodeLabel("TestNode");
        customParam.setVectorIndexName("test_index");
        customParam.setFieldNameUniqueId("test_id");
        customParam.setFieldNamePageContent("test_content");
        customParam.setFieldNameEmbedding("test_embedding");
        customParam.setFieldNameMetadata("test_metadata");
        
        // 使用默认的本地连接进行测试
        String uri = "bolt://localhost:7687";

        assertDoesNotThrow(() -> {
            Neo4jService testService = new Neo4jService(uri, "neo4j", "testpassword", "neo4j", customParam);
            testService.init(fakeEmbeddings);
            testService.close();
        }, "Custom parameter service should work correctly");
        
        System.out.println("Parameter validation test: SUCCESS");
    }

    /**
     * 手动测试方法 - 可以单独运行来测试Neo4j服务连接
     */
    public static void main(String[] args) {
        System.out.println("=== Manual Neo4j Service Test ===");
        System.out.println("请确保Neo4j服务正在运行:");
        System.out.println("1. Docker: docker run -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/password neo4j:5.15");
        System.out.println("2. 或使用Neo4j Desktop");
        System.out.println("3. 访问 http://localhost:7474 查看Neo4j Browser");
        
        try {
            Neo4jService service = new Neo4jService("bolt://localhost:7687", "neo4j", "password", "neo4j", new Neo4jParam());
            
            System.out.println("Connection test: " + (service.isHealthy() ? "SUCCESS" : "FAILED"));
            System.out.println("Document count: " + service.getDocumentCount());
            
            service.close();
        } catch (Exception e) {
            System.out.println("Service test: FAILED - " + e.getMessage());
        }
    }
}
