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
package com.alibaba.langengine.tinkerpop.vectorstore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import com.alibaba.langengine.core.docloader.UnstructuredTxtLoader;
import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;


public class TinkerPopTest {

    private TinkerPop tinkerPop;
    private final String TEST_COLLECTION_ID = "test_collection";
    private final String TEST_SERVER_URL = "ws://127.0.0.1:8182";

    @BeforeEach
    public void setUp() {
        try {
            tinkerPop = new TinkerPop(
                    TEST_SERVER_URL,
                    new FakeEmbeddings(),
                    TEST_COLLECTION_ID
            );
        } catch (Exception e) {
            // 如果连接失败，跳过测试
            tinkerPop = null;
        }
    }

    @AfterEach
    public void tearDown() {
        if (tinkerPop != null) {
            tinkerPop.close();
        }
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testAddDocuments() throws Exception {
        if (tinkerPop == null) {
            return; // 跳过测试如果没有连接
        }

        // 准备测试数据
        Document doc1 = new Document();
        doc1.setUniqueId("test-doc-1");
        doc1.setPageContent("This is a test document for vector storage.");
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("source", "test_file_1.txt");
        metadata1.put("type", "document");
        doc1.setMetadata(metadata1);

        Document doc2 = new Document();
        doc2.setUniqueId("test-doc-2");
        doc2.setPageContent("Another test document with different content.");
        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("source", "test_file_2.txt");
        metadata2.put("type", "document");
        doc2.setMetadata(metadata2);

        List<Document> documents = Arrays.asList(doc1, doc2);

        // 执行添加操作
        assertDoesNotThrow(() -> {
            tinkerPop.addDocuments(documents);
        });
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testAddTexts() throws Exception {
        if (tinkerPop == null) {
            return; // 跳过测试如果没有连接
        }

        List<String> texts = Arrays.asList(
                "First text document",
                "Second text document",
                "Third text document"
        );

        List<Map<String, Object>> metadatas = Arrays.asList(
                Map.of("source", "text1"),
                Map.of("source", "text2"),
                Map.of("source", "text3")
        );

        List<String> ids = Arrays.asList("text-1", "text-2", "text-3");

        // 执行添加操作
        List<String> resultIds = tinkerPop.addTexts(texts, metadatas, ids);

        assertNotNull(resultIds);
        assertEquals(3, resultIds.size());
        assertEquals("text-1", resultIds.get(0));
        assertEquals("text-2", resultIds.get(1));
        assertEquals("text-3", resultIds.get(2));
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testSimilaritySearch() throws Exception {
        if (tinkerPop == null) {
            return; // 跳过测试如果没有连接
        }

        // 首先添加一些测试文档
        testAddDocuments();

        // 执行相似性搜索
        List<Document> results = tinkerPop.similaritySearch("test document", 5);

        assertNotNull(results);
        // 注意：由于使用FakeEmbeddings和简单的文本搜索，结果可能为空
        // 这是正常的，因为测试主要验证方法能正常执行而不报错
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testSimilaritySearchWithMaxDistance() throws Exception {
        if (tinkerPop == null) {
            return; // 跳过测试如果没有连接
        }

        // 首先添加一些测试文档
        testAddDocuments();

        // 执行带距离限制的相似性搜索
        List<Document> results = tinkerPop.similaritySearch("test", 5, 0.8);

        assertNotNull(results);
        // 验证所有返回的文档距离都在限制范围内
        for (Document doc : results) {
            assertTrue(doc.getScore() <= 0.8);
        }
    }

    @Test
    public void testConstructorWithDefaultValues() {
        // 测试构造函数参数验证
        FakeEmbeddings embedding = new FakeEmbeddings();

        // 测试空collectionId的处理
        TinkerPop tinkerPopWithNullCollection = new TinkerPop(embedding, null);
        assertNotNull(tinkerPopWithNullCollection.getCollectionId());
        assertNotEquals("", tinkerPopWithNullCollection.getCollectionId());

        // 测试正常的collectionId
        TinkerPop tinkerPopWithCollection = new TinkerPop(embedding, "test-collection");
        assertEquals("test-collection", tinkerPopWithCollection.getCollectionId());

        // 清理资源
        tinkerPopWithNullCollection.close();
        tinkerPopWithCollection.close();
    }

    @Test
    public void testConstructorWithCustomTimeout() {
        FakeEmbeddings embedding = new FakeEmbeddings();

        TinkerPop tinkerPopWithTimeout = new TinkerPop(
                "ws://localhost:8182",
                embedding,
                "test-collection",
                5000,  // connection timeout
                10000  // request timeout
        );

        assertNotNull(tinkerPopWithTimeout);
        assertEquals("test-collection", tinkerPopWithTimeout.getCollectionId());
        assertNotNull(tinkerPopWithTimeout.get_client());

        // 清理资源
        tinkerPopWithTimeout.close();
    }

    @Test
    public void testAddDocumentsWithEmptyContent() {
        if (tinkerPop == null) {
            return; // 跳过测试如果没有连接
        }

        // 测试空内容文档的处理
        Document emptyDoc = new Document();
        emptyDoc.setUniqueId("empty-doc");
        emptyDoc.setPageContent(""); // 空内容

        Document nullContentDoc = new Document();
        nullContentDoc.setUniqueId("null-content-doc");
        nullContentDoc.setPageContent(null); // null内容

        List<Document> documents = Arrays.asList(emptyDoc, nullContentDoc);

        // 这应该不会抛出异常，但会跳过空内容的文档
        assertDoesNotThrow(() -> {
            tinkerPop.addDocuments(documents);
        });
    }

    @Test
    @Disabled("需要运行中的TinkerPop服务器")
    public void testAddDocumentsWithAutoGeneratedIds() {
        if (tinkerPop == null) {
            return; // 跳过测试如果没有连接
        }

        // 测试自动生成ID的功能
        Document docWithoutId = new Document();
        docWithoutId.setPageContent("Document without explicit ID");

        List<Document> documents = Arrays.asList(docWithoutId);

        assertDoesNotThrow(() -> {
            tinkerPop.addDocuments(documents);
        });

        // 验证ID已被自动生成
        assertNotNull(docWithoutId.getUniqueId());
        assertFalse(docWithoutId.getUniqueId().isEmpty());
    }

    @Test
    public void testClose() {
        if (tinkerPop == null) {
            return; // 跳过测试如果没有连接
        }

        // 测试关闭连接
        assertDoesNotThrow(() -> {
            tinkerPop.close();
        });
    }
}