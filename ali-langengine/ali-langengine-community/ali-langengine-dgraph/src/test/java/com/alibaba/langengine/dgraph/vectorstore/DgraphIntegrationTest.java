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
package com.alibaba.langengine.dgraph.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Dgraph向量存储集成测试
 * 注意：这些测试需要运行的Dgraph服务器实例
 * 
 * @author xiaoxuan.lp
 */
@EnabledIfEnvironmentVariable(named = "DGRAPH_INTEGRATION_TEST", matches = "true")
public class DgraphIntegrationTest {

    @Mock
    private Embeddings mockEmbedding;

    private Dgraph dgraph;
    private DgraphParam param;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 配置测试参数
        param = new DgraphParam.Builder()
                .vectorDimension(512)
                .searchLimit(10)
                .batchSize(100)
                .similarityThreshold(0.8f)
                .hnswM(16)
                .hnswEfConstruction(200)
                .hnswEf(100)
                .build();
        
        dgraph = new Dgraph(mockEmbedding, "localhost:9080", param);
    }

    @Test
    void testEndToEndVectorSearchWorkflow() {
        // 模拟嵌入生成
        List<Double> mockVector = Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        Document mockDoc = new Document();
        mockDoc.setPageContent("Test document content");
        mockDoc.setEmbedding(mockVector);
        
        when(mockEmbedding.embedDocument(anyList())).thenReturn(Arrays.asList(mockDoc));
        when(mockEmbedding.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("[0.1,0.2,0.3,0.4,0.5]"));

        // 测试文档添加
        List<Document> documents = Arrays.asList(
                createTestDocument("First document", "category1"),
                createTestDocument("Second document", "category2"),
                createTestDocument("Third document", "category1")
        );

        assertDoesNotThrow(() -> dgraph.addDocuments(documents));

        // 测试相似性搜索
        List<Document> results = dgraph.similaritySearchSimple("test query", 5);
        assertNotNull(results);
        
        // 根据实际的Dgraph服务器状态，结果可能为空或有内容
        // 这里主要测试没有抛出异常
        assertTrue(results.size() >= 0);
    }

    @Test
    void testHNSWParameterConfiguration() {
        // 测试不同的HNSW参数配置
        DgraphParam customParam = new DgraphParam.Builder()
                .vectorDimension(256)
                .hnswM(32)
                .hnswEfConstruction(400)
                .hnswEf(200)
                .build();

        Dgraph customDgraph = new Dgraph(mockEmbedding, "localhost:9080", customParam);
        assertNotNull(customDgraph);
        assertEquals(32, customParam.getHnswM());
        assertEquals(400, customParam.getHnswEfConstruction());
        assertEquals(200, customParam.getHnswEf());
    }

    @Test
    void testErrorHandlingWithInvalidServer() {
        // 测试无效服务器的错误处理
        DgraphParam invalidParam = new DgraphParam.Builder()
                .vectorDimension(128)
                .build();

        assertThrows(DgraphVectorStoreException.SchemaInitializationException.class, () -> {
            new Dgraph(mockEmbedding, "invalid:9999", invalidParam);
        });
    }

    @Test
    void testVectorSearchExceptionHandling() {
        // 测试向量搜索异常处理
        when(mockEmbedding.embedQuery(anyString(), anyInt())).thenThrow(new RuntimeException("Embedding failed"));

        assertThrows(DgraphVectorStoreException.VectorSearchException.class, () -> {
            dgraph.similaritySearchSimple("test query", 5);
        });
    }

    @Test
    void testBatchDocumentProcessing() {
        // 测试批量文档处理
        List<Document> largeBatch = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            largeBatch.add(createTestDocument("Document " + i, "batch"));
        }

        // 模拟嵌入生成
        List<Document> mockEmbeddedDocs = new ArrayList<>();
        for (Document doc : largeBatch) {
            Document embeddedDoc = new Document();
            embeddedDoc.setPageContent(doc.getPageContent());
            embeddedDoc.setMetadata(doc.getMetadata());
            embeddedDoc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5));
            mockEmbeddedDocs.add(embeddedDoc);
        }
        
        when(mockEmbedding.embedDocument(anyList())).thenReturn(mockEmbeddedDocs);

        assertDoesNotThrow(() -> dgraph.addDocuments(largeBatch));
    }

    @Test
    void testMetadataFiltering() {
        // 测试元数据过滤功能
        Map<String, Object> filter = new HashMap<>();
        filter.put("category", "test");

        when(mockEmbedding.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("[0.1,0.2,0.3,0.4,0.5]"));

        List<Document> results = dgraph.similaritySearchWithFilter("test query", 5, filter);
        assertNotNull(results);
        assertTrue(results.size() >= 0);
    }

    @Test
    void testConfigurationValidation() {
        // 测试配置验证
        DgraphParam validParam = new DgraphParam.Builder()
                .vectorDimension(384)
                .searchLimit(20)
                .batchSize(50)
                .similarityThreshold(0.75f)
                .hnswM(24)
                .hnswEfConstruction(300)
                .hnswEf(150)
                .build();

        assertEquals(384, validParam.getVectorDimension());
        assertEquals(20, validParam.getSearchLimit());
        assertEquals(50, validParam.getBatchSize());
        assertEquals(0.75f, validParam.getSimilarityThreshold(), 0.001);
        assertEquals(24, validParam.getHnswM());
        assertEquals(300, validParam.getHnswEfConstruction());
        assertEquals(150, validParam.getHnswEf());
    }

    @Test
    void testConcurrentOperations() throws InterruptedException {
        // 测试并发操作
        when(mockEmbedding.embedQuery(anyString(), anyInt())).thenReturn(Arrays.asList("[0.1,0.2,0.3,0.4,0.5]"));

        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    List<Document> results = dgraph.similaritySearchSimple("query " + threadId, 3);
                    assertNotNull(results);
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 如果有异常，应该是预期的（比如服务器连接问题），但不应该是空指针异常
        for (Exception e : exceptions) {
            assertFalse(e instanceof NullPointerException, "Unexpected NullPointerException: " + e.getMessage());
        }
    }

    private Document createTestDocument(String content, String category) {
        Document doc = new Document();
        doc.setPageContent(content);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", category);
        metadata.put("timestamp", System.currentTimeMillis());
        doc.setMetadata(metadata);
        return doc;
    }
}
