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
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MarqoTest {

    private Marqo marqo;
    private MarqoService mockService;

    @BeforeEach
    void setUp() {
        mockService = mock(MarqoService.class);
        marqo = spy(new Marqo("test-index"));
        
        // Replace the service with our mock
        try {
            Field serviceField = Marqo.class.getDeclaredField("marqoService");
            serviceField.setAccessible(true);
            serviceField.set(marqo, mockService);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testConstructor() {
        Marqo marqoStore = new Marqo("test-index");
        assertEquals("test-index", marqoStore.getIndexName());
        assertNotNull(marqoStore.getMarqoService());
    }

    @Test
    @Order(2)
    void testConstructorWithParam() {
        MarqoParam param = new MarqoParam();
        param.getInitParam().setModel("hf/e5-base-v2");
        
        Marqo marqoStore = new Marqo("test-index", param);
        assertEquals("test-index", marqoStore.getIndexName());
        assertNotNull(marqoStore.getMarqoService());
    }

    @Test
    @Order(3)
    void testInit() {
        doNothing().when(mockService).init();

        assertDoesNotThrow(() -> marqo.init());

        verify(mockService, times(1)).init();
    }

    @Test
    @Order(4)
    void testAddDocuments() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("This is a test document");

        Document d2 = new Document();
        d2.setUniqueId("d2");
        d2.setPageContent("Another test document");

        doNothing().when(mockService).addDocuments(anyList());

        marqo.addDocuments(Arrays.asList(d1, d2));

        verify(mockService, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(5)
    void testAddDocumentsEmpty() {
        marqo.addDocuments(Collections.emptyList());

        verify(mockService, never()).addDocuments(anyList());
    }

    @Test
    @Order(6)
    void testAddDocumentsNull() {
        marqo.addDocuments(null);

        verify(mockService, never()).addDocuments(anyList());
    }

    @Test
    @Order(7)
    void testSimilaritySearch() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("AI and machine learning");
        d1.setScore(0.1);

        Document d2 = new Document();
        d2.setUniqueId("d2");
        d2.setPageContent("Deep learning algorithms");
        d2.setScore(0.3);

        when(mockService.similaritySearch(eq("artificial intelligence"), eq(2), isNull(), isNull()))
                .thenReturn(Arrays.asList(d1, d2));

        List<Document> results = marqo.similaritySearch("artificial intelligence", 2, null, null);

        assertEquals(2, results.size());
        assertEquals("d1", results.get(0).getUniqueId());
        assertEquals("d2", results.get(1).getUniqueId());

        verify(mockService, times(1)).similaritySearch(eq("artificial intelligence"), eq(2), isNull(), isNull());
    }

    @Test
    @Order(8)
    void testSimilaritySearchEmptyQuery() {
        List<Document> results = marqo.similaritySearch("", 5, null, null);

        assertEquals(0, results.size());

        verify(mockService, never()).similaritySearch(anyString(), anyInt(), any(), any());
    }

    @Test
    @Order(9)
    void testSimilaritySearchNullQuery() {
        List<Document> results = marqo.similaritySearch(null, 5, null, null);

        assertEquals(0, results.size());

        verify(mockService, never()).similaritySearch(anyString(), anyInt(), any(), any());
    }

    @Test
    @Order(10)
    void testSimilaritySearchWithMaxDistance() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("Relevant document");
        d1.setScore(0.2);

        when(mockService.similaritySearch(eq("test query"), eq(3), eq(0.5), eq(1)))
                .thenReturn(Collections.singletonList(d1));

        List<Document> results = marqo.similaritySearch("test query", 3, 0.5, 1);

        assertEquals(1, results.size());
        assertEquals("d1", results.get(0).getUniqueId());
        assertTrue(results.get(0).getScore() <= 0.5);

        verify(mockService, times(1)).similaritySearch(eq("test query"), eq(3), eq(0.5), eq(1));
    }

    @Test
    @Order(11)
    void testDropIndex() {
        doNothing().when(mockService).dropIndex();

        assertDoesNotThrow(() -> marqo.dropIndex());

        verify(mockService, times(1)).dropIndex();
    }

    @Test
    @Order(12)
    void testGetMarqoService() {
        MarqoService service = marqo.getMarqoService();
        assertNotNull(service);
        assertEquals(mockService, service);
    }
}
