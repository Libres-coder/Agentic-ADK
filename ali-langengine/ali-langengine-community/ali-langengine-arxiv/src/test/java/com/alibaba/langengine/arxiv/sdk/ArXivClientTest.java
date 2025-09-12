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
package com.alibaba.langengine.arxiv.sdk;

import com.alibaba.langengine.arxiv.model.ArXivSearchRequest;
import com.alibaba.langengine.arxiv.model.ArXivSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


class ArXivClientTest {
    
    private ArXivClient client;
    
    @BeforeEach
    void setUp() {
        client = new ArXivClient();
    }
    
    @Test
    void testArXivClientConstruction() {
        assertNotNull(client);
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSimpleSearch() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search("machine learning");
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            assertFalse(response.isEmpty());
            assertTrue(response.getReturnedCount() > 0);
            assertNotNull(response.getPapers());
            assertEquals("machine learning", response.getQuery());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSearchWithCount() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search("deep learning", 5);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            assertFalse(response.isEmpty());
            assertTrue(response.getReturnedCount() <= 5);
            assertTrue(response.getReturnedCount() > 0);
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSearchWithRequestObject() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("neural networks")
                .maxResults(3)
                .sortBy("relevance")
                .sortOrder("descending")
                .categories(Arrays.asList("cs.AI", "cs.LG"))
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            assertFalse(response.isEmpty());
            assertTrue(response.getReturnedCount() <= 3);
            assertTrue(response.getReturnedCount() > 0);
            assertEquals("neural networks", response.getQuery());
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testCategorySearch() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("cat:cs.AI")
                .maxResults(5)
                .sortBy("lastUpdatedDate")
                .sortOrder("descending")
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            assertFalse(response.isEmpty());
            
            // Verify that returned papers have the correct category
            response.getPapers().forEach(paper -> {
                assertNotNull(paper.getCategories());
                assertTrue(paper.getCategories().contains("cs.AI") || 
                          paper.getPrimaryCategory().equals("cs.AI"));
            });
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testAuthorSearch() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("au:\"Geoffrey Hinton\"")
                .maxResults(3)
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            // May not find results for specific author, but should not error
            if (!response.isEmpty()) {
                response.getPapers().forEach(paper -> {
                    assertNotNull(paper.getAuthors());
                    assertNotNull(paper.getTitle());
                });
            }
        });
    }
    
    @Test
    void testEmptyQuery() {
        assertThrows(ArXivException.class, () -> {
            client.search("");
        });
        
        assertThrows(ArXivException.class, () -> {
            client.search("   ");
        });
        
        ArXivSearchRequest request = new ArXivSearchRequest();
        request.setQuery("");
        assertThrows(ArXivException.class, () -> {
            client.search(request);
        });
    }
    
    @Test
    void testNullQuery() {
        ArXivSearchRequest request = new ArXivSearchRequest();
        request.setQuery(null);
        assertThrows(ArXivException.class, () -> {
            client.search(request);
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testMaxResultsLimit() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("artificial intelligence")
                .maxResults(150) // Exceeds API limit
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            // Should be limited to MAX_RESULTS_LIMIT
            assertTrue(response.getReturnedCount() <= ArXivConstant.MAX_RESULTS_LIMIT);
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testPaginationParameters() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("computer science")
                .maxResults(5)
                .start(10)
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            if (!response.isEmpty()) {
                assertEquals(Integer.valueOf(10), response.getStartIndex());
                assertTrue(response.getReturnedCount() <= 5);
            }
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSortingParameters() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("quantum computing")
                .maxResults(5)
                .sortBy("submittedDate")
                .sortOrder("ascending")
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            if (!response.isEmpty()) {
                // Verify papers have required fields
                response.getPapers().forEach(paper -> {
                    assertNotNull(paper.getId());
                    assertNotNull(paper.getTitle());
                    assertNotNull(paper.getArxivUrl());
                    assertNotNull(paper.getPdfUrl());
                });
            }
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testResponseFields() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = client.search("test", 3);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            if (!response.isEmpty()) {
                // Verify response metadata
                assertNotNull(response.getTotalResults());
                assertNotNull(response.getStartIndex());
                assertNotNull(response.getItemsPerPage());
                assertNotNull(response.getExecutionTimeMs());
                assertTrue(response.getExecutionTimeMs() > 0);
                
                // Verify paper fields
                response.getPapers().forEach(paper -> {
                    assertNotNull(paper.getId());
                    assertNotNull(paper.getTitle());
                    assertNotNull(paper.getSummary());
                    assertNotNull(paper.getAuthors());
                    assertNotNull(paper.getCategories());
                    assertNotNull(paper.getArxivUrl());
                    assertNotNull(paper.getPdfUrl());
                    assertNotNull(paper.getPublished());
                    
                    // Verify URL formats
                    assertTrue(paper.getArxivUrl().contains("arxiv.org/abs/"));
                    assertTrue(paper.getPdfUrl().contains("arxiv.org/pdf/"));
                    assertTrue(paper.getPdfUrl().endsWith(".pdf"));
                });
            }
        });
    }
}
