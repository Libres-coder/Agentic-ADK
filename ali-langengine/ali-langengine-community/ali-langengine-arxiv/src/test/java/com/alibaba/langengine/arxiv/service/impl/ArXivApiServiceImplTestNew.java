package com.alibaba.langengine.arxiv.service.impl;

import com.alibaba.langengine.arxiv.config.ArXivConfiguration;
import com.alibaba.langengine.arxiv.model.ArXivSearchRequest;
import com.alibaba.langengine.arxiv.model.ArXivSearchResponse;
import com.alibaba.langengine.arxiv.sdk.ArXivClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class ArXivApiServiceImplTestNew {

    @Mock
    private ArXivClient mockClient;
    
    private ArXivApiServiceImpl service;
    private ArXivConfiguration config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = new ArXivConfiguration();
        service = new ArXivApiServiceImpl(config);
        service.setArXivClient(mockClient);
    }

    @Test
    void testGetArxivUrl() {
        String arxivUrl = service.getArxivUrl("2301.12345");
        assertNotNull(arxivUrl);
        assertTrue(arxivUrl.contains("arxiv.org/abs/"));
        assertEquals("https://arxiv.org/abs/2301.12345", arxivUrl);
    }

    @Test
    void testSearchPapersWithMockClient() throws Exception {
        // 创建Mock响应
        ArXivSearchResponse mockResponse = new ArXivSearchResponse();
        mockResponse.setTotalResults(0);
        
        when(mockClient.search(any(ArXivSearchRequest.class))).thenReturn(mockResponse);
        
        ArXivSearchRequest request = new ArXivSearchRequest();
        request.setQuery("test");
        request.setMaxResults(10);
        
        ArXivSearchResponse result = service.searchPapers(request);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalResults());
    }

    @Test
    void testBuildSearchUrl() {
        ArXivSearchRequest request = new ArXivSearchRequest();
        request.setQuery("machine learning");
        request.setMaxResults(10);
        request.setStart(0);
        
        String url = service.buildSearchUrl(request);
        
        assertNotNull(url);
        assertTrue(url.contains("machine%20learning"));
        assertTrue(url.contains("max_results=10"));
        assertTrue(url.contains("start=0"));
    }

    @Test  
    void testInvalidQueries() {
        // Test empty query
        ArXivSearchRequest emptyRequest = new ArXivSearchRequest();
        emptyRequest.setQuery("");
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers(emptyRequest);
        });
        
        // Test null query
        ArXivSearchRequest nullRequest = new ArXivSearchRequest();
        nullRequest.setQuery(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.searchPapers(nullRequest);
        });
    }
}
