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
package com.alibaba.langengine.arxiv.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ArXivModelTest {
    
    @Test
    void testArXivSearchRequestCreation() {
        ArXivSearchRequest request = new ArXivSearchRequest("test query");
        
        assertEquals("test query", request.getQuery());
        assertEquals(Integer.valueOf(10), request.getMaxResults());
        assertEquals(Integer.valueOf(0), request.getStart());
        assertEquals("relevance", request.getSortBy());
        assertEquals("descending", request.getSortOrder());
        assertEquals(Boolean.TRUE, request.getIncludeAbstract());
        assertEquals(Boolean.TRUE, request.getIncludeFullTextLink());
        assertEquals(Boolean.TRUE, request.getIncludePdfLink());
    }
    
    @Test
    void testArXivSearchRequestBuilder() {
        List<String> categories = Arrays.asList("cs.AI", "cs.LG");
        List<String> authors = Arrays.asList("Smith", "Johnson");
        
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("machine learning")
                .maxResults(20)
                .start(10)
                .sortBy("lastUpdatedDate")
                .sortOrder("ascending")
                .categories(categories)
                .authors(authors)
                .includeAbstract(false)
                .includeFullTextLink(false)
                .includePdfLink(true)
                .dateRange("2023-01-01", "2023-12-31")
                .build();
        
        assertEquals("machine learning", request.getQuery());
        assertEquals(Integer.valueOf(20), request.getMaxResults());
        assertEquals(Integer.valueOf(10), request.getStart());
        assertEquals("lastUpdatedDate", request.getSortBy());
        assertEquals("ascending", request.getSortOrder());
        assertEquals(categories, request.getCategories());
        assertEquals(authors, request.getAuthors());
        assertEquals(Boolean.FALSE, request.getIncludeAbstract());
        assertEquals(Boolean.FALSE, request.getIncludeFullTextLink());
        assertEquals(Boolean.TRUE, request.getIncludePdfLink());
        assertEquals("2023-01-01", request.getStartDate());
        assertEquals("2023-12-31", request.getEndDate());
    }
    
    @Test
    void testArXivPaperCreation() {
        ArXivPaper paper = new ArXivPaper();
        
        paper.setId("2301.12345");
        paper.setTitle("Test Paper Title");
        paper.setSummary("This is a test summary of the paper.");
        paper.setAuthors(Arrays.asList("John Smith", "Jane Doe"));
        paper.setCategories(Arrays.asList("cs.AI", "cs.LG"));
        paper.setPrimaryCategory("cs.AI");
        paper.setPublished(LocalDateTime.of(2023, 1, 15, 10, 30));
        paper.setUpdated(LocalDateTime.of(2023, 1, 16, 14, 45));
        paper.setArxivUrl("https://arxiv.org/abs/2301.12345");
        paper.setPdfUrl("https://arxiv.org/pdf/2301.12345.pdf");
        paper.setDoi("10.1000/test.doi");
        paper.setJournalRef("Test Journal 2023");
        paper.setComment("Test comment");
        paper.setVersion("v1");
        
        assertEquals("2301.12345", paper.getId());
        assertEquals("Test Paper Title", paper.getTitle());
        assertEquals("This is a test summary of the paper.", paper.getSummary());
        assertEquals(Arrays.asList("John Smith", "Jane Doe"), paper.getAuthors());
        assertEquals(Arrays.asList("cs.AI", "cs.LG"), paper.getCategories());
        assertEquals("cs.AI", paper.getPrimaryCategory());
        assertEquals(LocalDateTime.of(2023, 1, 15, 10, 30), paper.getPublished());
        assertEquals(LocalDateTime.of(2023, 1, 16, 14, 45), paper.getUpdated());
        assertEquals("https://arxiv.org/abs/2301.12345", paper.getArxivUrl());
        assertEquals("https://arxiv.org/pdf/2301.12345.pdf", paper.getPdfUrl());
        assertEquals("10.1000/test.doi", paper.getDoi());
        assertEquals("Test Journal 2023", paper.getJournalRef());
        assertEquals("Test comment", paper.getComment());
        assertEquals("v1", paper.getVersion());
    }
    
    @Test
    void testArXivPaperCitation() {
        ArXivPaper paper = new ArXivPaper();
        paper.setId("2301.12345");
        paper.setTitle("Machine Learning Advances");
        paper.setAuthors(Arrays.asList("John Smith", "Jane Doe"));
        paper.setPublished(LocalDateTime.of(2023, 1, 15, 10, 30));
        
        String citation = paper.getCitation();
        assertNotNull(citation);
        assertTrue(citation.contains("John Smith, Jane Doe"));
        assertTrue(citation.contains("Machine Learning Advances"));
        assertTrue(citation.contains("2023"));
        assertTrue(citation.contains("2301.12345"));
    }
    
    @Test
    void testArXivPaperCitationManyAuthors() {
        ArXivPaper paper = new ArXivPaper();
        paper.setId("2301.12345");
        paper.setTitle("Deep Learning Research");
        paper.setAuthors(Arrays.asList("Author1", "Author2", "Author3", "Author4", "Author5"));
        paper.setPublished(LocalDateTime.of(2023, 1, 15, 10, 30));
        
        String citation = paper.getCitation();
        assertNotNull(citation);
        assertTrue(citation.contains("Author1 et al."));
        assertTrue(citation.contains("Deep Learning Research"));
        assertTrue(citation.contains("2023"));
    }
    
    @Test
    void testArXivPaperShortSummary() {
        ArXivPaper paper = new ArXivPaper();
        
        // Test short summary
        paper.setSummary("This is a short summary.");
        assertEquals("This is a short summary.", paper.getShortSummary());
        
        // Test long summary
        StringBuilder longSummary = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longSummary.append("This is a very long summary. ");
        }
        paper.setSummary(longSummary.toString());
        
        String shortSummary = paper.getShortSummary();
        assertTrue(shortSummary.length() <= 200);
        assertTrue(shortSummary.endsWith("..."));
    }
    
    @Test
    void testArXivPaperIsRecent() {
        ArXivPaper paper = new ArXivPaper();
        
        // Test recent paper (within 30 days)
        paper.setPublished(LocalDateTime.now().minusDays(15));
        assertTrue(paper.isRecent());
        
        // Test old paper (more than 30 days)
        paper.setPublished(LocalDateTime.now().minusDays(45));
        assertFalse(paper.isRecent());
        
        // Test paper with no published date
        paper.setPublished(null);
        assertFalse(paper.isRecent());
    }
    
    @Test
    void testArXivPaperMainCategory() {
        ArXivPaper paper = new ArXivPaper();
        
        // Test with categories list
        paper.setCategories(Arrays.asList("cs.AI", "cs.LG", "stat.ML"));
        assertEquals("cs.AI", paper.getMainCategory());
        
        // Test with empty categories but primary category set
        paper.setCategories(Arrays.asList());
        paper.setPrimaryCategory("math.CO");
        assertEquals("math.CO", paper.getMainCategory());
        
        // Test with null categories
        paper.setCategories(null);
        paper.setPrimaryCategory("physics.comp-ph");
        assertEquals("physics.comp-ph", paper.getMainCategory());
    }
    
    @Test
    void testArXivSearchResponseSuccess() {
        ArXivPaper paper1 = new ArXivPaper();
        paper1.setId("2301.12345");
        paper1.setTitle("Paper 1");
        
        ArXivPaper paper2 = new ArXivPaper();
        paper2.setId("2301.12346");
        paper2.setTitle("Paper 2");
        
        List<ArXivPaper> papers = Arrays.asList(paper1, paper2);
        
        ArXivSearchResponse response = new ArXivSearchResponse(papers, 10, 0, 5, "test query");
        
        assertTrue(response.isSuccessful());
        assertFalse(response.isEmpty());
        assertEquals(2, response.getReturnedCount());
        assertEquals(papers, response.getPapers());
        assertEquals(Integer.valueOf(10), response.getTotalResults());
        assertEquals(Integer.valueOf(0), response.getStartIndex());
        assertEquals(Integer.valueOf(5), response.getItemsPerPage());
        assertEquals("test query", response.getQuery());
        assertEquals(Boolean.TRUE, response.getHasMoreResults());
    }
    
    @Test
    void testArXivSearchResponseError() {
        ArXivSearchResponse response = new ArXivSearchResponse("Search failed");
        
        assertFalse(response.isSuccessful());
        assertTrue(response.isEmpty());
        assertEquals(0, response.getReturnedCount());
        assertEquals("Search failed", response.getErrorMessage());
        assertEquals(Integer.valueOf(0), response.getTotalResults());
        assertEquals(Boolean.FALSE, response.getHasMoreResults());
    }
    
    @Test
    void testArXivSearchResponsePagination() {
        List<ArXivPaper> papers = Arrays.asList(new ArXivPaper(), new ArXivPaper());
        ArXivSearchResponse response = new ArXivSearchResponse(papers, 20, 10, 5, "test");
        
        assertEquals(Integer.valueOf(3), response.getCurrentPage());
        assertEquals(Integer.valueOf(4), response.getTotalPages());
        assertEquals(Integer.valueOf(15), response.getNextStartIndex());
        assertEquals(Integer.valueOf(5), response.getPreviousStartIndex());
    }
    
    @Test
    void testArXivSearchResponsePaginationEdgeCases() {
        List<ArXivPaper> papers = Arrays.asList(new ArXivPaper());
        
        // First page
        ArXivSearchResponse response = new ArXivSearchResponse(papers, 10, 0, 5, "test");
        assertEquals(Integer.valueOf(1), response.getCurrentPage());
        assertNull(response.getPreviousStartIndex());
        assertEquals(Integer.valueOf(5), response.getNextStartIndex());
        
        // Last page
        response = new ArXivSearchResponse(papers, 10, 10, 5, "test");
        assertEquals(Integer.valueOf(3), response.getCurrentPage());
        assertEquals(Integer.valueOf(5), response.getPreviousStartIndex());
        assertNull(response.getNextStartIndex()); // No more results
    }
    
    @Test
    void testArXivSearchResponseSummary() {
        List<ArXivPaper> papers = Arrays.asList(new ArXivPaper(), new ArXivPaper());
        
        ArXivSearchResponse response = new ArXivSearchResponse(papers, 10, 0, 5, "machine learning");
        response.setExecutionTimeMs(150L);
        
        String summary = response.getSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("10"));
        assertTrue(summary.contains("2"));
        assertTrue(summary.contains("machine learning"));
        assertTrue(summary.contains("150ms"));
    }
    
    @Test
    void testArXivPaperToString() {
        ArXivPaper paper = new ArXivPaper();
        paper.setId("2301.12345");
        paper.setTitle("Test Paper");
        paper.setAuthors(Arrays.asList("John Smith"));
        paper.setCategories(Arrays.asList("cs.AI"));
        paper.setSummary("This is a test summary.");
        paper.setArxivUrl("https://arxiv.org/abs/2301.12345");
        paper.setPdfUrl("https://arxiv.org/pdf/2301.12345.pdf");
        
        String toString = paper.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("2301.12345"));
        assertTrue(toString.contains("Test Paper"));
        assertTrue(toString.contains("John Smith"));
        assertTrue(toString.contains("cs.AI"));
        assertTrue(toString.contains("This is a test summary."));
    }
    
    @Test
    void testEqualsAndHashCode() {
        ArXivSearchRequest request1 = ArXivSearchRequest.builder()
                .query("test")
                .maxResults(10)
                .build();
        
        ArXivSearchRequest request2 = ArXivSearchRequest.builder()
                .query("test")
                .maxResults(10)
                .build();
        
        ArXivSearchRequest request3 = ArXivSearchRequest.builder()
                .query("different")
                .maxResults(10)
                .build();
        
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        assertNotEquals(request1.hashCode(), request3.hashCode());
        
        ArXivPaper paper1 = new ArXivPaper();
        paper1.setId("123");
        paper1.setTitle("Test");
        
        ArXivPaper paper2 = new ArXivPaper();
        paper2.setId("123");
        paper2.setTitle("Test");
        
        ArXivPaper paper3 = new ArXivPaper();
        paper3.setId("456");
        paper3.setTitle("Test");
        
        assertEquals(paper1, paper2);
        assertEquals(paper1.hashCode(), paper2.hashCode());
        assertNotEquals(paper1, paper3);
        assertNotEquals(paper1.hashCode(), paper3.hashCode());
    }
}
