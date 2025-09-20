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
package com.alibaba.langengine.vald.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.vald.vectorstore.service.ValdInsertRequest;
import com.alibaba.langengine.vald.vectorstore.service.ValdSearchRequest;
import com.alibaba.langengine.vald.vectorstore.service.ValdSearchResponse;
import com.alibaba.langengine.vald.vectorstore.service.ValdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ValdStoreTest {

    @Mock
    private ValdService mockValdService;

    @Mock
    private Embeddings mockEmbeddings;

    private ValdStore valdStore;

    @BeforeEach
    public void setUp() {
        valdStore = new ValdStore(mockEmbeddings, "test-collection");
        valdStore.set_service(mockValdService);
    }

    @Test
    @DisplayName("测试添加单个文档 - 有向量")
    public void testAddDocuments_withEmbedding() {
        List<Double> embedding = Arrays.asList(0.1, 0.2, 0.3, 0.4);
        Document document = new Document();
        document.setUniqueId("doc1");
        document.setPageContent("测试文档内容");
        document.setEmbedding(embedding);
        document.setMetadata(Map.of("type", "test"));

        when(mockValdService.multiInsert(any())).thenReturn(null);

        valdStore.addDocuments(Arrays.asList(document));

        ArgumentCaptor<List<ValdInsertRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockValdService).multiInsert(captor.capture());

        List<ValdInsertRequest> requests = captor.getValue();
        assertEquals(1, requests.size());
        ValdInsertRequest request = requests.get(0);
        assertEquals("test-collection_doc1", request.getId());
        assertEquals(4, request.getVector().size());
        assertEquals(0.1, request.getVector().get(0), 0.0001);
        assertEquals(0.2, request.getVector().get(1), 0.0001);
        assertEquals(0.3, request.getVector().get(2), 0.0001);
        assertEquals(0.4, request.getVector().get(3), 0.0001);
    }

    @Test
    @DisplayName("测试添加单个文档 - 无向量需要生成")
    public void testAddDocuments_withoutEmbedding() {
        Document document = new Document();
        document.setUniqueId("doc2");
        document.setPageContent("需要生成向量的文档");
        document.setMetadata(Map.of("type", "generate"));

        Document embeddedDoc = new Document();
        embeddedDoc.setEmbedding(Arrays.asList(0.5, 0.6, 0.7, 0.8));

        when(mockEmbeddings.embedTexts(anyList())).thenReturn(Arrays.asList(embeddedDoc));
        when(mockValdService.multiInsert(any())).thenReturn(null);

        valdStore.addDocuments(Arrays.asList(document));

        verify(mockEmbeddings).embedTexts(Arrays.asList("需要生成向量的文档"));
        ArgumentCaptor<List<ValdInsertRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockValdService).multiInsert(captor.capture());

        List<ValdInsertRequest> requests = captor.getValue();
        assertEquals(1, requests.size());
        assertEquals("test-collection_doc2", requests.get(0).getId());
    }

    @Test
    @DisplayName("测试批量添加文档")
    public void testAddDocuments_multiple() {
        List<Document> documents = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc" + i);
            doc.setPageContent("文档内容 " + i);
            doc.setEmbedding(Arrays.asList(0.1 * i, 0.2 * i, 0.3 * i));
            doc.setMetadata(Map.of("index", i));
            documents.add(doc);
        }

        when(mockValdService.multiInsert(any())).thenReturn(null);

        valdStore.addDocuments(documents);

        ArgumentCaptor<List<ValdInsertRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockValdService).multiInsert(captor.capture());

        List<ValdInsertRequest> requests = captor.getValue();
        assertEquals(3, requests.size());
        for (int i = 0; i < 3; i++) {
            assertEquals("test-collection_doc" + (i + 1), requests.get(i).getId());
        }
    }

    @Test
    @DisplayName("测试添加文档 - 跳过空内容")
    public void testAddDocuments_skipEmptyContent() {
        Document validDoc = new Document();
        validDoc.setUniqueId("valid");
        validDoc.setPageContent("有效内容");
        validDoc.setEmbedding(Arrays.asList(0.1, 0.2));

        Document emptyDoc = new Document();
        emptyDoc.setUniqueId("empty");
        emptyDoc.setPageContent("");
        emptyDoc.setEmbedding(Arrays.asList(0.3, 0.4));

        Document nullDoc = new Document();
        nullDoc.setUniqueId("null");
        nullDoc.setPageContent(null);
        nullDoc.setEmbedding(Arrays.asList(0.5, 0.6));

        when(mockValdService.multiInsert(any())).thenReturn(null);

        valdStore.addDocuments(Arrays.asList(validDoc, emptyDoc, nullDoc));

        ArgumentCaptor<List<ValdInsertRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockValdService).multiInsert(captor.capture());

        List<ValdInsertRequest> requests = captor.getValue();
        assertEquals(1, requests.size());
        assertEquals("test-collection_valid", requests.get(0).getId());
    }

    @Test
    @DisplayName("测试相似性搜索")
    public void testSimilaritySearch() {
        String query = "搜索查询";
        List<Double> queryEmbedding = Arrays.asList(0.1, 0.2, 0.3);

        Document embeddedQuery = new Document();
        embeddedQuery.setEmbedding(queryEmbedding);

        List<ValdSearchResponse.ValdSearchResult> searchResults = Arrays.asList(
            new ValdSearchResponse.ValdSearchResult("test-collection_doc1", 0.1, null),
            new ValdSearchResponse.ValdSearchResult("test-collection_doc2", 0.3, null)
        );
        ValdSearchResponse searchResponse = new ValdSearchResponse(searchResults);

        when(mockEmbeddings.embedTexts(Arrays.asList(query))).thenReturn(Arrays.asList(embeddedQuery));
        when(mockValdService.search(any(ValdSearchRequest.class))).thenReturn(searchResponse);

        valdStore.get_documentContentMap().put("test-collection_doc1", "文档1内容");
        valdStore.get_documentContentMap().put("test-collection_doc2", "文档2内容");
        valdStore.get_documentMetadataMap().put("test-collection_doc1", Map.of("type", "doc1"));
        valdStore.get_documentMetadataMap().put("test-collection_doc2", Map.of("type", "doc2"));

        List<Document> results = valdStore.similaritySearch(query, 2, 0.5, null);

        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        assertEquals("文档1内容", results.get(0).getPageContent());
        assertEquals(0.1, results.get(0).getScore());
        assertEquals("doc1", results.get(0).getMetadata().get("type"));

        ArgumentCaptor<ValdSearchRequest> captor = ArgumentCaptor.forClass(ValdSearchRequest.class);
        verify(mockValdService).search(captor.capture());
        ValdSearchRequest searchRequest = captor.getValue();
        assertEquals(queryEmbedding, searchRequest.getVector());
        assertEquals(2, searchRequest.getK());
        assertEquals(0.5, searchRequest.getMaxDistance());
    }

    @Test
    @DisplayName("测试相似性搜索 - 距离过滤")
    public void testSimilaritySearch_distanceFilter() {
        String query = "搜索查询";
        List<Double> queryEmbedding = Arrays.asList(0.1, 0.2, 0.3);

        Document embeddedQuery = new Document();
        embeddedQuery.setEmbedding(queryEmbedding);

        List<ValdSearchResponse.ValdSearchResult> searchResults = Arrays.asList(
            new ValdSearchResponse.ValdSearchResult("test-collection_doc1", 0.1, null),
            new ValdSearchResponse.ValdSearchResult("test-collection_doc2", 0.8, null),
            new ValdSearchResponse.ValdSearchResult("test-collection_doc3", 0.3, null)
        );
        ValdSearchResponse searchResponse = new ValdSearchResponse(searchResults);

        when(mockEmbeddings.embedTexts(Arrays.asList(query))).thenReturn(Arrays.asList(embeddedQuery));
        when(mockValdService.search(any(ValdSearchRequest.class))).thenReturn(searchResponse);

        valdStore.get_documentContentMap().put("test-collection_doc1", "文档1内容");
        valdStore.get_documentContentMap().put("test-collection_doc2", "文档2内容");
        valdStore.get_documentContentMap().put("test-collection_doc3", "文档3内容");

        List<Document> results = valdStore.similaritySearch(query, 3, 0.5, null);

        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        assertEquals("doc3", results.get(1).getUniqueId());
    }

    @Test
    @DisplayName("测试相似性搜索 - 查询向量生成失败")
    public void testSimilaritySearch_embedFailure() {
        String query = "搜索查询";

        when(mockEmbeddings.embedTexts(Arrays.asList(query))).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> valdStore.similaritySearch(query, 2, null, null));

        assertTrue(exception.getMessage().contains("Failed to generate embedding for query"));
    }

    @Test
    @DisplayName("测试添加文本")
    public void testAddTexts() {
        List<String> texts = Arrays.asList("文本1", "文本2");
        List<String> ids = Arrays.asList("id1", "id2");
        List<Map<String, String>> metadatas = Arrays.asList(
            Map.of("type", "text1"),
            Map.of("type", "text2")
        );

        List<Document> embeddings = Arrays.asList(
            createDocumentWithEmbedding(Arrays.asList(0.1, 0.2)),
            createDocumentWithEmbedding(Arrays.asList(0.3, 0.4))
        );

        when(mockEmbeddings.embedTexts(texts)).thenReturn(embeddings);
        when(mockValdService.multiInsert(any())).thenReturn(null);

        List<String> resultIds = valdStore.addTexts(texts, metadatas, ids);

        assertEquals(Arrays.asList("id1", "id2"), resultIds);

        ArgumentCaptor<List<ValdInsertRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockValdService).multiInsert(captor.capture());

        List<ValdInsertRequest> requests = captor.getValue();
        assertEquals(2, requests.size());
        assertEquals("test-collection_id1", requests.get(0).getId());
        assertEquals("test-collection_id2", requests.get(1).getId());
    }

    @Test
    @DisplayName("测试添加文本 - 自动生成ID")
    public void testAddTexts_autoGenerateIds() {
        List<String> texts = Arrays.asList("文本1");

        List<Document> embeddings = Arrays.asList(
            createDocumentWithEmbedding(Arrays.asList(0.1, 0.2))
        );

        when(mockEmbeddings.embedTexts(texts)).thenReturn(embeddings);
        when(mockValdService.multiInsert(any())).thenReturn(null);

        List<String> resultIds = valdStore.addTexts(texts, null, null);

        assertEquals(1, resultIds.size());
        assertNotNull(resultIds.get(0));
        assertFalse(resultIds.get(0).isEmpty());
    }

    @Test
    @DisplayName("测试ValdService异常处理")
    public void testValdServiceException() {
        Document document = new Document();
        document.setUniqueId("doc1");
        document.setPageContent("测试文档");
        document.setEmbedding(Arrays.asList(0.1, 0.2));

        when(mockValdService.multiInsert(any())).thenThrow(new RuntimeException("Vald服务异常"));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> valdStore.addDocuments(Arrays.asList(document)));

        assertTrue(exception.getMessage().contains("Failed to insert vectors to Vald") ||
                  exception.getMessage().contains("Vald服务异常") ||
                  (exception.getCause() != null &&
                   (exception.getCause().getMessage().contains("Vald服务异常") ||
                    exception.getCause().getMessage().contains("Failed to insert vectors to Vald"))));
    }

    @Test
    @DisplayName("测试关闭资源")
    public void testClose() {
        valdStore.close();
        verify(mockValdService).close();
    }

    @Test
    @DisplayName("测试向量ID生成和提取")
    public void testVectorIdGeneration() {
        Document document = new Document();
        document.setUniqueId("original-id");
        document.setPageContent("测试内容");
        document.setEmbedding(Arrays.asList(0.1, 0.2));

        when(mockValdService.multiInsert(any())).thenReturn(null);

        valdStore.addDocuments(Arrays.asList(document));

        ArgumentCaptor<List<ValdInsertRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockValdService).multiInsert(captor.capture());

        String generatedId = captor.getValue().get(0).getId();
        assertEquals("test-collection_original-id", generatedId);
    }

    @Test
    @DisplayName("测试HTML内容过滤")
    public void testContentFiltering() {
        String htmlContent = "<div>测试<span>内容</span>&amp;符号</div>";
        String query = "搜索";

        Document embeddedQuery = new Document();
        embeddedQuery.setEmbedding(Arrays.asList(0.1, 0.2));

        List<ValdSearchResponse.ValdSearchResult> searchResults = Arrays.asList(
            new ValdSearchResponse.ValdSearchResult("test-collection_doc1", 0.1, null)
        );
        ValdSearchResponse searchResponse = new ValdSearchResponse(searchResults);

        when(mockEmbeddings.embedTexts(Arrays.asList(query))).thenReturn(Arrays.asList(embeddedQuery));
        when(mockValdService.search(any())).thenReturn(searchResponse);

        valdStore.get_documentContentMap().put("test-collection_doc1", htmlContent);

        List<Document> results = valdStore.similaritySearch(query, 1, null, null);

        assertEquals(1, results.size());
        String filteredContent = results.get(0).getPageContent();
        assertFalse(filteredContent.contains("<div>"));
        assertFalse(filteredContent.contains("</span>"));
        assertTrue(filteredContent.contains("测试内容&符号"));
    }

    private Document createDocumentWithEmbedding(List<Double> embedding) {
        Document doc = new Document();
        doc.setEmbedding(embedding);
        return doc;
    }
}