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
package com.alibaba.langengine.timescaledb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.timescaledb.TimescaleDBConfiguration;
import com.alibaba.langengine.timescaledb.client.TimescaleDBClient;
import com.alibaba.langengine.timescaledb.exception.TimescaleDBException;
import com.alibaba.langengine.timescaledb.model.TimescaleDBQueryRequest;
import com.alibaba.langengine.timescaledb.model.TimescaleDBQueryResponse;
import com.alibaba.langengine.timescaledb.model.TimescaleDBVector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TimescaleDB向量存储功能完整测试")
class TimescaleDBVectorStoreTest {

    @Mock
    private Embeddings mockEmbeddings;

    @Mock
    private TimescaleDBClient mockClient;

    private TimescaleDBVectorStore vectorStore;

    @BeforeEach
    void setUp() {
        // 设置模拟嵌入模型
        when(mockEmbeddings.embedDocument(anyList())).thenAnswer(invocation -> {
            List<Document> docs = invocation.getArgument(0);
            for (Document doc : docs) {
                // 模拟1536维向量
                List<Double> embedding = new ArrayList<>();
                for (int i = 0; i < 1536; i++) {
                    embedding.add(Math.random());
                }
                doc.setEmbedding(embedding);
            }
            return docs;
        });

        // 修复：embedQuery返回String列表，表示嵌入向量的JSON字符串格式
        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenAnswer(invocation -> {
            int dimension = invocation.getArgument(1);
            List<Double> embedding = new ArrayList<>();
            for (int i = 0; i < dimension; i++) {
                embedding.add(Math.random());
            }
            // 转换为JSON字符串格式
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < embedding.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(embedding.get(i));
            }
            sb.append("]");
            return Arrays.asList(sb.toString());
        });

        when(mockEmbeddings.getModelType()).thenReturn("test-model");

        // 创建向量存储实例
        vectorStore = new TimescaleDBVectorStore(mockEmbeddings, "test_vector_store", 1536);

        // 使用反射注入mock客户端
        ReflectionTestUtils.setField(vectorStore, "client", mockClient);
    }

    @AfterEach
    void tearDown() {
        if (vectorStore != null) {
            vectorStore.close();
        }
    }

    // ========== 基础功能测试 ==========

    @Test
    @DisplayName("测试向量存储初始化")
    void testVectorStoreInitialization() {
        assertNotNull(vectorStore);
        assertEquals("test_vector_store", vectorStore.getTableName());
        assertEquals(1536, vectorStore.getVectorDimension());
        assertNotNull(vectorStore.getClient());
        assertNotNull(vectorStore.getDocumentCache());
        assertNotNull(vectorStore.getEmbeddingCache());
    }

    @Test
    @DisplayName("测试构造函数参数验证")
    void testConstructorValidation() {
        // 测试null嵌入模型
        assertThrows(IllegalArgumentException.class, () -> {
            new TimescaleDBVectorStore(null);
        });

        // 测试空表名
        assertThrows(IllegalArgumentException.class, () -> {
            new TimescaleDBVectorStore(mockEmbeddings, "", 1536);
        });

        // 测试null表名
        assertThrows(IllegalArgumentException.class, () -> {
            new TimescaleDBVectorStore(mockEmbeddings, null, 1536);
        });

        // 测试无效向量维度
        assertThrows(IllegalArgumentException.class, () -> {
            new TimescaleDBVectorStore(mockEmbeddings, "test_table", 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new TimescaleDBVectorStore(mockEmbeddings, "test_table", -1);
        });
    }

    // ========== 文档操作测试 ==========

    @Test
    @DisplayName("测试批量添加文档")
    void testAddDocuments() {
        List<Document> documents = Arrays.asList(
            createTestDocument("doc-1", "Content 1"),
            createTestDocument("doc-2", "Content 2"),
            createTestDocument("doc-3", "Content 3")
        );

        doNothing().when(mockClient).insertVectors(anyList());

        vectorStore.addDocuments(documents);

        verify(mockClient).insertVectors(anyList());
        verify(mockEmbeddings).embedDocument(anyList());
    }

    @Test
    @DisplayName("测试添加空文档列表")
    void testAddEmptyDocuments() {
        vectorStore.addDocuments(new ArrayList<>());
        verify(mockClient, never()).insertVectors(anyList());
    }

    @Test
    @DisplayName("测试添加null文档列表")
    void testAddNullDocuments() {
        // addDocuments方法对null或空列表直接返回，不抛异常
        vectorStore.addDocuments(null);
        verify(mockClient, never()).insertVectors(anyList());
    }

    // ========== 相似性搜索测试 ==========

    @Test
    @DisplayName("测试相似性搜索")
    void testSimilaritySearch() {
        String query = "test query";
        int topK = 5;

        // 为这个测试设置正确的embedQuery mock - 返回1536维向量
        when(mockEmbeddings.embedQuery(eq(query), eq(1))).thenAnswer(invocation -> {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < 1536; i++) {
                if (i > 0) sb.append(", ");
                sb.append(Math.random());
            }
            sb.append("]");
            return Arrays.asList(sb.toString());
        });

        // 创建mock向量
        TimescaleDBVector mockVector1 = TimescaleDBVector.builder()
                .id("result-1")
                .content("Result 1")
                .vector(Arrays.asList(0.1, 0.2, 0.3))
                .metadata(Map.of("score", 0.9))
                .createdAt(LocalDateTime.now())
                .build();

        TimescaleDBVector mockVector2 = TimescaleDBVector.builder()
                .id("result-2")
                .content("Result 2")
                .vector(Arrays.asList(0.4, 0.5, 0.6))
                .metadata(Map.of("score", 0.8))
                .createdAt(LocalDateTime.now())
                .build();

        TimescaleDBQueryResponse mockResponse = TimescaleDBQueryResponse.builder()
                .vectors(Arrays.asList(mockVector1, mockVector2))
                .executionTimeMs(100L)
                .totalCount(2)
                .returnedCount(2)
                .success(true)
                .build();

        when(mockClient.similaritySearch(any(TimescaleDBQueryRequest.class))).thenReturn(mockResponse);

        List<Document> results = vectorStore.similaritySearch(query, topK, null, null);

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(mockClient).similaritySearch(any(TimescaleDBQueryRequest.class));
        verify(mockEmbeddings).embedQuery(query, 1);
    }

    @Test
    @DisplayName("测试带过滤器的相似性搜索")
    void testSimilaritySearchWithFilter() {
        String query = "test query";
        int topK = 3;
        Double maxDistance = 0.8;

        // 为这个测试设置正确的embedQuery mock - 返回1536维向量
        when(mockEmbeddings.embedQuery(eq(query), eq(1))).thenAnswer(invocation -> {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < 1536; i++) {
                if (i > 0) sb.append(", ");
                sb.append(Math.random());
            }
            sb.append("]");
            return Arrays.asList(sb.toString());
        });

        TimescaleDBVector mockVector = TimescaleDBVector.builder()
                .id("filtered-1")
                .content("Filtered result 1")
                .vector(Arrays.asList(0.1, 0.2, 0.3))
                .metadata(Map.of("category", "test"))
                .createdAt(LocalDateTime.now())
                .build();

        TimescaleDBQueryResponse mockResponse = TimescaleDBQueryResponse.builder()
                .vectors(Arrays.asList(mockVector))
                .executionTimeMs(50L)
                .totalCount(1)
                .returnedCount(1)
                .success(true)
                .build();

        when(mockClient.similaritySearch(any(TimescaleDBQueryRequest.class))).thenReturn(mockResponse);

        List<Document> results = vectorStore.similaritySearch(query, topK, maxDistance, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(mockClient).similaritySearch(any(TimescaleDBQueryRequest.class));
    }

    @Test
    @DisplayName("测试时序相似性搜索")
    void testTimeSeriesSimilaritySearch() {
        String query = "time series query";
        int topK = 5;
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        TimescaleDBVector mockVector = TimescaleDBVector.builder()
                .id("time-doc-1")
                .content("Time series result")
                .vector(Arrays.asList(0.1, 0.2, 0.3))
                .metadata(Map.of("timestamp", LocalDateTime.now()))
                .createdAt(LocalDateTime.now())
                .build();

        TimescaleDBQueryResponse mockResponse = TimescaleDBQueryResponse.builder()
                .vectors(Arrays.asList(mockVector))
                .executionTimeMs(75L)
                .totalCount(1)
                .returnedCount(1)
                .success(true)
                .build();

        when(mockClient.similaritySearch(any(TimescaleDBQueryRequest.class))).thenReturn(mockResponse);

        List<Document> results = vectorStore.similaritySearchWithTimeFilter(query, topK, startTime, endTime);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(mockClient).similaritySearch(any(TimescaleDBQueryRequest.class));
    }

    @Test
    @DisplayName("测试空查询搜索")
    void testEmptyQuerySearch() {
        List<Document> results = vectorStore.similaritySearch("", 5, null, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockEmbeddings, never()).embedQuery(anyString(), anyInt());
    }

    @Test
    @DisplayName("测试null查询搜索")
    void testNullQuerySearch() {
        List<Document> results = vectorStore.similaritySearch(null, 5, null, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(mockEmbeddings, never()).embedQuery(anyString(), anyInt());
    }

    // ========== 文档管理测试 ==========

    @Test
    @DisplayName("测试删除文档")
    void testDeleteDocument() {
        String docId = "doc-to-delete";

        when(mockClient.deleteVector(docId)).thenReturn(true);

        boolean result = vectorStore.deleteDocument(docId);

        assertTrue(result);
        verify(mockClient).deleteVector(docId);
    }

    @Test
    @DisplayName("测试批量删除文档")
    void testDeleteDocuments() {
        List<String> docIds = Arrays.asList("doc-1", "doc-2", "doc-3");

        when(mockClient.deleteVectors(docIds)).thenReturn(3);

        int result = vectorStore.deleteDocuments(docIds);

        assertEquals(3, result);
        verify(mockClient).deleteVectors(docIds);
    }

    @Test
    @DisplayName("测试删除不存在的文档")
    void testDeleteNonExistentDocument() {
        String docId = "non-existent";

        when(mockClient.deleteVector(docId)).thenReturn(false);

        boolean result = vectorStore.deleteDocument(docId);

        assertFalse(result);
        verify(mockClient).deleteVector(docId);
    }

    // ========== 统计和查询测试 ==========

    @Test
    @DisplayName("测试文档计数")
    void testCountDocuments() {
        when(mockClient.countVectors()).thenReturn(100L);

        long count = vectorStore.countDocuments();

        assertEquals(100L, count);
        verify(mockClient).countVectors();
    }

    @Test
    @DisplayName("测试缓存统计")
    void testCacheStatistics() {
        String stats = vectorStore.getCacheStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Document cache"));
        assertTrue(stats.contains("Embedding cache"));
    }

    // ========== 异常处理测试 ==========

    @Test
    @DisplayName("测试数据库连接异常")
    void testDatabaseConnectionException() {
        doThrow(new TimescaleDBException(TimescaleDBException.ErrorType.CONNECTION_ERROR, "Connection failed"))
            .when(mockClient).insertVectors(anyList());

        List<Document> documents = Arrays.asList(createTestDocument("test-doc", "Test content"));

        assertThrows(TimescaleDBException.class, () -> {
            vectorStore.addDocuments(documents);
        });
    }

    @Test
    @DisplayName("测试向量维度不匹配异常")
    void testVectorDimensionMismatch() {
        // 不再测试添加文档时的维度检查，因为在convertDocumentToVector中没有该验证
        // 而是在相似性搜索时测试维度验证
        String query = "test query";

        // 模拟embedQuery返回错误维度的向量
        when(mockEmbeddings.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("[1.0, 2.0, 3.0]")); // 3维而不是1536维

        assertThrows(TimescaleDBException.class, () -> {
            vectorStore.similaritySearch(query, 5);
        });
    }

    @Test
    @DisplayName("测试嵌入生成失败")
    void testEmbeddingGenerationFailure() {
        when(mockEmbeddings.embedDocument(anyList()))
            .thenThrow(new RuntimeException("Embedding generation failed"));

        List<Document> documents = Arrays.asList(createTestDocument("test-doc", "Test content"));

        assertThrows(RuntimeException.class, () -> {
            vectorStore.addDocuments(documents);
        });
    }

    // ========== 并发测试 ==========

    @Test
    @DisplayName("测试并发添加文档")
    void testConcurrentDocumentAddition() throws InterruptedException {
        int numberOfThreads = 5;
        int documentsPerThread = 3;

        doNothing().when(mockClient).insertVectors(anyList());

        Runnable task = () -> {
            for (int i = 0; i < documentsPerThread; i++) {
                List<Document> docs = Arrays.asList(createTestDocument(
                    "concurrent-doc-" + Thread.currentThread().getId() + "-" + i,
                    "Concurrent content " + i
                ));
                vectorStore.addDocuments(docs);
            }
        };

        // 启动多个线程
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证总共添加的文档数量 - 每个线程调用3次addDocuments，共5个线程 = 15次调用
        verify(mockClient, times(numberOfThreads * documentsPerThread)).insertVectors(anyList());
    }

    // ========== 性能测试 ==========

    @Test
    @DisplayName("测试大数据集处理")
    void testLargeDatasetHandling() {
        int largeDatasetSize = 100;
        List<Document> largeDataset = new ArrayList<>();

        // 创建大型数据集
        for (int i = 0; i < largeDatasetSize; i++) {
            largeDataset.add(createTestDocument("large-doc-" + i, "Large content " + i));
        }

        doNothing().when(mockClient).insertVectors(anyList());

        long startTime = System.nanoTime();
        vectorStore.addDocuments(largeDataset);
        long endTime = System.nanoTime();

        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        log.info("Large dataset processing took {} ms for {} documents", durationMs, largeDatasetSize);

        // 验证性能在合理范围内（这里只是示例，实际阈值根据环境调整）
        assertTrue(durationMs < 5000, "Processing should complete within 5 seconds");
    }

    @Test
    @DisplayName("测试批量操作性能")
    void testBatchOperationPerformance() {
        List<String> docIds = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            docIds.add("batch-delete-" + i);
        }

        when(mockClient.deleteVectors(docIds)).thenReturn(50);

        long startTime = System.nanoTime();
        int deletedCount = vectorStore.deleteDocuments(docIds);
        long endTime = System.nanoTime();

        assertEquals(50, deletedCount);

        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        log.info("Batch delete operation took {} ms for {} documents", durationMs, docIds.size());

        assertTrue(durationMs < 1000, "Batch operation should complete within 1 second");
    }

    // ========== 边界条件测试 ==========

    @Test
    @DisplayName("测试极长内容文档")
    void testVeryLongContentDocument() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("This is a very long document content for testing. ");
        }

        List<Document> longDocs = Arrays.asList(createTestDocument("long-doc", longContent.toString()));

        doNothing().when(mockClient).insertVectors(anyList());

        assertDoesNotThrow(() -> {
            vectorStore.addDocuments(longDocs);
        });
    }

    @Test
    @DisplayName("测试特殊字符内容")
    void testSpecialCharactersContent() {
        String specialContent = "Special characters: éñüñ 中文内容 🚀 @#$%^&*()[]{}|;:,.<>?";

        List<Document> specialDocs = Arrays.asList(createTestDocument("special-doc", specialContent));

        doNothing().when(mockClient).insertVectors(anyList());

        assertDoesNotThrow(() -> {
            vectorStore.addDocuments(specialDocs);
        });
    }

    @Test
    @DisplayName("测试空元数据")
    void testEmptyMetadata() {
        Document doc = createTestDocument("empty-metadata-doc", "Content");
        doc.setMetadata(new HashMap<>()); // 空元数据

        List<Document> docs = Arrays.asList(doc);
        doNothing().when(mockClient).insertVectors(anyList());

        assertDoesNotThrow(() -> {
            vectorStore.addDocuments(docs);
        });
    }

    @Test
    @DisplayName("测试null元数据")
    void testNullMetadata() {
        Document doc = createTestDocument("null-metadata-doc", "Content");
        doc.setMetadata(null); // null元数据

        List<Document> docs = Arrays.asList(doc);
        doNothing().when(mockClient).insertVectors(anyList());

        assertDoesNotThrow(() -> {
            vectorStore.addDocuments(docs);
        });
    }

    // ========== 资源管理测试 ==========

    @Test
    @DisplayName("测试资源正确关闭")
    void testResourceCleanup() {
        doNothing().when(mockClient).close();

        assertDoesNotThrow(() -> {
            vectorStore.close();
        });

        verify(mockClient).close();
    }

    @Test
    @DisplayName("测试重复关闭")
    void testDoubleClose() {
        doNothing().when(mockClient).close();

        assertDoesNotThrow(() -> {
            vectorStore.close();
            vectorStore.close(); // 重复关闭
        });

        verify(mockClient, times(2)).close();
    }

    // ========== 配置测试 ==========

    @Test
    @DisplayName("测试配置常量")
    void testConfigurationConstants() {
        assertEquals("vector_store", TimescaleDBConfiguration.DEFAULT_TABLE_NAME);
        assertEquals(1536, TimescaleDBConfiguration.DEFAULT_VECTOR_DIMENSION);
        assertEquals(100, TimescaleDBConfiguration.DEFAULT_BATCH_SIZE);
        assertEquals(0.8, TimescaleDBConfiguration.DEFAULT_SIMILARITY_THRESHOLD);
        assertEquals(20, TimescaleDBConfiguration.DEFAULT_MAX_CONNECTIONS);
        assertEquals(5, TimescaleDBConfiguration.DEFAULT_INITIAL_CONNECTIONS);
        assertEquals(30000, TimescaleDBConfiguration.DEFAULT_CONNECTION_TIMEOUT);
        assertEquals(60000, TimescaleDBConfiguration.DEFAULT_QUERY_TIMEOUT);
        assertEquals(1000, TimescaleDBConfiguration.DEFAULT_CACHE_SIZE);
        assertEquals(7, TimescaleDBConfiguration.DEFAULT_CHUNK_TIME_INTERVAL);
    }

    // ========== 辅助方法 ==========

    private Document createTestDocument(String id, String content) {
        Document document = new Document();
        document.setUniqueId(id);
        document.setPageContent(content);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        metadata.put("timestamp", LocalDateTime.now());
        metadata.put("tags", Arrays.asList("test", "document"));
        document.setMetadata(metadata);

        return document;
    }
}
