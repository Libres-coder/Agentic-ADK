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
public class CassandraTest {

    private Cassandra cassandra;
    private CassandraConfiguration configuration;
    private CassandraParam cassandraParam;

    @BeforeEach
    public void setUp() {
        configuration = createTestConfiguration();
        cassandraParam = createTestParameters();
        
        try {
            cassandra = new Cassandra(configuration, cassandraParam);
        } catch (Exception e) {
            // Skip tests if Cassandra is not available
            org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                    "Skipping Cassandra tests - Cassandra not available: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        if (cassandra != null) {
            cassandra.close();
        }
    }

    private CassandraConfiguration createTestConfiguration() {
        CassandraConfiguration config = new CassandraConfiguration();
        config.setContactPoints(Arrays.asList("127.0.0.1:9042"));
        config.setLocalDatacenter("datacenter1");
        config.setKeyspace("test_langengine");
        // Note: username and password can be null for local testing
        return config;
    }

    private CassandraParam createTestParameters() {
        CassandraParam param = new CassandraParam();
        
        CassandraParam.InitParam initParam = new CassandraParam.InitParam();
        initParam.setTableName("test_documents");
        initParam.setVectorDimensions(3); // Small dimension for testing
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
    public void testConstructorWithConfiguration() {
        assertNotNull(cassandra);
        assertNotNull(cassandra.getConfiguration());
        assertNotNull(cassandra.getCassandraParam());
        assertNotNull(cassandra.getCassandraService());
    }

    @Test
    public void testAddSingleDocument() {
        Document document = createTestDocument("Test content", Arrays.asList(0.1, 0.2, 0.3));
        
        assertDoesNotThrow(() -> {
            cassandra.addDocuments(Arrays.asList(document));
        });
    }

    @Test
    public void testAddMultipleDocuments() {
        List<Document> documents = Arrays.asList(
                createTestDocument("First document", Arrays.asList(0.1, 0.2, 0.3)),
                createTestDocument("Second document", Arrays.asList(0.4, 0.5, 0.6)),
                createTestDocument("Third document", Arrays.asList(0.7, 0.8, 0.9))
        );
        
        assertDoesNotThrow(() -> {
            cassandra.addDocuments(documents);
        });
    }

    @Test
    public void testSimilaritySearch() {
        // Add test documents
        List<Document> documents = Arrays.asList(
                createTestDocument("Apple fruit", Arrays.asList(0.1, 0.9, 0.1)),
                createTestDocument("Orange fruit", Arrays.asList(0.2, 0.8, 0.2)),
                createTestDocument("Car vehicle", Arrays.asList(0.9, 0.1, 0.8))
        );
        
        cassandra.addDocuments(documents);
        
        // Search for fruit-like vectors
        List<Double> queryVector = Arrays.asList(0.15, 0.85, 0.15);
        List<Document> results = cassandra.similaritySearch(queryVector, 2);
        
        assertNotNull(results);
        assertTrue(results.size() <= 2);
        
        if (!results.isEmpty()) {
            // Results should be ordered by similarity
            assertTrue(results.get(0).getPageContent().contains("fruit") || 
                      results.get(0).getPageContent().contains("Apple") ||
                      results.get(0).getPageContent().contains("Orange"));
        }
    }

    @Test
    public void testSimilaritySearchByVector() {
        Document document = createTestDocument("Test document", Arrays.asList(0.5, 0.5, 0.5));
        cassandra.addDocuments(Arrays.asList(document));
        
        List<Double> queryVector = Arrays.asList(0.6, 0.4, 0.5);
        List<Document> results = cassandra.similaritySearchByVector(queryVector, 1);
        
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("Test document", results.get(0).getPageContent());
    }

    @Test
    public void testSimilaritySearchWithExtraParams() {
        Document document = createTestDocument("Test document", Arrays.asList(0.1, 0.2, 0.3));
        cassandra.addDocuments(Arrays.asList(document));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("maxDistanceValue", 0.5);
        
        List<Document> results = cassandra.similaritySearchByVector(queryVector, 1, extraParams);
        
        assertNotNull(results);
    }

    @Test
    public void testFromDocuments() {
        List<Document> documents = Arrays.asList(
                createTestDocument("Document 1", Arrays.asList(0.1, 0.2, 0.3)),
                createTestDocument("Document 2", Arrays.asList(0.4, 0.5, 0.6))
        );
        
        Cassandra newCassandra = (Cassandra) cassandra.fromDocuments(documents, null);
        
        assertNotNull(newCassandra);
        assertSame(cassandra, newCassandra);
    }

    @Test
    public void testFromTexts() {
        List<String> texts = Arrays.asList("Text 1", "Text 2", "Text 3");
        List<Map<String, Object>> metadatas = Arrays.asList(
                createTestMetadata("key1", "value1"),
                createTestMetadata("key2", "value2"),
                createTestMetadata("key3", "value3")
        );
        
        Cassandra newCassandra = (Cassandra) cassandra.fromTexts(texts, metadatas, null);
        
        assertNotNull(newCassandra);
        assertSame(cassandra, newCassandra);
    }

    @Test
    public void testDocumentWithMetadata() {
        Map<String, Object> metadata = createTestMetadata("author", "test_user");
        Document document = createTestDocumentWithMetadata("Document with metadata", 
                Arrays.asList(0.1, 0.2, 0.3), metadata);
        
        cassandra.addDocuments(Arrays.asList(document));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        List<Document> results = cassandra.similaritySearch(queryVector, 1);
        
        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        Document result = results.get(0);
        assertNotNull(result.getMetadata());
        assertTrue(result.getMetadata().containsKey("author") || result.getMetadata().containsKey("similarity"));
    }

    @Test
    public void testEmptyEmbeddingHandling() {
        List<Double> emptyEmbedding = new ArrayList<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            cassandra.similaritySearch(emptyEmbedding, 1);
        });
    }

    @Test
    public void testNullEmbeddingHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            cassandra.similaritySearch((List<Double>) null, 1);
        });
    }

    @Test
    public void testMaxMarginalRelevanceSearch() {
        Document document = createTestDocument("Test document", Arrays.asList(0.1, 0.2, 0.3));
        cassandra.addDocuments(Arrays.asList(document));
        
        List<Double> queryVector = Arrays.asList(0.1, 0.2, 0.3);
        
        // This should fall back to similarity search for now
        List<Document> results = cassandra.maxMarginalRelevanceSearchByVector(queryVector, 1, 0.5);
        
        assertNotNull(results);
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

    private Map<String, Object> createTestMetadata(String key, String value) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(key, value);
        return metadata;
    }
}
