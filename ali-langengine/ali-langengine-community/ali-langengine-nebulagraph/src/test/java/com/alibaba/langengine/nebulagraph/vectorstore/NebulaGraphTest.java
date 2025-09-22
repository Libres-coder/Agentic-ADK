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
package com.alibaba.langengine.nebulagraph.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.nebulagraph.NebulaGraphConfiguration;
import com.alibaba.langengine.nebulagraph.exception.NebulaGraphVectorStoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NebulaGraphTest {

    private NebulaGraph nebulaGraph;
    private FakeEmbeddings fakeEmbeddings;
    private String testSpaceName;

    @BeforeEach
    public void setUp() {
        // 使用测试参数
        NebulaGraphParam param = NebulaGraphParam.createForTesting();
        fakeEmbeddings = new FakeEmbeddings();
        testSpaceName = "test_space_" + UUID.randomUUID().toString().replace("-", "");

        try {
            // 验证配置
            NebulaGraphConfiguration.validateConfiguration();

            // 创建 NebulaGraph 实例
            nebulaGraph = new NebulaGraph(fakeEmbeddings, testSpaceName, "TestDocument", param);
        } catch (Exception e) {
            // 如果无法连接到 NebulaGraph，跳过测试
            org.junit.jupiter.api.Assumptions.assumeTrue(false,
                "NebulaGraph not available for testing: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (nebulaGraph != null) {
            try {
                nebulaGraph.close();
            } catch (Exception e) {
                // 忽略关闭错误
            }
        }
    }

    @Test
    public void testConstructorWithDefaults() {
        assertNotNull(nebulaGraph);
        assertEquals(testSpaceName, nebulaGraph.getSpaceName());
        assertEquals("TestDocument", nebulaGraph.getTagName());
        assertNotNull(nebulaGraph.getEmbedding());
        assertNotNull(nebulaGraph.getParam());
    }

    @Test
    public void testConstructorWithNullParameters() {
        // 测试空参数的处理
        NebulaGraph graph = new NebulaGraph(fakeEmbeddings, null, null, null);
        assertNotNull(graph.getSpaceName());
        assertEquals("Document", graph.getTagName());
        assertNotNull(graph.getParam());
    }

    @Test
    public void testAddDocuments() {
        List<Document> documents = createTestDocuments();

        assertDoesNotThrow(() -> {
            nebulaGraph.addDocuments(documents);
        });
    }

    @Test
    public void testAddEmptyDocuments() {
        List<Document> emptyDocuments = new ArrayList<>();

        assertDoesNotThrow(() -> {
            nebulaGraph.addDocuments(emptyDocuments);
        });
    }

    @Test
    public void testAddNullDocuments() {
        assertDoesNotThrow(() -> {
            nebulaGraph.addDocuments(null);
        });
    }

    @Test
    public void testAddDocumentsWithoutEmbedding() {
        List<Document> documents = createTestDocuments();
        documents.forEach(doc -> doc.setEmbedding(null));

        assertDoesNotThrow(() -> {
            nebulaGraph.addDocuments(documents);
        });
    }

    @Test
    public void testSimilaritySearch() {
        // 先添加一些测试数据
        List<Document> documents = createTestDocuments();
        nebulaGraph.addDocuments(documents);

        // 执行相似度搜索
        List<Document> results = nebulaGraph.similaritySearch("test content", 5, 0.7, null);

        assertNotNull(results);
        assertTrue(results.size() <= 5);
    }

    @Test
    public void testSimilaritySearchWithEmptyQuery() {
        List<Document> results = nebulaGraph.similaritySearch("", 5, 0.7, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSimilaritySearchWithNullQuery() {
        List<Document> results = nebulaGraph.similaritySearch(null, 5, 0.7, null);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSimilaritySearchWithInvalidParameters() {
        assertThrows(NebulaGraphVectorStoreException.class, () -> {
            nebulaGraph.similaritySearch("test", 0, 0.7, null);
        });

        assertThrows(NebulaGraphVectorStoreException.class, () -> {
            nebulaGraph.similaritySearch("test", -1, 0.7, null);
        });
    }

    @Test
    public void testDeleteDocuments() {
        // 先添加一些测试数据
        List<Document> documents = createTestDocuments();
        nebulaGraph.addDocuments(documents);

        // 删除文档
        List<String> idsToDelete = new ArrayList<>();
        idsToDelete.add(documents.get(0).getUniqueId());

        assertDoesNotThrow(() -> {
            nebulaGraph.deleteDocuments(idsToDelete);
        });
    }

    @Test
    public void testDeleteEmptyDocuments() {
        List<String> emptyIds = new ArrayList<>();

        assertDoesNotThrow(() -> {
            nebulaGraph.deleteDocuments(emptyIds);
        });
    }

    @Test
    public void testDeleteNullDocuments() {
        assertDoesNotThrow(() -> {
            nebulaGraph.deleteDocuments(null);
        });
    }

    @Test
    public void testParamValidation() {
        NebulaGraphParam param = NebulaGraphParam.createDefault();
        assertDoesNotThrow(() -> {
            param.validate();
        });

        // 测试无效参数
        NebulaGraphParam invalidParam = NebulaGraphParam.createDefault();
        invalidParam.getInitParam().setDimension(-1);

        assertThrows(IllegalArgumentException.class, () -> {
            invalidParam.validate();
        });
    }

    @Test
    public void testConfigurationValidation() {
        assertDoesNotThrow(() -> {
            NebulaGraphConfiguration.validateConfiguration();
        });
    }

    @Test
    public void testVectorDimensionConsistency() {
        List<Document> documents = createTestDocuments();

        // 确保所有文档的向量维度一致
        int expectedDimension = documents.get(0).getEmbedding().size();
        for (Document doc : documents) {
            assertEquals(expectedDimension, doc.getEmbedding().size());
        }
    }

    @Test
    public void testMetadataHandling() {
        List<Document> documents = createTestDocumentsWithMetadata();

        assertDoesNotThrow(() -> {
            nebulaGraph.addDocuments(documents);
        });

        // 测试包含元数据的搜索
        List<Document> results = nebulaGraph.similaritySearch("test content", 5, 0.5, null);

        assertNotNull(results);
        for (Document result : results) {
            assertNotNull(result.getMetadata());
        }
    }

    @Test
    public void testLargeVectorBatch() {
        List<Document> largeDocuments = new ArrayList<>();

        // 创建较大批次的文档
        for (int i = 0; i < 150; i++) {
            Document doc = new Document();
            doc.setUniqueId("large_doc_" + i);
            doc.setPageContent("Large batch test document " + i);
            doc.setEmbedding(fakeEmbeddings.embedTexts(List.of(doc.getPageContent())).get(0).getEmbedding());
            doc.setMetadata(Map.of("batch", "large", "index", i));
            largeDocuments.add(doc);
        }

        assertDoesNotThrow(() -> {
            nebulaGraph.addDocuments(largeDocuments);
        });
    }

    @Test
    public void testConcurrentOperations() {
        List<Document> documents = createTestDocuments();

        // 测试并发添加文档
        assertDoesNotThrow(() -> {
            Thread thread1 = new Thread(() -> nebulaGraph.addDocuments(documents.subList(0, 2)));
            Thread thread2 = new Thread(() -> nebulaGraph.addDocuments(documents.subList(2, 4)));

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();
        });
    }

    /**
     * 创建测试文档
     */
    private List<Document> createTestDocuments() {
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Document doc = new Document();
            doc.setUniqueId("test_doc_" + i);
            doc.setPageContent("This is test content for document " + i);
            doc.setEmbedding(fakeEmbeddings.embedTexts(List.of(doc.getPageContent())).get(0).getEmbedding());
            doc.setMetadata(new HashMap<>());
            documents.add(doc);
        }

        return documents;
    }

    /**
     * 创建带元数据的测试文档
     */
    private List<Document> createTestDocumentsWithMetadata() {
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Document doc = new Document();
            doc.setUniqueId("meta_doc_" + i);
            doc.setPageContent("Document with metadata " + i);
            doc.setEmbedding(fakeEmbeddings.embedTexts(List.of(doc.getPageContent())).get(0).getEmbedding());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", "Test Document " + i);
            metadata.put("doc_type", "test");
            metadata.put("doc_index", "index_" + i);
            metadata.put("tags", List.of("test", "metadata", "document"));
            metadata.put("category", "testing");
            metadata.put("priority", i + 1);
            doc.setMetadata(metadata);

            documents.add(doc);
        }

        return documents;
    }
}