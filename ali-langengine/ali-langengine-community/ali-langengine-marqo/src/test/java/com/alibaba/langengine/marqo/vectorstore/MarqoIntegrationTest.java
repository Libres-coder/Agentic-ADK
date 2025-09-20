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
package com.alibaba.langengine.marqo.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.*;
import java.util.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Integration tests require running Marqo instance")
public class MarqoIntegrationTest {

    private static Marqo marqo;
    private static final String TEST_INDEX = "test-integration-index";

    @BeforeAll
    static void setUpClass() {
        // Set up Marqo configuration for testing
        System.setProperty("marqo_server_url", "http://localhost:8882");
        
        MarqoParam param = new MarqoParam();
        param.getInitParam().setModel("hf/all_datasets_v4_MiniLM-L6");
        param.getInitParam().setMetric("cosine");
        
        marqo = new Marqo(TEST_INDEX, param);
    }

    @AfterAll
    static void tearDownClass() {
        if (marqo != null) {
            try {
                marqo.dropIndex();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    @Order(1)
    void testInit() {
        Assertions.assertDoesNotThrow(() -> marqo.init());
    }

    @Test
    @Order(2)
    void testAddDocuments() {
        List<Document> documents = Arrays.asList(
            createDocument("doc1", "Artificial intelligence is transforming the world"),
            createDocument("doc2", "Machine learning algorithms are becoming more sophisticated"),
            createDocument("doc3", "Deep learning models require large amounts of data"),
            createDocument("doc4", "Natural language processing enables computers to understand text"),
            createDocument("doc5", "Computer vision allows machines to interpret images")
        );

        Assertions.assertDoesNotThrow(() -> marqo.addDocuments(documents));
        
        // Wait for indexing to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Order(3)
    void testSimilaritySearch() {
        List<Document> results = marqo.similaritySearch("AI and machine learning", 3, null, null);
        
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.size() <= 3);
        
        if (!results.isEmpty()) {
            for (Document doc : results) {
                Assertions.assertNotNull(doc.getUniqueId());
                Assertions.assertNotNull(doc.getPageContent());
                System.out.println("Found document: " + doc.getUniqueId() + " - " + doc.getPageContent() + " (score: " + doc.getScore() + ")");
            }
        }
    }

    @Test
    @Order(4)
    void testSimilaritySearchWithMaxDistance() {
        List<Document> results = marqo.similaritySearch("computer vision", 5, 0.8, null);
        
        Assertions.assertNotNull(results);
        
        for (Document doc : results) {
            Assertions.assertTrue(doc.getScore() <= 0.8, 
                "Document score should be within max distance threshold");
        }
    }

    @Test
    @Order(5)
    void testSimilaritySearchNoResults() {
        List<Document> results = marqo.similaritySearch("completely unrelated quantum physics", 5, 0.1, null);
        
        Assertions.assertNotNull(results);
        // With a very low threshold, we might get no results
        System.out.println("Results for unrelated query: " + results.size());
    }

    @Test
    @Order(6)
    void testAddMoreDocuments() {
        List<Document> moreDocuments = Arrays.asList(
            createDocument("doc6", "Robotics combines AI with mechanical engineering"),
            createDocument("doc7", "Autonomous vehicles use computer vision and machine learning")
        );

        Assertions.assertDoesNotThrow(() -> marqo.addDocuments(moreDocuments));
        
        // Wait for indexing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify new documents can be found
        List<Document> results = marqo.similaritySearch("robotics and autonomous vehicles", 2, null, null);
        Assertions.assertNotNull(results);
    }

    private static Document createDocument(String id, String content) {
        Document doc = new Document();
        doc.setUniqueId(id);
        doc.setPageContent(content);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "integration-test");
        metadata.put("timestamp", System.currentTimeMillis());
        doc.setMetadata(metadata);
        
        return doc;
    }
}
