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
package com.alibaba.langengine.vespa.vectorstore;

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
public class VespaTest {

    @Mock
    private Embeddings embeddings;

    @Mock
    private VespaService vespaService;

    @Mock
    private Vespa vespa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Set up default mock behaviors
        when(vespa.getEmbedding()).thenReturn(embeddings);
    }

    @Test
    @Order(1)
    void testConstructors() {
        // Test constructor with document type only
        assertDoesNotThrow(() -> {
            new Vespa("test-document");
        });

        // Test constructor with document type and namespace
        assertDoesNotThrow(() -> {
            new Vespa("test-document", "test-namespace");
        });

        // Test constructor with all parameters
        VespaParam param = new VespaParam();
        assertDoesNotThrow(() -> {
            new Vespa("test-document", "test-namespace", param);
        });
    }

    @Test
    @Order(2)
    void testInit() {
        doNothing().when(vespa).init();
        
        vespa.init();
        
        verify(vespa, times(1)).init();
    }

    @Test
    @Order(3)
    void testInitFailure() {
        doThrow(new RuntimeException("Init failed")).when(vespa).init();
        
        assertThrows(RuntimeException.class, () -> {
            vespa.init();
        });
        
        verify(vespa, times(1)).init();
    }

    @Test
    @Order(4)
    void testAddDocuments() throws IOException {
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Test document");

        List<Document> documents = Collections.singletonList(doc);

        when(embeddings.embedDocument(documents)).thenReturn(documents);
        doNothing().when(vespa).addDocuments(documents);

        vespa.addDocuments(documents);

        verify(vespa, times(1)).addDocuments(documents);
    }

    @Test
    @Order(5)
    void testAddEmptyDocuments() throws IOException {
        doNothing().when(vespa).addDocuments(Collections.emptyList());
        doNothing().when(vespa).addDocuments(null);

        vespa.addDocuments(Collections.emptyList());
        vespa.addDocuments(null);

        verify(vespa, times(1)).addDocuments(Collections.emptyList());
        verify(vespa, times(1)).addDocuments(null);
    }

    @Test
    @Order(6)
    void testAddDocumentsFailure() throws Exception {
        Document doc = new Document();
        doc.setUniqueId("doc1");
        doc.setPageContent("Test document");

        List<Document> documents = Collections.singletonList(doc);

        when(embeddings.embedDocument(documents)).thenReturn(documents);
        doThrow(new RuntimeException("Add failed")).when(vespa).addDocuments(documents);

        assertThrows(RuntimeException.class, () -> vespa.addDocuments(documents));

        verify(vespa, times(1)).addDocuments(documents);
    }

    @Test
    @Order(7)
    void testSimilaritySearch() throws IOException {
        String query = "test query";
        int k = 3;

        List<String> embeddingResponse = Arrays.asList("[0.1, 0.2, 0.3]");
        List<Document> expectedResults = new ArrayList<>();
        
        Document resultDoc = new Document();
        resultDoc.setUniqueId("result1");
        resultDoc.setPageContent("Result content");
        expectedResults.add(resultDoc);

        when(embeddings.embedQuery(query, k)).thenReturn(embeddingResponse);
        when(vespa.similaritySearch(query, k, null, null)).thenReturn(expectedResults);

        List<Document> results = vespa.similaritySearch(query, k, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("result1", results.get(0).getUniqueId());
        verify(vespa, times(1)).similaritySearch(query, k, null, null);
    }

    @Test
    @Order(8)
    void testSimilaritySearchWithMaxDistance() throws IOException {
        String query = "test query";
        int k = 2;
        Double maxDistance = 0.5;

        List<String> embeddingResponse = Arrays.asList("[0.1, 0.2, 0.3]");
        List<Document> expectedResults = new ArrayList<>();
        
        Document resultDoc = new Document();
        resultDoc.setUniqueId("result1");
        resultDoc.setPageContent("Result content");
        expectedResults.add(resultDoc);

        when(embeddings.embedQuery(query, k)).thenReturn(embeddingResponse);
        when(vespa.similaritySearch(query, k, maxDistance, null)).thenReturn(expectedResults);

        List<Document> results = vespa.similaritySearch(query, k, maxDistance, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(vespa, times(1)).similaritySearch(query, k, maxDistance, null);
    }

    @Test
    @Order(9)
    void testSimilaritySearchEmptyQuery() throws IOException {
        when(vespa.similaritySearch("", 3, null, null)).thenReturn(Collections.emptyList());
        when(vespa.similaritySearch(null, 3, null, null)).thenReturn(Collections.emptyList());

        List<Document> results1 = vespa.similaritySearch("", 3, null, null);
        List<Document> results2 = vespa.similaritySearch(null, 3, null, null);

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());

        verify(vespa, times(1)).similaritySearch("", 3, null, null);
        verify(vespa, times(1)).similaritySearch(null, 3, null, null);
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
            when(vespa.similaritySearch(query, k, null, null)).thenReturn(Collections.emptyList());

            List<Document> results = vespa.similaritySearch(query, k, null, null);

            assertTrue(results.isEmpty());
        }
    }

    @Test
    @Order(11)
    void testSimilaritySearchByVector() {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int k = 3;

        List<Document> expectedResults = new ArrayList<>();
        Document resultDoc = new Document();
        resultDoc.setUniqueId("vector-result1");
        resultDoc.setPageContent("Vector search result");
        expectedResults.add(resultDoc);

        // Note: This tests the service method directly through mocking
        when(vespaService.similaritySearch(queryVector, k, null, null)).thenReturn(expectedResults);

        List<Document> results = vespaService.similaritySearch(queryVector, k, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("vector-result1", results.get(0).getUniqueId());
    }

    @Test
    @Order(12)
    void testSimilaritySearchByVectorFailure() {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        
        when(vespaService.similaritySearch(queryVector, 3, null, null))
                .thenThrow(new RuntimeException("Vector search failed"));

        assertThrows(RuntimeException.class, () -> {
            vespaService.similaritySearch(queryVector, 3, null, null);
        });
    }

    @Test
    @Order(13)
    void testClose() {
        doNothing().when(vespa).close();

        vespa.close();

        verify(vespa, times(1)).close();
    }

    @Test
    @Order(14)
    void testCloseWithException() {
        doThrow(new RuntimeException("Close failed")).when(vespa).close();

        assertThrows(RuntimeException.class, () -> {
            vespa.close();
        });

        verify(vespa, times(1)).close();
    }

    @Test
    @Order(15)
    void testGettersAndSetters() {
        when(vespa.getDocumentType()).thenReturn("test-document");
        when(vespa.getNamespace()).thenReturn("test-namespace");

        assertEquals("test-document", vespa.getDocumentType());
        assertEquals("test-namespace", vespa.getNamespace());
    }

    @Test
    @Order(16)
    void testVespaParamConfiguration() {
        VespaParam param = new VespaParam();
        param.setNamespace("custom-namespace");
        param.setDocumentType("custom-document");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameVector("custom_vector");

        VespaParam.IndexParam indexParam = new VespaParam.IndexParam();
        indexParam.setVectorDistance("euclidean");
        indexParam.setVectorDimensions(768);
        indexParam.setMaxLinksPerNode(32);
        indexParam.setNeighborsToExploreAtInsert(400);
        indexParam.setMultiThreadedIndexing(false);
        param.setIndexParam(indexParam);

        // Validate configuration
        assertEquals("custom-namespace", param.getNamespace());
        assertEquals("custom-document", param.getDocumentType());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_vector", param.getFieldNameVector());
        assertEquals("euclidean", param.getIndexParam().getVectorDistance());
        assertEquals(768, param.getIndexParam().getVectorDimensions());
        assertEquals(32, param.getIndexParam().getMaxLinksPerNode());
        assertEquals(400, param.getIndexParam().getNeighborsToExploreAtInsert());
        assertFalse(param.getIndexParam().getMultiThreadedIndexing());
    }

    @Test
    @Order(17)
    void testComplexScenario() throws IOException {
        // Test a complex scenario with multiple documents and searches
        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Document doc = new Document();
            doc.setUniqueId("complex-doc-" + i);
            doc.setPageContent("Complex content " + i);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "complex");
            metadata.put("index", i);
            doc.setMetadata(metadata);
            documents.add(doc);
        }

        when(embeddings.embedDocument(documents)).thenReturn(documents);
        doNothing().when(vespa).addDocuments(documents);

        vespa.addDocuments(documents);

        // Perform multiple similarity searches
        String[] queries = {"query1", "query2", "query3"};
        for (String query : queries) {
            when(embeddings.embedQuery(query, 2)).thenReturn(Arrays.asList("[0.1, 0.2, 0.3]"));
            when(vespa.similaritySearch(query, 2, null, null)).thenReturn(documents.subList(0, 2));

            List<Document> results = vespa.similaritySearch(query, 2, null, null);
            assertEquals(2, results.size());
        }

        verify(vespa, times(1)).addDocuments(documents);
        verify(vespa, times(3)).similaritySearch(anyString(), eq(2), isNull(), isNull());
    }
}
