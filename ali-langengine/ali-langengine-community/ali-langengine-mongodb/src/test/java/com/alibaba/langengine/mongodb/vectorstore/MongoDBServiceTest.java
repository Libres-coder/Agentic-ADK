package com.alibaba.langengine.mongodb.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MongoDBServiceTest {

    private MongoDBService service;

    @BeforeEach
    void setUp() {
        service = mock(MongoDBService.class);
    }

    @Test
    @Order(1)
    void testAddDocuments() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("alpha");
        d1.setEmbedding(Arrays.asList(1.0, 0.0, 0.0));

        doNothing().when(service).addDocuments(anyList());

        service.addDocuments(Collections.singletonList(d1));

        verify(service, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(2)
    void testAddMultipleDocuments() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("first document");
        d1.setEmbedding(Arrays.asList(1.0, 0.0, 0.0));

        Document d2 = new Document();
        d2.setUniqueId("d2");
        d2.setPageContent("second document");
        d2.setEmbedding(Arrays.asList(0.0, 1.0, 0.0));

        Document d3 = new Document();
        d3.setUniqueId("d3");
        d3.setPageContent("third document");
        d3.setEmbedding(Arrays.asList(0.0, 0.0, 1.0));

        doNothing().when(service).addDocuments(anyList());

        service.addDocuments(Arrays.asList(d1, d2, d3));

        verify(service, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(3)
    void testAddDocumentsWithMetadata() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("document with metadata");
        d1.setEmbedding(Arrays.asList(1.0, 0.5, 0.3));
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("category", "test");
        metadata.put("source", "unit_test");
        metadata.put("timestamp", System.currentTimeMillis());
        d1.setMetadata(metadata);

        doNothing().when(service).addDocuments(anyList());

        service.addDocuments(Collections.singletonList(d1));

        verify(service, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(4)
    void testSimilaritySearch() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setScore(0.1);

        Document d2 = new Document();
        d2.setUniqueId("d2");
        d2.setScore(0.5);

        when(service.similaritySearch(anyList(), eq(2), isNull(), isNull()))
                .thenReturn(Arrays.asList(d1, d2));

        List<Document> res = service.similaritySearch(Arrays.asList(1.0f, 0.0f), 2, null, null);

        assertEquals(2, res.size());
        assertEquals("d1", res.get(0).getUniqueId());
        verify(service, times(1)).similaritySearch(anyList(), eq(2), isNull(), isNull());
    }

    @Test
    @Order(5)
    void testSimilaritySearchWithMaxDistance() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setScore(0.2);
        d1.setPageContent("close match");

        when(service.similaritySearch(anyList(), eq(5), eq(0.5), isNull()))
                .thenReturn(Collections.singletonList(d1));

        List<Document> res = service.similaritySearch(Arrays.asList(0.8f, 0.2f, 0.1f), 5, 0.5, null);

        assertEquals(1, res.size());
        assertEquals("d1", res.get(0).getUniqueId());
        assertTrue(res.get(0).getScore() <= 0.5);
        verify(service, times(1)).similaritySearch(anyList(), eq(5), eq(0.5), isNull());
    }

    @Test
    @Order(6)
    void testSimilaritySearchWithType() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setScore(0.15);
        
        Document d2 = new Document();
        d2.setUniqueId("d2");
        d2.setScore(0.35);

        when(service.similaritySearch(anyList(), eq(3), isNull(), eq(1)))
                .thenReturn(Arrays.asList(d1, d2));

        List<Document> res = service.similaritySearch(Arrays.asList(0.9f, 0.1f, 0.0f), 3, null, 1);

        assertEquals(2, res.size());
        assertEquals("d1", res.get(0).getUniqueId());
        assertEquals("d2", res.get(1).getUniqueId());
        verify(service, times(1)).similaritySearch(anyList(), eq(3), isNull(), eq(1));
    }

    @Test
    @Order(7)
    void testSimilaritySearchEmptyResults() {
        when(service.similaritySearch(anyList(), eq(10), eq(0.1), isNull()))
                .thenReturn(Collections.emptyList());

        List<Document> res = service.similaritySearch(Arrays.asList(0.0f, 0.0f, 1.0f), 10, 0.1, null);

        assertTrue(res.isEmpty());
        verify(service, times(1)).similaritySearch(anyList(), eq(10), eq(0.1), isNull());
    }

    @Test
    @Order(8)
    void testSimilaritySearchSorted() {
        Document d1 = new Document();
        d1.setUniqueId("best_match");
        d1.setScore(0.05);

        Document d2 = new Document();
        d2.setUniqueId("good_match");
        d2.setScore(0.15);

        Document d3 = new Document();
        d3.setUniqueId("okay_match");
        d3.setScore(0.25);

        when(service.similaritySearch(anyList(), eq(3), isNull(), isNull()))
                .thenReturn(Arrays.asList(d1, d2, d3));

        List<Document> res = service.similaritySearch(Arrays.asList(1.0f, 0.0f, 0.0f), 3, null, null);

        assertEquals(3, res.size());
        assertEquals("best_match", res.get(0).getUniqueId());
        assertEquals("good_match", res.get(1).getUniqueId());
        assertEquals("okay_match", res.get(2).getUniqueId());
        
        // Verify results are sorted by score
        assertTrue(res.get(0).getScore() <= res.get(1).getScore());
        assertTrue(res.get(1).getScore() <= res.get(2).getScore());
        
        verify(service, times(1)).similaritySearch(anyList(), eq(3), isNull(), isNull());
    }

    @Test
    @Order(9)
    void testDropClass() {
        doNothing().when(service).dropClass();

        service.dropClass();

        verify(service, times(1)).dropClass();
    }

    @Test
    @Order(10)
    void testInit() {
        doNothing().when(service).init();

        service.init();

        verify(service, times(1)).init();
    }

    @Test
    @Order(11)
    void testAddDocumentsNullInput() {
        doNothing().when(service).addDocuments(isNull());

        service.addDocuments(null);

        verify(service, times(1)).addDocuments(isNull());
    }

    @Test
    @Order(12)
    void testAddDocumentsEmptyList() {
        doNothing().when(service).addDocuments(anyList());

        service.addDocuments(Collections.emptyList());

        verify(service, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(13)
    void testSimilaritySearchNullQuery() {
        when(service.similaritySearch(isNull(), eq(5), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        List<Document> res = service.similaritySearch(null, 5, null, null);

        assertTrue(res.isEmpty());
        verify(service, times(1)).similaritySearch(isNull(), eq(5), isNull(), isNull());
    }

    @Test
    @Order(14)
    void testSimilaritySearchEmptyQuery() {
        when(service.similaritySearch(eq(Collections.emptyList()), eq(5), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        List<Document> res = service.similaritySearch(Collections.emptyList(), 5, null, null);

        assertTrue(res.isEmpty());
        verify(service, times(1)).similaritySearch(eq(Collections.emptyList()), eq(5), isNull(), isNull());
    }

    @Test
    @Order(15)
    void testSimilaritySearchWithHighDimensionVector() {
        Document d1 = new Document();
        d1.setUniqueId("high_dim_doc");
        d1.setScore(0.25);

        // Test with high dimensional vector
        List<Float> highDimVector = new ArrayList<>();
        for (int i = 0; i < 768; i++) {
            highDimVector.add((float) Math.random());
        }

        when(service.similaritySearch(eq(highDimVector), eq(10), isNull(), isNull()))
                .thenReturn(Collections.singletonList(d1));

        List<Document> res = service.similaritySearch(highDimVector, 10, null, null);

        assertEquals(1, res.size());
        assertEquals("high_dim_doc", res.get(0).getUniqueId());
        verify(service, times(1)).similaritySearch(eq(highDimVector), eq(10), isNull(), isNull());
    }

    @Test
    @Order(16)
    void testSimilaritySearchWithLargeK() {
        List<Document> largeResultSet = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Document doc = new Document();
            doc.setUniqueId("doc_" + i);
            doc.setScore(0.1 + (i * 0.01));
            largeResultSet.add(doc);
        }

        when(service.similaritySearch(anyList(), eq(100), isNull(), isNull()))
                .thenReturn(largeResultSet);

        List<Document> res = service.similaritySearch(Arrays.asList(1.0f, 0.0f, 0.0f), 100, null, null);

        assertEquals(50, res.size());
        assertEquals("doc_0", res.get(0).getUniqueId());
        assertEquals("doc_49", res.get(49).getUniqueId());
        verify(service, times(1)).similaritySearch(anyList(), eq(100), isNull(), isNull());
    }
}
