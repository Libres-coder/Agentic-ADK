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
package com.alibaba.langengine.elasticsearch.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ElasticsearchTest {

    @Mock
    private Embeddings embeddings;

    @Mock
    private ElasticsearchService elasticsearchService;

    @Mock
    private Elasticsearch elasticsearch;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create mocked Elasticsearch instance for testing
        elasticsearch = mock(Elasticsearch.class);
        
        // Set up default mock behaviors
        when(elasticsearch.getEmbedding()).thenReturn(embeddings);
    }

    @Test
    @Order(1)
    void testConstructors() {
        // Test default constructor
        Elasticsearch es1 = new Elasticsearch("test-index");
        assertNotNull(es1);
        assertEquals("test-index", es1.getIndexName());

        // Test constructor with parameters
        ElasticsearchParam param = new ElasticsearchParam();
        Elasticsearch es2 = new Elasticsearch("test-index", param);
        assertNotNull(es2);
        assertEquals("test-index", es2.getIndexName());

        // Test constructor with server URL
        Elasticsearch es3 = new Elasticsearch("http://localhost:9200", "test-index", param);
        assertNotNull(es3);
        assertEquals("test-index", es3.getIndexName());

        // Test constructor with authentication
        Elasticsearch es4 = new Elasticsearch("http://localhost:9200", "user", "pass", 
                                            "api-key", "test-index", param);
        assertNotNull(es4);
        assertEquals("test-index", es4.getIndexName());
    }

    @Test
    @Order(2)
    void testInit() throws Exception {
        doNothing().when(elasticsearchService).init();

        assertDoesNotThrow(() -> elasticsearch.init());

        verify(elasticsearchService, times(1)).init();
    }

    @Test
    @Order(3)
    void testInitFailure() throws Exception {
        doThrow(new RuntimeException("Init failed")).when(elasticsearchService).init();

        assertThrows(RuntimeException.class, () -> elasticsearch.init());

        verify(elasticsearchService, times(1)).init();
    }

    @Test
    @Order(4)
    void testAddDocuments() throws Exception {
        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("Test document 1");

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("Test document 2");

        List<Document> documents = Arrays.asList(doc1, doc2);

        // Mock embedding
        List<Document> embeddedDocs = Arrays.asList(doc1, doc2);
        embeddedDocs.get(0).setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        embeddedDocs.get(1).setEmbedding(Arrays.asList(0.4, 0.5, 0.6));

        when(embeddings.embedDocument(documents)).thenReturn(embeddedDocs);
        doNothing().when(elasticsearchService).addDocuments(embeddedDocs);

        elasticsearch.addDocuments(documents);

        verify(embeddings, times(1)).embedDocument(documents);
        verify(elasticsearchService, times(1)).addDocuments(embeddedDocs);
    }

    @Test
    @Order(5)
    void testAddEmptyDocuments() throws IOException {
        elasticsearch.addDocuments(Collections.emptyList());
        elasticsearch.addDocuments(null);

        // Should not call any service methods for empty/null documents
        verify(elasticsearchService, never()).addDocuments(any());
        verify(embeddings, never()).embedDocument(any());
    }

    @Test
    @Order(6)
    void testAddDocumentsFailure() throws Exception {
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Test document");

        List<Document> documents = Collections.singletonList(doc);

        when(embeddings.embedDocument(documents)).thenReturn(documents);
        doThrow(new RuntimeException("Add failed")).when(elasticsearchService).addDocuments(documents);

        assertThrows(RuntimeException.class, () -> elasticsearch.addDocuments(documents));

        verify(embeddings, times(1)).embedDocument(documents);
        verify(elasticsearchService, times(1)).addDocuments(documents);
    }

    @Test
    @Order(7)
    void testSimilaritySearch() throws Exception {
        String query = "test query";
        int k = 2;
        
        // Mock embedding response
        List<String> embeddingStrings = Collections.singletonList("[0.1, 0.2, 0.3]");
        when(embeddings.embedQuery(query, k)).thenReturn(embeddingStrings);

        // Mock search results
        Document result1 = new Document();
        result1.setUniqueId("doc1");
        result1.setPageContent("Similar document 1");
        result1.setScore(0.95);

        Document result2 = new Document();
        result2.setUniqueId("doc2");
        result2.setPageContent("Similar document 2");
        result2.setScore(0.85);

        List<Document> expectedResults = Arrays.asList(result1, result2);
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);

        when(elasticsearchService.similaritySearch(queryVector, k, null, null))
                .thenReturn(expectedResults);

        List<Document> results = elasticsearch.similaritySearch(query, k, null, null);

        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        assertEquals("doc2", results.get(1).getUniqueId());
        assertTrue(results.get(0).getScore() >= results.get(1).getScore());

        verify(embeddings, times(1)).embedQuery(query, k);
        verify(elasticsearchService, times(1)).similaritySearch(queryVector, k, null, null);
    }

    @Test
    @Order(8)
    void testSimilaritySearchWithMaxDistance() throws Exception {
        String query = "test query";
        int k = 5;
        Double maxDistance = 0.3;
        
        List<String> embeddingStrings = Collections.singletonList("[0.1, 0.2, 0.3]");
        when(embeddings.embedQuery(query, k)).thenReturn(embeddingStrings);

        Document result = new Document();
        result.setUniqueId("doc1");
        result.setPageContent("Close document");
        result.setScore(0.8);

        List<Document> expectedResults = Collections.singletonList(result);
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);

        when(elasticsearchService.similaritySearch(queryVector, k, maxDistance, null))
                .thenReturn(expectedResults);

        List<Document> results = elasticsearch.similaritySearch(query, k, maxDistance, null);

        assertEquals(1, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());

        verify(embeddings, times(1)).embedQuery(query, k);
        verify(elasticsearchService, times(1)).similaritySearch(queryVector, k, maxDistance, null);
    }

    @Test
    @Order(9)
    void testSimilaritySearchEmptyQuery() throws IOException {
        List<Document> results1 = elasticsearch.similaritySearch("", 2, null, null);
        List<Document> results2 = elasticsearch.similaritySearch(null, 2, null, null);
        List<Document> results3 = elasticsearch.similaritySearch("   ", 2, null, null);

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
        assertTrue(results3.isEmpty());

        verify(embeddings, never()).embedQuery(any(), anyInt());
        verify(elasticsearchService, never()).similaritySearch(any(), anyInt(), any(), any());
    }

    @Test
    @Order(10)
    void testSimilaritySearchInvalidEmbedding() throws IOException {
        String query = "test query";
        int k = 2;
        
        // Mock invalid embedding response
        List<String> invalidEmbeddings = Arrays.asList("invalid", "", null);
        
        for (String invalidEmbedding : invalidEmbeddings) {
            when(embeddings.embedQuery(query, k))
                    .thenReturn(invalidEmbedding != null ? Collections.singletonList(invalidEmbedding) : Collections.emptyList());

            List<Document> results = elasticsearch.similaritySearch(query, k, null, null);
            assertTrue(results.isEmpty());
        }

        verify(elasticsearchService, never()).similaritySearch(any(), anyInt(), any(), any());
    }

    @Test
    @Order(11)
    void testSimilaritySearchByVector() throws Exception {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int k = 2;

        Document result = new Document();
        result.setUniqueId("doc1");
        result.setPageContent("Vector search result");
        result.setScore(0.9);

        List<Document> expectedResults = Collections.singletonList(result);

        when(elasticsearchService.similaritySearch(queryVector, k, null, null))
                .thenReturn(expectedResults);

        List<Document> results = elasticsearch.similaritySearchByVector(queryVector, k, null, null);

        assertEquals(1, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());

        verify(elasticsearchService, times(1)).similaritySearch(queryVector, k, null, null);
    }

    @Test
    @Order(12)
    void testSimilaritySearchByVectorFailure() throws Exception {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int k = 2;

        when(elasticsearchService.similaritySearch(queryVector, k, null, null))
                .thenThrow(new RuntimeException("Search failed"));

        assertThrows(RuntimeException.class, 
                () -> elasticsearch.similaritySearchByVector(queryVector, k, null, null));

        verify(elasticsearchService, times(1)).similaritySearch(queryVector, k, null, null);
    }

    @Test
    @Order(13)
    void testDeleteIndex() throws Exception {
        doNothing().when(elasticsearchService).deleteIndex();

        assertDoesNotThrow(() -> elasticsearch.deleteIndex());

        verify(elasticsearchService, times(1)).deleteIndex();
    }

    @Test
    @Order(14)
    void testDeleteIndexFailure() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(elasticsearchService).deleteIndex();

        assertThrows(RuntimeException.class, () -> elasticsearch.deleteIndex());

        verify(elasticsearchService, times(1)).deleteIndex();
    }

    @Test
    @Order(15)
    void testClose() throws Exception {
        doNothing().when(elasticsearchService).close();

        assertDoesNotThrow(() -> elasticsearch.close());

        verify(elasticsearchService, times(1)).close();
    }

    @Test
    @Order(16)
    void testCloseWithException() throws Exception {
        doThrow(new RuntimeException("Close failed")).when(elasticsearchService).close();

        // Close should not throw exception even if service close fails
        assertDoesNotThrow(() -> elasticsearch.close());

        verify(elasticsearchService, times(1)).close();
    }

    @Test
    @Order(17)
    void testGettersAndSetters() {
        assertEquals("test-index", elasticsearch.getIndexName());
        
        Embeddings newEmbeddings = mock(Embeddings.class);
        elasticsearch.setEmbedding(newEmbeddings);
        assertEquals(newEmbeddings, elasticsearch.getEmbedding());
        
        assertNotNull(elasticsearch.getElasticsearchService());
    }
}
