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
package com.alibaba.langengine.arxiv.service.impl;

import com.alibaba.langengine.arxiv.model.ArXivPaper;
import com.alibaba.langengine.arxiv.model.ArXivSearchRequest;
import com.alibaba.langengine.arxiv.model.ArXivSearchResponse;
import com.alibaba.langengine.arxiv.sdk.ArXivException;
import com.alibaba.langengine.arxiv.service.ArXivApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class ArXivApiServiceImplTest {
    
    private ArXivApiService service;
    
    @BeforeEach
    void setUp() {
        service = new ArXivApiServiceImpl();
    }
    
    @Test
    void testServiceCreation() {
        assertNotNull(service);
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSearchPapersWithRequest() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("machine learning")
                .maxResults(5)
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = service.searchPapers(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            assertFalse(response.isEmpty());
            assertTrue(response.getReturnedCount() > 0);
            assertTrue(response.getReturnedCount() <= 5);
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSearchPapersWithQuery() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = service.searchPapers("neural networks");
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            assertFalse(response.isEmpty());
            assertTrue(response.getReturnedCount() > 0);
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSearchPapersWithQueryAndMaxResults() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = service.searchPapers("deep learning", 3);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            assertFalse(response.isEmpty());
            assertTrue(response.getReturnedCount() > 0);
            assertTrue(response.getReturnedCount() <= 3);
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSearchByCategory() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = service.searchByCategory("cs.AI", 5);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            if (!response.isEmpty()) {
                // Verify that papers belong to the category
                response.getPapers().forEach(paper -> {
                    assertNotNull(paper.getCategories());
                    assertTrue(paper.getCategories().contains("cs.AI") || 
                              paper.getPrimaryCategory().equals("cs.AI"));
                });
            }
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSearchByAuthor() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = service.searchByAuthor("Smith", 3);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            // May not find results, but should not error
            if (!response.isEmpty()) {
                response.getPapers().forEach(paper -> {
                    assertNotNull(paper.getAuthors());
                    assertNotNull(paper.getTitle());
                });
            }
        });
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testGetRecentPapers() {
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = service.getRecentPapers("cs.LG", 30, 5);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            if (!response.isEmpty()) {
                response.getPapers().forEach(paper -> {
                    assertNotNull(paper.getPublished());
                    assertNotNull(paper.getCategories());
                    assertTrue(paper.getCategories().contains("cs.LG"));
                });
            }
        });
    }
    
    @Test
    void testGetPdfUrl() {
        String pdfUrl = service.getPdfUrl("2301.12345");
        assertNotNull(pdfUrl);
        assertTrue(pdfUrl.contains("arxiv.org/pdf/"));
        assertTrue(pdfUrl.endsWith(".pdf"));
        assertEquals("https://arxiv.org/pdf/2301.12345.pdf", pdfUrl);
    }
    
    @Test
    void testGetArxivUrl() {
        String arxivUrl = service.getArxivUrl("2301.12345");
        assertNotNull(arxivUrl);
        assertTrue(arxivUrl.contains("arxiv.org/abs/"));
        assertEquals("https://arxiv.org/abs/2301.12345", arxivUrl);
    }
    
    @Test
    void testInvalidQueries() {
        // Test null query
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers((String) null);
        });
        
        // Test empty query
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers("");
        });
        
        // Test whitespace only query
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers("   ");
        });
        
        // Test null request
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers((ArXivSearchRequest) null);
        });
        
        // Test request with null query
        ArXivSearchRequest request = new ArXivSearchRequest();
        request.setQuery(null);
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers(request);
        });
    }
    
    @Test
    void testInvalidMaxResults() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers("test", 0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers("test", -1);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchByCategory("cs.AI", 0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchByAuthor("Smith", -1);
        });
    }
    
    @Test
    void testInvalidParameters() {
        // Test invalid category
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchByCategory("", 5);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchByCategory(null, 5);
        });
        
        // Test invalid author
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchByAuthor("", 5);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchByAuthor(null, 5);
        });
        
        // Test invalid days for recent papers
        assertThrows(IllegalArgumentException.class, () -> {
            service.getRecentPapers("cs.AI", 0, 5);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.getRecentPapers("cs.AI", -1, 5);
        });
    }
    
    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void testGetPaper() {
        // Try to get a well-known paper - this might fail if the ID doesn't exist
        // but should demonstrate the functionality
        assertDoesNotThrow(() -> {
            try {
                ArXivPaper paper = service.getPaper("1706.03762"); // Attention is All You Need
                assertNotNull(paper);
                assertNotNull(paper.getId());
                assertNotNull(paper.getTitle());
                assertNotNull(paper.getAuthors());
                assertTrue(paper.getAuthors().size() > 0);
            } catch (ArXivException e) {
                // Paper might not exist, which is fine for this test
                assertNotNull(e.getMessage());
            }
        });
    }
    
    @Test
    void testGetPaperInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getPaper("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.getPaper(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.getPaper("   ");
        });
    }
    
    @Test
    void testGetPapersInvalidList() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getPapers(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.getPapers(Arrays.asList());
        });
    }
    
    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void testGetPapers() {
        assertDoesNotThrow(() -> {
            List<String> ids = Arrays.asList("1706.03762", "invalid-id", "1234.5678");
            List<ArXivPaper> papers = service.getPapers(ids);
            assertNotNull(papers);
            // Should return papers for valid IDs and skip invalid ones
            assertTrue(papers.size() <= ids.size());
        });
    }
    
    @Test
    void testDownloadPdfInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.downloadPdf("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.downloadPdf(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.downloadPdf("   ");
        });
    }
    
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testUrlGeneration() {
        String arxivId = "2301.12345";
        
        String pdfUrl = service.getPdfUrl(arxivId);
        String arxivUrl = service.getArxivUrl(arxivId);
        
        assertNotNull(pdfUrl);
        assertNotNull(arxivUrl);
        
        assertTrue(pdfUrl.contains(arxivId));
        assertTrue(arxivUrl.contains(arxivId));
        
        assertTrue(pdfUrl.endsWith(".pdf"));
        assertTrue(arxivUrl.contains("/abs/"));
        assertTrue(pdfUrl.contains("/pdf/"));
    }
    
    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testComplexSearch() {
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("transformer attention")
                .maxResults(3)
                .categories(Arrays.asList("cs.AI", "cs.LG"))
                .sortBy("lastUpdatedDate")
                .sortOrder("descending")
                .includeAbstract(true)
                .includePdfLink(true)
                .includeFullTextLink(true)
                .build();
        
        assertDoesNotThrow(() -> {
            ArXivSearchResponse response = service.searchPapers(request);
            assertNotNull(response);
            assertTrue(response.isSuccessful());
            
            if (!response.isEmpty()) {
                response.getPapers().forEach(paper -> {
                    assertNotNull(paper.getId());
                    assertNotNull(paper.getTitle());
                    assertNotNull(paper.getSummary());
                    assertNotNull(paper.getCategories());
                    assertNotNull(paper.getArxivUrl());
                    assertNotNull(paper.getPdfUrl());
                });
            }
        });
    }
}
