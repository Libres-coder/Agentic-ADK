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


@DisplayName("JanusGraph服务测试")
class JanusGraphServiceTest {

    @Mock
    private Embeddings mockEmbedding;

    private JanusGraphService janusGraphService;
    private JanusGraphParam testParam;
    private Path tempDirectory;
    // 移除不需要的mockCloseable字段

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        
        // 创建临时目录用于BerkeleyDB存储
        tempDirectory = Files.createTempDirectory("janusgraph-test");
        
        // 配置使用内存数据库和Lucene索引
        testParam = createInMemoryTestConfig();
        
        // 模拟Embedding返回
        when(mockEmbedding.embedQuery(anyString(), anyInt()))
            .thenReturn(Arrays.asList("0.1", "0.2", "0.3", "0.4", "0.5"));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (janusGraphService != null) {
            janusGraphService.close();
        }
        // MockCloseable已移除，无需关闭
        // 清理临时目录
        deleteDirectory(tempDirectory.toFile());
    }

    private JanusGraphParam createInMemoryTestConfig() {
        return JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("berkeleyje")  // 使用BerkeleyDB后端
                .storageDirectory(tempDirectory.toString())  // 使用临时目录
                .build())
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vertexLabel("TestDocument")
                .vectorDimension(5)
                .build())
            .connectionConfig(JanusGraphParam.ConnectionConfig.builder()
                .maxConnectionPoolSize(2)
                .connectionTimeoutSeconds(10)
                .build())
            .indexConfig(JanusGraphParam.IndexConfig.builder()
                .indexBackend("lucene")  // 使用Lucene索引
                .enableMixedIndex(false)  // 简化测试，不使用混合索引
                .build())
            .batchConfig(JanusGraphParam.BatchConfig.builder()
                .batchSize(10)
                .enableBatchCommit(false)
                .build())
            .initParam(JanusGraphParam.InitParam.builder()
                .createSchemaOnInit(true)
                .createIndexOnInit(true)
                .build())
            .build();
    }

    @Test
    @DisplayName("测试服务初始化")
    @Disabled("需要JanusGraph运行环境") // 禁用需要实际数据库的测试
    void testServiceInitialization() {
        assertDoesNotThrow(() -> {
            janusGraphService = new JanusGraphService(testParam);
            assertNotNull(janusGraphService);
        });
    }

    @Test
    @DisplayName("测试配置验证")
    void testConfigurationValidation() {
        // 测试null配置
        assertDoesNotThrow(() -> {
            JanusGraphService service = new JanusGraphService(null);
            assertNotNull(service.getJanusGraphParam());
        });

        // 测试有效配置
        assertDoesNotThrow(() -> {
            JanusGraphService service = new JanusGraphService(testParam);
            assertNotNull(service);
            assertEquals(testParam, service.getJanusGraphParam());
        });
    }

    @Test
    @DisplayName("测试文档添加")
    @Disabled("需要JanusGraph运行环境")
    void testAddDocuments() {
        janusGraphService = new JanusGraphService(testParam);
        janusGraphService.initializeSchema(mockEmbedding);

        List<Document> documents = createTestDocuments();
        
        assertDoesNotThrow(() -> {
            List<String> documentIds = janusGraphService.addDocuments(documents, mockEmbedding);
            assertEquals(documents.size(), documentIds.size());
            assertFalse(documentIds.contains(null));
        });
    }

    @Test
    @DisplayName("测试批量文档添加")
    @Disabled("需要JanusGraph运行环境")
    void testBatchAddDocuments() {
        // 配置批量提交
        JanusGraphParam batchParam = JanusGraphParam.builder()
            .graphConfig(testParam.getGraphConfig())
            .vectorConfig(testParam.getVectorConfig())
            .connectionConfig(testParam.getConnectionConfig())
            .indexConfig(testParam.getIndexConfig())
            .batchConfig(JanusGraphParam.BatchConfig.builder()
                .batchSize(2)
                .enableBatchCommit(true)
                .build())
            .initParam(testParam.getInitParam())
            .build();

        janusGraphService = new JanusGraphService(batchParam);
        janusGraphService.initializeSchema(mockEmbedding);

        List<Document> largeDocumentSet = createLargeTestDocumentSet(5);
        
        assertDoesNotThrow(() -> {
            List<String> documentIds = janusGraphService.addDocuments(largeDocumentSet, mockEmbedding);
            assertEquals(largeDocumentSet.size(), documentIds.size());
        });
    }

    @Test
    @DisplayName("测试相似度搜索")
    @Disabled("需要JanusGraph运行环境")
    void testSimilaritySearch() {
        janusGraphService = new JanusGraphService(testParam);
        janusGraphService.initializeSchema(mockEmbedding);

        // 先添加文档
        List<Document> documents = createTestDocuments();
        janusGraphService.addDocuments(documents, mockEmbedding);

        // 执行搜索
        assertDoesNotThrow(() -> {
            List<Document> results = janusGraphService.similaritySearch(
                "test query", mockEmbedding, 3, null, null);
            assertNotNull(results);
            assertTrue(results.size() <= 3);
        });
    }

    @Test
    @DisplayName("测试文档删除")
    @Disabled("需要JanusGraph运行环境")
    void testDeleteDocument() {
        janusGraphService = new JanusGraphService(testParam);
        janusGraphService.initializeSchema(mockEmbedding);

        // 添加文档
        List<Document> documents = createTestDocuments();
        List<String> documentIds = janusGraphService.addDocuments(documents, mockEmbedding);

        // 删除第一个文档
        String firstDocId = documentIds.get(0);
        assertDoesNotThrow(() -> {
            boolean deleted = janusGraphService.deleteDocument(firstDocId);
            assertTrue(deleted);
        });

        // 尝试删除不存在的文档
        assertDoesNotThrow(() -> {
            boolean deleted = janusGraphService.deleteDocument("non-existent-id");
            assertFalse(deleted);
        });
    }

    @Test
    @DisplayName("测试连接验证")
    @Disabled("需要JanusGraph运行环境")
    void testVerifyConnection() {
        janusGraphService = new JanusGraphService(testParam);
        
        assertDoesNotThrow(() -> {
            janusGraphService.verifyConnection();
        });
    }

    @Test
    @DisplayName("测试Schema初始化")
    @Disabled("需要JanusGraph运行环境")
    void testSchemaInitialization() {
        janusGraphService = new JanusGraphService(testParam);
        
        assertDoesNotThrow(() -> {
            janusGraphService.initializeSchema(mockEmbedding);
        });

        // 测试跳过Schema创建
        JanusGraphParam noSchemaParam = JanusGraphParam.builder()
            .graphConfig(testParam.getGraphConfig())
            .vectorConfig(testParam.getVectorConfig())
            .connectionConfig(testParam.getConnectionConfig())
            .indexConfig(testParam.getIndexConfig())
            .batchConfig(testParam.getBatchConfig())
            .initParam(JanusGraphParam.InitParam.builder()
                .createSchemaOnInit(false)
                .createIndexOnInit(false)
                .build())
            .build();

        JanusGraphService noSchemaService = new JanusGraphService(noSchemaParam);
        assertDoesNotThrow(() -> {
            noSchemaService.initializeSchema(mockEmbedding);
        });
        noSchemaService.close();
    }

    @Test
    @DisplayName("测试服务关闭")
    void testServiceClose() {
        // 测试关闭未初始化的服务
        JanusGraphService uninitializedService = new JanusGraphService(testParam);
        assertDoesNotThrow(() -> {
            uninitializedService.close();
        });

        // 测试重复关闭
        assertDoesNotThrow(() -> {
            uninitializedService.close();
        });
    }

    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling() {
        // 测试无效配置
        JanusGraphParam invalidParam = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("invalid_backend")
                .build())
            .build();

        assertThrows(RuntimeException.class, () -> {
            new JanusGraphService(invalidParam);
        });
    }

    @Test
    @DisplayName("测试并发操作")
    @Disabled("需要JanusGraph运行环境")
    void testConcurrentOperations() {
        janusGraphService = new JanusGraphService(testParam);
        janusGraphService.initializeSchema(mockEmbedding);

        List<Document> documents = createTestDocuments();
        
        // 模拟并发添加文档
        assertDoesNotThrow(() -> {
            List<String> batch1 = janusGraphService.addDocuments(
                documents.subList(0, 2), mockEmbedding);
            List<String> batch2 = janusGraphService.addDocuments(
                documents.subList(2, documents.size()), mockEmbedding);
            
            assertNotNull(batch1);
            assertNotNull(batch2);
        });
    }

    @Test
    @DisplayName("测试大数据集处理")
    @Disabled("需要JanusGraph运行环境")
    void testLargeDatasetHandling() {
        janusGraphService = new JanusGraphService(testParam);
        janusGraphService.initializeSchema(mockEmbedding);

        List<Document> largeDataset = createLargeTestDocumentSet(100);
        
        assertDoesNotThrow(() -> {
            List<String> documentIds = janusGraphService.addDocuments(largeDataset, mockEmbedding);
            assertEquals(largeDataset.size(), documentIds.size());
        });
    }

    @Test
    @DisplayName("测试内存使用")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 创建并关闭多个服务实例
        for (int i = 0; i < 5; i++) {
            JanusGraphService service = new JanusGraphService(testParam);
            service.close();
        }
        
        // 强制垃圾回收
        System.gc();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // 确保内存增长在合理范围内（小于50MB）
        assertTrue(memoryIncrease < 50 * 1024 * 1024, 
            "Memory usage increased by " + (memoryIncrease / 1024 / 1024) + " MB");
    }

    // 辅助方法

    private List<Document> createTestDocuments() {
        Document doc1 = new Document();
        doc1.setPageContent("This is the first test document about machine learning.");
        doc1.setMetadata(Map.of("id", "doc1", "category", "AI"));

        Document doc2 = new Document();
        doc2.setPageContent("This document discusses natural language processing techniques.");
        doc2.setMetadata(Map.of("id", "doc2", "category", "NLP"));

        Document doc3 = new Document();
        doc3.setPageContent("Graph databases are useful for storing connected data.");
        doc3.setMetadata(Map.of("id", "doc3", "category", "Database"));

        return Arrays.asList(doc1, doc2, doc3);
    }

    private List<Document> createLargeTestDocumentSet(int size) {
        return java.util.stream.IntStream.range(0, size)
            .mapToObj(i -> {
                Document doc = new Document();
                doc.setPageContent("This is test document number " + i + " with some content.");
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("id", "doc" + i);
                metadata.put("index", i);
                metadata.put("category", "test");
                doc.setMetadata(metadata);
                return doc;
            })
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
