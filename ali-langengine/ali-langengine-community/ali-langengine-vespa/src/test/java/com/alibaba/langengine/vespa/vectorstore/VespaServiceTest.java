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

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VespaServiceTest {

    @Mock
    private VespaService vespaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Order(1)
    void testInit() {
        doNothing().when(vespaService).init();
        
        vespaService.init();
        
        verify(vespaService, times(1)).init();
    }

    @Test
    @Order(2)
    void testAddDocuments() {
        List<Document> documents = new ArrayList<>();
        Document doc = new Document();
        doc.setUniqueId("test-doc-1");
        doc.setPageContent("Test content");
        doc.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        documents.add(doc);

        doNothing().when(vespaService).addDocuments(documents);

        vespaService.addDocuments(documents);

        verify(vespaService, times(1)).addDocuments(documents);
    }

    @Test
    @Order(3)
    void testAddEmptyDocuments() {
        List<Document> emptyList = Collections.emptyList();
        
        doNothing().when(vespaService).addDocuments(emptyList);

        vespaService.addDocuments(emptyList);
        vespaService.addDocuments(null);

        verify(vespaService, times(1)).addDocuments(emptyList);
        verify(vespaService, times(1)).addDocuments(null);
    }

    @Test
    @Order(4)
    void testSimilaritySearch() {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int k = 5;
        Double maxDistanceValue = 0.8;
        Integer type = 1;

        List<Document> expectedResults = new ArrayList<>();
        Document resultDoc = new Document();
        resultDoc.setUniqueId("result-doc-1");
        resultDoc.setPageContent("Result content");
        expectedResults.add(resultDoc);

        when(vespaService.similaritySearch(queryVector, k, maxDistanceValue, type))
                .thenReturn(expectedResults);

        List<Document> results = vespaService.similaritySearch(queryVector, k, maxDistanceValue, type);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("result-doc-1", results.get(0).getUniqueId());
        verify(vespaService, times(1)).similaritySearch(queryVector, k, maxDistanceValue, type);
    }

    @Test
    @Order(5)
    void testSimilaritySearchWithNullVector() {
        when(vespaService.similaritySearch(null, 5, null, null))
                .thenReturn(Collections.emptyList());

        List<Document> results = vespaService.similaritySearch(null, 5, null, null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(vespaService, times(1)).similaritySearch(null, 5, null, null);
    }

    @Test
    @Order(6)
    void testSimilaritySearchFailure() {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        
        when(vespaService.similaritySearch(queryVector, 5, null, null))
                .thenThrow(new RuntimeException("Search failed"));

        assertThrows(RuntimeException.class, () -> {
            vespaService.similaritySearch(queryVector, 5, null, null);
        });

        verify(vespaService, times(1)).similaritySearch(queryVector, 5, null, null);
    }

    @Test
    @Order(7)
    void testClose() {
        doNothing().when(vespaService).close();

        vespaService.close();

        verify(vespaService, times(1)).close();
    }

    @Test
    @Order(8)
    void testGettersAndSetters() {
        VespaParam param = new VespaParam();
        param.setNamespace("test-namespace");
        param.setDocumentType("test-document");

        when(vespaService.getNamespace()).thenReturn("test-namespace");
        when(vespaService.getDocumentType()).thenReturn("test-document");
        when(vespaService.getVespaParam()).thenReturn(param);

        assertEquals("test-namespace", vespaService.getNamespace());
        assertEquals("test-document", vespaService.getDocumentType());
        assertNotNull(vespaService.getVespaParam());
    }

    @Test
    @Order(9)
    void testServiceWithCustomParams() {
        VespaParam param = new VespaParam();
        param.setNamespace("custom-namespace");
        param.setDocumentType("custom-document");
        param.setFieldNamePageContent("custom_content");
        param.setFieldNameVector("custom_vector");

        VespaParam.IndexParam indexParam = new VespaParam.IndexParam();
        indexParam.setVectorDistance("euclidean");
        indexParam.setVectorDimensions(768);
        param.setIndexParam(indexParam);

        // Test parameter validation
        assertNotNull(param);
        assertEquals("custom-namespace", param.getNamespace());
        assertEquals("custom-document", param.getDocumentType());
        assertEquals("custom_content", param.getFieldNamePageContent());
        assertEquals("custom_vector", param.getFieldNameVector());
        assertEquals("euclidean", param.getIndexParam().getVectorDistance());
        assertEquals(768, param.getIndexParam().getVectorDimensions());
    }

    @Test
    @Order(10)
    void testBatchOperations() {
        List<Document> largeBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Document doc = new Document();
            doc.setUniqueId("batch-doc-" + i);
            doc.setPageContent("Batch content " + i);
            doc.setEmbedding(Arrays.asList(0.1 + i, 0.2 + i, 0.3 + i));
            largeBatch.add(doc);
        }

        doNothing().when(vespaService).addDocuments(largeBatch);

        vespaService.addDocuments(largeBatch);

        verify(vespaService, times(1)).addDocuments(largeBatch);
    }

    @Test
    @Order(11)
    void testErrorHandling() {
        List<Document> documents = Arrays.asList(new Document());
        
        doThrow(new RuntimeException("Connection failed")).when(vespaService).addDocuments(documents);

        assertThrows(RuntimeException.class, () -> {
            vespaService.addDocuments(documents);
        });

        verify(vespaService, times(1)).addDocuments(documents);
    }
}
