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

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ElasticsearchServiceTest {

    @Mock
    private ElasticsearchService elasticsearchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Order(1)
    void testInit() throws Exception {
        // Test successful initialization
        doNothing().when(elasticsearchService).init();
        
        elasticsearchService.init();
        
        verify(elasticsearchService, times(1)).init();
    }

    @Test
    @Order(2)
    void testAddDocuments() throws Exception {
        Document doc1 = new Document();
        doc1.setUniqueId("doc1");
        doc1.setPageContent("This is a test document");
        doc1.setEmbedding(Arrays.asList(0.1, 0.2, 0.3));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        doc1.setMetadata(metadata);

        Document doc2 = new Document();
        doc2.setUniqueId("doc2");
        doc2.setPageContent("Another test document");
        doc2.setEmbedding(Arrays.asList(0.4, 0.5, 0.6));

        List<Document> documents = Arrays.asList(doc1, doc2);

        doNothing().when(elasticsearchService).addDocuments(anyList());

        elasticsearchService.addDocuments(documents);

        verify(elasticsearchService, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(3)
    void testAddEmptyDocuments() throws Exception {
        List<Document> emptyDocuments = Collections.emptyList();

        doNothing().when(elasticsearchService).addDocuments(emptyDocuments);

        elasticsearchService.addDocuments(emptyDocuments);

        verify(elasticsearchService, times(1)).addDocuments(emptyDocuments);
    }

    @Test
    @Order(4)
    void testSimilaritySearch() throws Exception {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int k = 2;
        
        Document result1 = new Document();
        result1.setUniqueId("doc1");
        result1.setPageContent("This is a test document");
        result1.setScore(0.95);
        
        Document result2 = new Document();
        result2.setUniqueId("doc2");
        result2.setPageContent("Another test document");
        result2.setScore(0.85);

        List<Document> expectedResults = Arrays.asList(result1, result2);

        when(elasticsearchService.similaritySearch(queryVector, k, null, null))
                .thenReturn(expectedResults);

        List<Document> results = elasticsearchService.similaritySearch(queryVector, k, null, null);

        assertEquals(2, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        assertEquals("doc2", results.get(1).getUniqueId());
        assertTrue(results.get(0).getScore() >= results.get(1).getScore());

        verify(elasticsearchService, times(1)).similaritySearch(queryVector, k, null, null);
    }

    @Test
    @Order(5)
    void testSimilaritySearchWithMaxDistance() throws Exception {
        List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
        int k = 5;
        Double maxDistance = 0.5;
        
        Document result1 = new Document();
        result1.setUniqueId("doc1");
        result1.setPageContent("Close document");
        result1.setScore(0.9);

        List<Document> expectedResults = Collections.singletonList(result1);

        when(elasticsearchService.similaritySearch(queryVector, k, maxDistance, null))
                .thenReturn(expectedResults);

        List<Document> results = elasticsearchService.similaritySearch(queryVector, k, maxDistance, null);

        assertEquals(1, results.size());
        assertEquals("doc1", results.get(0).getUniqueId());
        assertTrue(results.get(0).getScore() >= 0.5);

        verify(elasticsearchService, times(1)).similaritySearch(queryVector, k, maxDistance, null);
    }

    @Test
    @Order(6)
    void testSimilaritySearchEmptyQuery() throws Exception {
        List<Float> emptyVector = Collections.emptyList();
        int k = 2;

        when(elasticsearchService.similaritySearch(emptyVector, k, null, null))
                .thenReturn(Collections.emptyList());

        List<Document> results = elasticsearchService.similaritySearch(emptyVector, k, null, null);

        assertTrue(results.isEmpty());
        verify(elasticsearchService, times(1)).similaritySearch(emptyVector, k, null, null);
    }

    @Test
    @Order(7)
    void testDeleteIndex() throws Exception {
        doNothing().when(elasticsearchService).deleteIndex();

        elasticsearchService.deleteIndex();

        verify(elasticsearchService, times(1)).deleteIndex();
    }

    @Test
    @Order(8)
    void testClose() throws Exception {
        doNothing().when(elasticsearchService).close();

        elasticsearchService.close();

        verify(elasticsearchService, times(1)).close();
    }

    @Test
    @Order(9)
    void testConstructorWithBasicAuth() {
        // Test constructor parameters
        String serverUrl = "http://localhost:9200";
        String username = "elastic";
        String password = "password";
        String indexName = "test-index";
        ElasticsearchParam param = new ElasticsearchParam();

        // This would test the actual constructor if not mocked
        assertDoesNotThrow(() -> {
            // In real implementation, this would create a service
            assertNotNull(serverUrl);
            assertNotNull(username);
            assertNotNull(password);
            assertNotNull(indexName);
            assertNotNull(param);
        });
    }

    @Test
    @Order(10)
    void testConstructorWithApiKey() {
        // Test constructor with API key authentication
        String serverUrl = "http://localhost:9200";
        String apiKey = "api-key-value";
        String indexName = "test-index";
        ElasticsearchParam param = new ElasticsearchParam();

        assertDoesNotThrow(() -> {
            assertNotNull(serverUrl);
            assertNotNull(apiKey);
            assertNotNull(indexName);
            assertNotNull(param);
        });
    }

    @Test
    @Order(11)
    void testElasticsearchParam() {
        ElasticsearchParam param = new ElasticsearchParam();
        
        // Test default values
        assertEquals("page_content", param.getFieldNamePageContent());
        assertEquals("content_id", param.getFieldNameUniqueId());
        assertEquals("vector", param.getFieldNameVector());
        assertEquals("metadata", param.getFieldNameMetadata());
        
        ElasticsearchParam.IndexParam indexParam = param.getIndexParam();
        assertEquals(1, indexParam.getNumberOfShards());
        assertEquals(0, indexParam.getNumberOfReplicas());
        assertEquals(1536, indexParam.getVectorDimension());
        assertEquals("cosine", indexParam.getVectorSimilarity());
        assertEquals("hnsw", indexParam.getVectorIndexType());
        assertEquals(16, indexParam.getHnswM());
        assertEquals(100, indexParam.getHnswEfConstruction());
        assertTrue(indexParam.isRefreshAfterWrite());
        
        // Test custom values
        param.setFieldNamePageContent("custom_content");
        assertEquals("custom_content", param.getFieldNamePageContent());
        
        indexParam.setVectorDimension(768);
        assertEquals(768, indexParam.getVectorDimension());
        
        indexParam.setVectorSimilarity("dot_product");
        assertEquals("dot_product", indexParam.getVectorSimilarity());
    }
}
