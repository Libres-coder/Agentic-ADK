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
package com.alibaba.langengine.cassandra.vectorstore;

import com.alibaba.langengine.cassandra.utils.Constants;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@EnabledIfSystemProperty(named = "test.cassandra.enabled", matches = "true")
public class CassandraServiceTest {

    private CassandraService cassandraService;
    private CassandraParam cassandraParam;

    @BeforeEach
    public void setUp() {
        cassandraParam = createTestParameters();
        
        try {
            cassandraService = new CassandraService(
                    "test_keyspace",
                    "test_table",
                    Arrays.asList("127.0.0.1:9042"),
                    "datacenter1",
                    null, // username
                    null, // password
                    cassandraParam
            );
            
            cassandraService.init();
        } catch (Exception e) {
            // Skip tests if Cassandra is not available
            org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                    "Skipping CassandraService tests - Cassandra not available: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (cassandraService != null) {
            cassandraService.close();
        }
    }

    private CassandraParam createTestParameters() {
        CassandraParam param = new CassandraParam();
        
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        initParam.setTableName("test_table");
        initParam.setVectorDimensions(3);
        initParam.setVectorSimilarityFunction(Constants.SIMILARITY_FUNCTION_COSINE);
        initParam.setReplicationFactor(1);
        
        param.setInitParam(initParam);
        param.setFieldNameUniqueId("id");
        param.setFieldNamePageContent("content");
        param.setFieldNameVector("vector");
        param.setFieldMeta("metadata");
        
        return param;
    }

    @Test
    public void testServiceInitialization() {
        assertNotNull(cassandraService);
        assertEquals("test_keyspace", cassandraService.getKeyspace());
        assertEquals("test_table", cassandraService.getTableName());
        assertNotNull(cassandraService.getContactPoints());
        assertEquals("datacenter1", cassandraService.getLocalDatacenter());
    }

    @Test
    public void testAddSingleDocument() {
        Document document = createTestDocument("Test content", Arrays.asList(0.1, 0.2, 0.3));
        
        assertDoesNotThrow(() -> {
            cassandraService.addDocuments(Arrays.asList(document));
        });
    }

    @Test
    public void testAddMultipleDocuments() {
        List<Document> documents = Arrays.asList(
                createTestDocument("Document 1", Arrays.asList(0.1, 0.2, 0.3)),
                createTestDocument("Document 2", Arrays.asList(0.4, 0.5, 0.6)),
                createTestDocument("Document 3", Arrays.asList(0.7, 0.8, 0.9))
        );
        
        assertDoesNotThrow(() -> {
            cassandraService.addDocuments(documents);
        });
    }

    @Test
    public void testAddDocumentWithoutEmbedding() {
        Document document = new Document();
        document.setPageContent("Document without embedding");
        document.setUniqueId(UUID.randomUUID().toString());
        
        assertDoesNotThrow(() -> {
            cassandraService.addDocuments(Arrays.asList(document));
        });
    }

    @Test
    public void testAddDocumentWithMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "test_user");
        metadata.put("category", "test");
        
        Document document = createTestDocumentWithMetadata("Document with metadata", 
                Arrays.asList(0.1, 0.2, 0.3), metadata);
        
        assertDoesNotThrow(() -> {
            cassandraService.addDocuments(Arrays.asList(document));
        });
    }

    @Test
    public void testSimilaritySearch() {
        // Add test documents
        List<Document> documents = Arrays.asList(
                createTestDocument("Apple", Arrays.asList(0.1, 0.9, 0.1)),
                createTestDocument("Orange", Arrays.asList(0.2, 0.8, 0.2)),
                createTestDocument("Car", Arrays.asList(0.9, 0.1, 0.8))
        );
        
        cassandraService.addDocuments(documents);
        
        // Search for fruit-like vectors
        List<Float> queryVector = Arrays.asList(0.15f, 0.85f, 0.15f);
        List<Document> results = cassandraService.similaritySearch(queryVector, 2, null, null);
        
        assertNotNull(results);
        assertTrue(results.size() <= 2);
    }

    @Test
    public void testSimilaritySearchWithMaxDistance() {
        Document document = createTestDocument("Test document", Arrays.asList(0.5, 0.5, 0.5));
        cassandraService.addDocuments(Arrays.asList(document));
        
        List<Float> queryVector = Arrays.asList(0.5f, 0.5f, 0.5f);
        List<Document> results = cassandraService.similaritySearch(queryVector, 10, 0.1, null);
        
        assertNotNull(results);
    }

    @Test
    public void testSimilaritySearchWithEmptyResult() {
        List<Float> queryVector = Arrays.asList(1.0f, 1.0f, 1.0f);
        List<Document> results = cassandraService.similaritySearch(queryVector, 10, null, null);
        
        assertNotNull(results);
        // Results might be empty if no documents are added or no matches found
    }

    @Test
    public void testAddNullDocuments() {
        assertDoesNotThrow(() -> {
            cassandraService.addDocuments(null);
        });
        
        assertDoesNotThrow(() -> {
            cassandraService.addDocuments(new ArrayList<>());
        });
    }

    @Test
    public void testSearchWithNullVector() {
        List<Document> results = cassandraService.similaritySearch(null, 10, null, null);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testSearchWithEmptyVector() {
        List<Document> results = cassandraService.similaritySearch(new ArrayList<>(), 10, null, null);
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testServiceClose() {
        assertDoesNotThrow(() -> {
            cassandraService.close();
        });
    }

    @Test
    public void testDocumentWithComplexMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("author", "test_user");
        metadata.put("tags", Arrays.asList("tag1", "tag2", "tag3"));
        metadata.put("score", 0.95);
        metadata.put("nested", Map.of("key1", "value1", "key2", "value2"));
        
        Document document = createTestDocumentWithMetadata("Complex metadata document", 
                Arrays.asList(0.1, 0.2, 0.3), metadata);
        
        assertDoesNotThrow(() -> {
            cassandraService.addDocuments(Arrays.asList(document));
        });
    }

    @Test
    public void testLargeEmbeddingVector() {
        // Test with larger embedding vector
        List<Double> largeEmbedding = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeEmbedding.add(Math.random());
        }
        
        // Note: This would fail with the current test setup (dimension=3)
        // But the service should handle gracefully
        Document document = createTestDocument("Large embedding document", largeEmbedding);
        
        // This might throw an exception due to dimension mismatch
        // which is expected behavior
        assertThrows(RuntimeException.class, () -> {
            cassandraService.addDocuments(Arrays.asList(document));
        });
    }

    private Document createTestDocument(String content, List<Double> embedding) {
        Document document = new Document();
        document.setPageContent(content);
        document.setEmbedding(embedding);
        document.setUniqueId(UUID.randomUUID().toString());
        return document;
    }

    private Document createTestDocumentWithMetadata(String content, List<Double> embedding, Map<String, Object> metadata) {
        Document document = createTestDocument(content, embedding);
        document.setMetadata(metadata);
        return document;
    }
}
