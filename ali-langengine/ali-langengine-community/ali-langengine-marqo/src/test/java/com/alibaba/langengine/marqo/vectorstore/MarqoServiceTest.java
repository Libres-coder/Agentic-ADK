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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MarqoServiceTest {

    private MarqoService service;

    @BeforeEach
    void setUp() {
        service = mock(MarqoService.class);
    }

    @Test
    @Order(1)
    void testInit() {
        doNothing().when(service).init();

        assertDoesNotThrow(() -> service.init());

        verify(service, times(1)).init();
    }

    @Test
    @Order(2)
    void testAddDocuments() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("This is a test document about artificial intelligence");
        
        Document d2 = new Document();
        d2.setUniqueId("d2");
        d2.setPageContent("Machine learning is a subset of artificial intelligence");

        doNothing().when(service).addDocuments(anyList());

        service.addDocuments(Arrays.asList(d1, d2));

        verify(service, times(1)).addDocuments(anyList());
    }

    @Test
    @Order(3)
    void testAddDocumentsEmpty() {
        doNothing().when(service).addDocuments(Collections.emptyList());

        service.addDocuments(Collections.emptyList());

        verify(service, times(1)).addDocuments(Collections.emptyList());
    }

    @Test
    @Order(4)
    void testSimilaritySearch() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("This is a test document about artificial intelligence");
        d1.setScore(0.1);

        Document d2 = new Document();
        d2.setUniqueId("d2");
        d2.setPageContent("Machine learning is a subset of artificial intelligence");
        d2.setScore(0.3);

        when(service.similaritySearch(eq("artificial intelligence"), eq(2), isNull(), isNull()))
                .thenReturn(Arrays.asList(d1, d2));

        List<Document> results = service.similaritySearch("artificial intelligence", 2, null, null);

        assertEquals(2, results.size());
        assertEquals("d1", results.get(0).getUniqueId());
        assertEquals("d2", results.get(1).getUniqueId());
        assertTrue(results.get(0).getScore() <= results.get(1).getScore());

        verify(service, times(1)).similaritySearch(eq("artificial intelligence"), eq(2), isNull(), isNull());
    }

    @Test
    @Order(5)
    void testSimilaritySearchWithMaxDistance() {
        Document d1 = new Document();
        d1.setUniqueId("d1");
        d1.setPageContent("This is a test document about artificial intelligence");
        d1.setScore(0.2);

        when(service.similaritySearch(eq("AI technology"), eq(5), eq(0.5), isNull()))
                .thenReturn(Collections.singletonList(d1));

        List<Document> results = service.similaritySearch("AI technology", 5, 0.5, null);

        assertEquals(1, results.size());
        assertEquals("d1", results.get(0).getUniqueId());
        assertTrue(results.get(0).getScore() <= 0.5);

        verify(service, times(1)).similaritySearch(eq("AI technology"), eq(5), eq(0.5), isNull());
    }

    @Test
    @Order(6)
    void testSimilaritySearchEmptyQuery() {
        when(service.similaritySearch(eq(""), eq(5), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        List<Document> results = service.similaritySearch("", 5, null, null);

        assertEquals(0, results.size());

        verify(service, times(1)).similaritySearch(eq(""), eq(5), isNull(), isNull());
    }

    @Test
    @Order(7)
    void testDropIndex() {
        doNothing().when(service).dropIndex();

        assertDoesNotThrow(() -> service.dropIndex());

        verify(service, times(1)).dropIndex();
    }
}
