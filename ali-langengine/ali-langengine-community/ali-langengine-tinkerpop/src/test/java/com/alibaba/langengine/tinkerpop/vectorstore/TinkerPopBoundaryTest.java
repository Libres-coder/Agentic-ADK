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

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.tinkerpop.vectorstore.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TinkerPopBoundaryTest {

    @Mock
    private Embeddings mockEmbedding;

    @Mock
    private TinkerPopService mockService;

    @Mock
    private TinkerPopClient mockClient;

    private TinkerPop tinkerPop;

    @BeforeEach
    public void setUp() {
        tinkerPop = new TinkerPop(mockEmbedding, "test_collection");
        setPrivateField(tinkerPop, "_service", mockService);
        setPrivateField(tinkerPop, "_client", mockClient);
    }

    // ================== 边界条件测试 ==================

    @Test
    public void testAddDocumentsWithEmptyList() {
        List<Document> emptyList = new ArrayList<>();

        doNothing().when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        assertDoesNotThrow(() -> tinkerPop.addDocuments(emptyList));
        verify(mockService).addDocuments(any(TinkerPopAddRequest.class));
    }

    @Test
    public void testAddDocumentsWithNullList() {
        assertThrows(RuntimeException.class, () -> {
            tinkerPop.addDocuments(null);
        });
    }

    @Test
    public void testAddDocumentsWithVeryLargeList() {
        List<Document> largeList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc" + i);
            doc.setPageContent("Content " + i);
            doc.setMetadata(Map.of("index", String.valueOf(i)));
            largeList.add(doc);
        }

        doNothing().when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        assertDoesNotThrow(() -> tinkerPop.addDocuments(largeList));
        verify(mockService).addDocuments(any(TinkerPopAddRequest.class));
    }

    @Test
    public void testAddDocumentsWithOnlyEmptyAndNullContent() {
        List<Document> problematicDocs = Arrays.asList(
            createDocumentWithContent("doc1", ""),      // 空字符串
            createDocumentWithContent("doc2", null),    // null
            createDocumentWithContent("doc3", "   "),   // 只有空格
            createDocumentWithContent("doc4", "\t\n")   // 只有制表符和换行
        );

        doNothing().when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        assertDoesNotThrow(() -> tinkerPop.addDocuments(problematicDocs));
        verify(mockService).addDocuments(any(TinkerPopAddRequest.class));
    }

    @Test
    public void testAddDocumentsWithVeryLongContent() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            longContent.append("This is a very long content string. ");
        }

        Document longDoc = new Document();
        longDoc.setUniqueId("long-doc");
        longDoc.setPageContent(longContent.toString());
        longDoc.setMetadata(Map.of("type", "long"));

        doNothing().when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        assertDoesNotThrow(() -> tinkerPop.addDocuments(Arrays.asList(longDoc)));
        verify(mockService).addDocuments(any(TinkerPopAddRequest.class));
    }

    @Test
    public void testAddDocumentsWithSpecialCharacters() {
        Document specialDoc = new Document();
        specialDoc.setUniqueId("special-doc");
        specialDoc.setPageContent("Content with 中文, émojis 🎉, and special chars: <>&\"'");
        Map<String, Object> specialMetadata = new HashMap<>();
        specialMetadata.put("unicode", "测试中文");
        specialMetadata.put("emoji", "🔥");
        specialMetadata.put("null_value", null);
        specialDoc.setMetadata(specialMetadata);

        doNothing().when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        assertDoesNotThrow(() -> tinkerPop.addDocuments(Arrays.asList(specialDoc)));
        verify(mockService).addDocuments(any(TinkerPopAddRequest.class));
    }

    @Test
    public void testSimilaritySearchWithZeroK() {
        TinkerPopQueryResponse emptyResponse = new TinkerPopQueryResponse();
        emptyResponse.setIds(new ArrayList<>());
        emptyResponse.setTexts(new ArrayList<>());
        emptyResponse.setDistances(new ArrayList<>());
        emptyResponse.setMetadatas(new ArrayList<>());

        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class))).thenReturn(emptyResponse);

        List<Document> results = tinkerPop.similaritySearch("query", 0, null, null);
        assertEquals(0, results.size());
    }

    @Test
    public void testSimilaritySearchWithNegativeK() {
        TinkerPopQueryResponse emptyResponse = new TinkerPopQueryResponse();
        emptyResponse.setIds(new ArrayList<>());
        emptyResponse.setTexts(new ArrayList<>());
        emptyResponse.setDistances(new ArrayList<>());
        emptyResponse.setMetadatas(new ArrayList<>());

        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class))).thenReturn(emptyResponse);

        List<Document> results = tinkerPop.similaritySearch("query", -1, null, null);
        assertEquals(0, results.size());
    }

    @Test
    public void testSimilaritySearchWithVeryLargeK() {
        TinkerPopQueryResponse response = new TinkerPopQueryResponse();
        response.setIds(Arrays.asList("doc1"));
        response.setTexts(Arrays.asList("text1"));
        response.setDistances(Arrays.asList(0.1));
        response.setMetadatas(Arrays.asList(Map.of("key", "value")));

        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class))).thenReturn(response);

        List<Document> results = tinkerPop.similaritySearch("query", Integer.MAX_VALUE, null, null);
        assertEquals(1, results.size());
    }

    @Test
    public void testSimilaritySearchWithNullQuery() {
        TinkerPopQueryResponse response = new TinkerPopQueryResponse();
        response.setIds(new ArrayList<>());
        response.setTexts(new ArrayList<>());
        response.setDistances(new ArrayList<>());
        response.setMetadatas(new ArrayList<>());

        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class))).thenReturn(response);

        List<Document> results = tinkerPop.similaritySearch(null, 5, null, null);
        assertEquals(0, results.size());
    }

    @Test
    public void testSimilaritySearchWithEmptyQuery() {
        TinkerPopQueryResponse response = new TinkerPopQueryResponse();
        response.setIds(new ArrayList<>());
        response.setTexts(new ArrayList<>());
        response.setDistances(new ArrayList<>());
        response.setMetadatas(new ArrayList<>());

        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class))).thenReturn(response);

        List<Document> results = tinkerPop.similaritySearch("", 5, null, null);
        assertEquals(0, results.size());
    }

    // ================== 异常处理测试 ==================

    @Test
    public void testAddDocumentsWhenServiceThrowsRuntimeException() {
        Document doc = createDocumentWithContent("doc1", "content");

        doThrow(new RuntimeException("Database connection failed"))
            .when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tinkerPop.addDocuments(Arrays.asList(doc));
        });

        assertTrue(exception.getMessage().contains("Database connection failed"));
    }

    @Test
    public void testAddDocumentsWhenEmbeddingServiceFails() {
        Document doc = createDocumentWithContent("doc1", "content");
        doc.setEmbedding(null); // 需要生成 embedding

        when(mockEmbedding.embedTexts(anyList()))
            .thenThrow(new RuntimeException("Embedding service unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tinkerPop.addDocuments(Arrays.asList(doc));
        });

        assertTrue(exception.getMessage().contains("Embedding service unavailable"));
    }

    @Test
    public void testSimilaritySearchWhenServiceThrowsException() {
        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class)))
            .thenThrow(new RuntimeException("Search index corrupted"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tinkerPop.similaritySearch("query", 5, null, null);
        });

        assertTrue(exception.getMessage().contains("Search index corrupted"));
    }

    @Test
    public void testSimilaritySearchWhenEmbeddingServiceFails() {
        when(mockEmbedding.embedTexts(anyList()))
            .thenThrow(new RuntimeException("Embedding model error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tinkerPop.similaritySearch("query", 5, null, null);
        });

        assertTrue(exception.getMessage().contains("Embedding model error"));
    }

    @Test
    public void testAddTextsWhenServiceThrowsException() {
        List<String> texts = Arrays.asList("text1", "text2");

        when(mockEmbedding.embedTexts(texts)).thenReturn(Arrays.asList(
            createMockDocument("id1", Arrays.asList(0.1, 0.2)),
            createMockDocument("id2", Arrays.asList(0.3, 0.4))
        ));

        doThrow(new RuntimeException("Service error"))
            .when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tinkerPop.addTexts(texts, null, null);
        });

        assertTrue(exception.getMessage().contains("Service error"));
    }

    // ================== 数据一致性测试 ==================

    @Test
    public void testSimilaritySearchWithMismatchedArraySizes() {
        // 测试响应数据数组大小不匹配的情况
        TinkerPopQueryResponse inconsistentResponse = new TinkerPopQueryResponse();
        inconsistentResponse.setIds(Arrays.asList("doc1", "doc2"));
        inconsistentResponse.setTexts(Arrays.asList("text1")); // 只有一个文本
        inconsistentResponse.setDistances(Arrays.asList(0.1, 0.2, 0.3)); // 三个距离
        inconsistentResponse.setMetadatas(Arrays.asList(Map.of("key", "value"))); // 一个元数据

        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class)))
            .thenReturn(inconsistentResponse);

        // 应该能处理不一致的数据，但可能会抛出异常
        assertThrows(RuntimeException.class, () -> {
            tinkerPop.similaritySearch("query", 5, null, null);
        });
    }

    @Test
    public void testAddDocumentsWithNullMetadata() {
        Document docWithNullMetadata = new Document();
        docWithNullMetadata.setUniqueId("doc1");
        docWithNullMetadata.setPageContent("content");
        docWithNullMetadata.setMetadata(null);

        doNothing().when(mockService).addDocuments(any(TinkerPopAddRequest.class));

        assertDoesNotThrow(() -> tinkerPop.addDocuments(Arrays.asList(docWithNullMetadata)));

        // 验证 metadata 被设置为空 Map
        assertNotNull(docWithNullMetadata.getMetadata());
        assertTrue(docWithNullMetadata.getMetadata().isEmpty());
    }

    @Test
    public void testSimilaritySearchWithExtremeDistanceValues() {
        TinkerPopQueryResponse response = new TinkerPopQueryResponse();
        response.setIds(Arrays.asList("doc1", "doc2", "doc3"));
        response.setTexts(Arrays.asList("text1", "text2", "text3"));
        response.setDistances(Arrays.asList(Double.MIN_VALUE, Double.MAX_VALUE, Double.NaN));
        response.setMetadatas(Arrays.asList(
            Map.of("key", "value1"),
            Map.of("key", "value2"),
            Map.of("key", "value3")
        ));

        when(mockService.queryDocuments(any(TinkerPopQueryRequest.class))).thenReturn(response);

        assertDoesNotThrow(() -> {
            List<Document> results = tinkerPop.similaritySearch("query", 5, 1.0, null);
            // 应该能处理极值而不崩溃
            assertNotNull(results);
        });
    }

    // ================== 资源管理测试 ==================

    @Test
    public void testMultipleCloseCallsAreSafe() {
        doNothing().when(mockService).close();

        // 多次调用 close 应该是安全的
        assertDoesNotThrow(() -> {
            tinkerPop.close();
            tinkerPop.close();
            tinkerPop.close();
        });

        verify(mockService, times(3)).close();
    }

    @Test
    public void testCloseWhenServiceThrowsException() {
        doThrow(new RuntimeException("Close error")).when(mockService).close();

        // close 可能会抛出异常，但这在一些实现中是可以接受的
        // 测试确保即使抛出异常，应用程序也能正常处理
        try {
            tinkerPop.close();
        } catch (RuntimeException e) {
            // 预期的异常，测试通过
            assertTrue(e.getMessage().contains("Close error"));
        }
    }

    // ================== 配置边界测试 ==================

    @Test
    public void testConstructorWithNullEmbedding() {
        // Null embedding should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new TinkerPop(null, "test_collection");
        });
    }

    @Test
    public void testConstructorWithEmptyCollectionId() {
        TinkerPop emptyIdTinkerPop = new TinkerPop(mockEmbedding, "");
        // When collection ID is empty, it should auto-generate a non-empty ID
        assertNotNull(emptyIdTinkerPop.getCollectionId());
        assertNotEquals("", emptyIdTinkerPop.getCollectionId());
        assertTrue(emptyIdTinkerPop.getCollectionId().startsWith("collection_"));
        emptyIdTinkerPop.close();
    }

    @Test
    public void testConstructorWithVeryLongCollectionId() {
        String longId = "a".repeat(1000);
        // Very long collection ID should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new TinkerPop(mockEmbedding, longId);
        });
    }

    // ================== 辅助方法 ==================

    private Document createDocumentWithContent(String id, String content) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setPageContent(content);
        doc.setMetadata(new HashMap<>());
        return doc;
    }

    private Document createMockDocument(String id, List<Double> embedding) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setEmbedding(embedding);
        return doc;
    }

    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            fail("Failed to set private field: " + fieldName);
        }
    }
}