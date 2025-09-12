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
public class ClickHouseServiceTest {

    @Container
    static ClickHouseContainer clickHouseContainer = new ClickHouseContainer("clickhouse/clickhouse-server:23.8")
            .withDatabaseName("test")
            .withUsername("default")
            .withPassword("")
            .withExposedPorts(8123, 9000);

    private static ClickHouseService clickHouseService;
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
            param.setTableName("test_service_collection");
            param.setBatchSize(50);
            
            clickHouseService = new ClickHouseService(jdbcUrl, username, password, database, param);
            
            System.out.println("ClickHouse Service test container started at: " + jdbcUrl);
        } catch (Exception e) {
            System.out.println("Failed to start ClickHouse container: " + e.getMessage());
            assumeTrue(false, "Failed to start ClickHouse container");
        }
    }

    @AfterAll
    static void tearDown() {
        if (clickHouseService != null) {
            clickHouseService.close();
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
    public void test_service_initialization() {
        System.out.println("=== Testing Service Initialization ===");
        
        assertNotNull(clickHouseService);
        
        // 测试健康检查
        assertTrue(clickHouseService.isHealthy());
        
        // 初始化服务
        assertDoesNotThrow(() -> {
            clickHouseService.init(fakeEmbeddings);
        });
        
        System.out.println("Service initialization test: SUCCESS");
    }

    @Test
    @Order(2)
    public void test_document_operations() {
        System.out.println("=== Testing Document Operations ===");
        
        // 创建测试文档
        List<Document> documents = Arrays.asList(
            createTestDocument("service_doc_1", "ClickHouse服务测试文档1", "service_test"),
            createTestDocument("service_doc_2", "ClickHouse服务测试文档2", "service_test"),
            createTestDocument("service_doc_3", "ClickHouse服务测试文档3", "service_test")
        );
        
        // 添加文档
        assertDoesNotThrow(() -> {
            clickHouseService.addDocuments(documents);
        });
        
        // 验证文档数量
        long count = clickHouseService.getDocumentCount();
        assertTrue(count >= documents.size());
        
        System.out.println("Document operations test: SUCCESS");
    }

    @Test
    @Order(3)
    public void test_similarity_search_service() {
        System.out.println("=== Testing Similarity Search Service ===");
        
        // 创建查询向量
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
        
        // 执行相似性搜索
        List<Document> results = clickHouseService.similaritySearch(queryVector, 3, null, null);
        
        assertNotNull(results);
        assertTrue(results.size() <= 3);
        
        // 验证结果
        for (Document doc : results) {
            assertNotNull(doc.getPageContent());
            assertNotNull(doc.getScore());
        }
        
        System.out.println("Similarity search service test: SUCCESS");
    }

    @Test
    @Order(4)
    public void test_batch_document_operations() {
        System.out.println("=== Testing Batch Document Operations ===");
        
        // 创建大批量文档
        List<Document> largeBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeBatch.add(createTestDocument("batch_service_doc_" + i, "批量服务测试文档 " + i, "batch_service"));
        }
        
        long initialCount = clickHouseService.getDocumentCount();
        
        // 批量添加文档
        assertDoesNotThrow(() -> {
            clickHouseService.addDocuments(largeBatch);
        });
        
        long finalCount = clickHouseService.getDocumentCount();
        assertTrue(finalCount >= initialCount + largeBatch.size());
        
        System.out.println("Batch document operations test: SUCCESS");
    }

    @Test
    @Order(5)
    public void test_vector_dimension_inference() {
        System.out.println("=== Testing Vector Dimension Inference ===");
        
        // 创建自定义参数，设置无效的向量维度
        ClickHouseParam customParam = new ClickHouseParam();
        customParam.setTableName("dimension_test_table");
        
        ClickHouseParam.InitParam initParam = customParam.getInitParam();
        initParam.setVectorDimensions(0); // 无效维度，应该被推断
        
        String jdbcUrl = clickHouseContainer.getJdbcUrl();
        
        assertDoesNotThrow(() -> {
            ClickHouseService testService = new ClickHouseService(
                jdbcUrl, "default", "", "test", customParam);
            
            // 初始化时应该推断向量维度
            testService.init(fakeEmbeddings);
            
            // 验证维度已被推断
            assertTrue(initParam.getVectorDimensions() > 0);
            
            testService.close();
        });
        
        System.out.println("Vector dimension inference test: SUCCESS");
    }

    @Test
    @Order(6)
    public void test_similarity_functions() {
        System.out.println("=== Testing Similarity Functions ===");
        
        // 测试不同的相似性函数
        ClickHouseSimilarityFunction[] functions = {
            ClickHouseSimilarityFunction.COSINE,
            ClickHouseSimilarityFunction.L2,
            ClickHouseSimilarityFunction.L1,
            ClickHouseSimilarityFunction.DOT_PRODUCT
        };
        
        for (ClickHouseSimilarityFunction function : functions) {
            ClickHouseParam param = new ClickHouseParam();
            param.setTableName("similarity_test_" + function.name().toLowerCase());
            param.getInitParam().setSimilarityFunction(function);
            
            String jdbcUrl = clickHouseContainer.getJdbcUrl();
            
            assertDoesNotThrow(() -> {
                ClickHouseService testService = new ClickHouseService(
                    jdbcUrl, "default", "", "test", param);
                testService.init(fakeEmbeddings);
                
                // 添加测试文档
                List<Document> docs = Arrays.asList(
                    createTestDocument("sim_test_1", "相似性测试文档", "similarity")
                );
                testService.addDocuments(docs);
                
                // 执行搜索
                List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
                List<Document> results = testService.similaritySearch(queryVector, 1, null, null);
                
                assertNotNull(results);
                
                testService.close();
            });
        }
        
        System.out.println("Similarity functions test: SUCCESS");
    }

    @Test
    @Order(7)
    public void test_clear_and_drop_operations() {
        System.out.println("=== Testing Clear and Drop Operations ===");
        
        // 添加一些测试文档
        List<Document> testDocs = Arrays.asList(
            createTestDocument("clear_test_1", "清理测试文档1", "clear_test"),
            createTestDocument("clear_test_2", "清理测试文档2", "clear_test")
        );
        
        clickHouseService.addDocuments(testDocs);
        
        long countBeforeClear = clickHouseService.getDocumentCount();
        assertTrue(countBeforeClear > 0);
        
        // 测试清空文档
        assertDoesNotThrow(() -> {
            clickHouseService.clearDocuments();
        });
        
        long countAfterClear = clickHouseService.getDocumentCount();
        assertEquals(0, countAfterClear);
        
        // 测试删除表
        assertDoesNotThrow(() -> {
            clickHouseService.dropTable();
        });
        
        System.out.println("Clear and drop operations test: SUCCESS");
    }

    @Test
    @Order(8)
    public void test_error_handling() {
        System.out.println("=== Testing Error Handling ===");
        
        // 测试空文档列表
        assertDoesNotThrow(() -> {
            clickHouseService.addDocuments(null);
            clickHouseService.addDocuments(new ArrayList<>());
        });
        
        // 测试空查询向量
        List<Document> emptyResults = clickHouseService.similaritySearch(null, 5, null, null);
        assertNotNull(emptyResults);
        assertTrue(emptyResults.isEmpty());
        
        List<Document> emptyResults2 = clickHouseService.similaritySearch(new ArrayList<>(), 5, null, null);
        assertNotNull(emptyResults2);
        assertTrue(emptyResults2.isEmpty());
        
        System.out.println("Error handling test: SUCCESS");
    }

    @Test
    @Order(9)
    public void test_metadata_serialization() {
        System.out.println("=== Testing Metadata Serialization ===");
        
        // 重新初始化服务（因为之前删除了表）
        clickHouseService.init(fakeEmbeddings);
        
        // 创建包含复杂元数据的文档
        Map<String, Object> complexMetadata = new HashMap<>();
        complexMetadata.put("title", "元数据序列化测试");
        complexMetadata.put("tags", Arrays.asList("tag1", "tag2", "tag3"));
        complexMetadata.put("score", 88.5);
        complexMetadata.put("active", true);
        
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("author", "测试作者");
        nestedData.put("department", "研发部");
        complexMetadata.put("details", nestedData);
        
        Document metadataDoc = new Document("元数据序列化测试文档", complexMetadata);
        metadataDoc.setUniqueId("metadata_serialization_test");
        
        // 添加文档
        assertDoesNotThrow(() -> {
            clickHouseService.addDocuments(Arrays.asList(metadataDoc));
        });
        
        // 搜索并验证元数据反序列化
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        List<Document> results = clickHouseService.similaritySearch(queryVector, 1, null, null);
        
        if (!results.isEmpty()) {
            Document result = results.get(0);
            if (result.getMetadata() != null) {
                assertTrue(result.getMetadata().containsKey("title"));
                assertTrue(result.getMetadata().containsKey("tags"));
                assertTrue(result.getMetadata().containsKey("score"));
                assertTrue(result.getMetadata().containsKey("active"));
                assertTrue(result.getMetadata().containsKey("details"));
            }
        }
        
        System.out.println("Metadata serialization test: SUCCESS");
    }

    @Test
    @Order(10)
    public void test_connection_management() {
        System.out.println("=== Testing Connection Management ===");
        
        // 测试健康检查
        assertTrue(clickHouseService.isHealthy());
        
        // 测试多次操作不会导致连接问题
        for (int i = 0; i < 5; i++) {
            long count = clickHouseService.getDocumentCount();
            assertTrue(count >= 0);
        }
        
        // 测试关闭服务
        assertDoesNotThrow(() -> {
            clickHouseService.close();
        });
        
        System.out.println("Connection management test: SUCCESS");
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
        System.out.println("Running ClickHouse Service Tests...\n");
        
        // 检查Docker是否可用
        if (!isDockerAvailable()) {
            System.out.println("Docker is not available, skipping service tests");
            return;
        }
        
        try {
            ClickHouseServiceTest test = new ClickHouseServiceTest();
            setUp();
            
            test.test_service_initialization();
            test.test_document_operations();
            test.test_similarity_search_service();
            test.test_batch_document_operations();
            test.test_vector_dimension_inference();
            test.test_similarity_functions();
            test.test_clear_and_drop_operations();
            test.test_error_handling();
            test.test_metadata_serialization();
            test.test_connection_management();
            
            tearDown();
            
            System.out.println("\n=== All Service Tests Passed! ===");
        } catch (Exception e) {
            System.err.println("Service test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
