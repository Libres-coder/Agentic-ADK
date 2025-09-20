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
import com.alibaba.langengine.arxiv.sdk.ArXivConstant;
import com.alibaba.langengine.arxiv.sdk.ArXivClient;
import com.alibaba.langengine.arxiv.sdk.ArXivException;
import com.alibaba.langengine.arxiv.service.ArXivApiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
public class ArXivApiServiceImpl implements ArXivApiService {
    
    private ArXivClient arxivClient;
    private final OkHttpClient httpClient;
    
    /**
     * Default constructor
     */
    public ArXivApiServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.arxivClient = new ArXivClient(httpClient);
    }
    
    /**
     * Constructor with custom ArXiv client
     */
    public ArXivApiServiceImpl(ArXivClient arxivClient) {
        this.arxivClient = arxivClient;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Constructor with custom HTTP client
     */
    public ArXivApiServiceImpl(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.arxivClient = new ArXivClient(httpClient);
    }
    
    /**
     * Clean up resources when service is destroyed
     */
    @PreDestroy
    public void cleanup() {
        if (httpClient != null) {
            try {
                httpClient.dispatcher().executorService().shutdown();
                httpClient.connectionPool().evictAll();
                if (httpClient.cache() != null) {
                    httpClient.cache().close();
                }
            } catch (Exception e) {
                log.warn("Error during HTTP client cleanup: {}", e.getMessage());
            }
        }
    }
    
    @Override
    public ArXivSearchResponse searchPapers(ArXivSearchRequest request) throws ArXivException {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }
        
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        try {
            log.info("Searching ArXiv papers with query: {}", request.getQuery());
            return arxivClient.search(request);
        } catch (ArXivException e) {
            log.error("ArXiv search failed for query '{}': {}", request.getQuery(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during ArXiv search: {}", e.getMessage(), e);
            throw new ArXivException("Search failed due to unexpected error", e);
        }
    }
    
    @Override
    public ArXivSearchResponse searchPapers(String query) throws ArXivException {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        ArXivSearchRequest request = new ArXivSearchRequest(query.trim());
        return searchPapers(request);
    }
    
    @Override
    public ArXivSearchResponse searchPapers(String query, int maxResults) throws ArXivException {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        
        if (maxResults <= 0) {
            throw new IllegalArgumentException("Max results must be positive");
        }
        
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query(query.trim())
                .maxResults(maxResults)
                .build();
        
        return searchPapers(request);
    }
    
    @Override
    public ArXivPaper getPaper(String arxivId) throws ArXivException {
        if (arxivId == null || arxivId.trim().isEmpty()) {
            throw new IllegalArgumentException("ArXiv ID cannot be empty");
        }
        
        // Clean the ArXiv ID (remove version if present)
        String cleanId = cleanArxivId(arxivId.trim());
        
        try {
            log.info("Getting ArXiv paper: {}", cleanId);
            
            // Use ArXiv ID as exact search query
            ArXivSearchRequest request = ArXivSearchRequest.builder()
                    .query("id:" + cleanId)
                    .maxResults(1)
                    .build();
            
            ArXivSearchResponse response = arxivClient.search(request);
            
            if (response.isEmpty()) {
                throw new ArXivException("Paper not found: " + arxivId);
            }
            
            return response.getPapers().get(0);
            
        } catch (ArXivException e) {
            log.error("Failed to get ArXiv paper '{}': {}", arxivId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting ArXiv paper '{}': {}", arxivId, e.getMessage(), e);
            throw new ArXivException("Failed to get paper due to unexpected error", e);
        }
    }
    
    @Override
    public List<ArXivPaper> getPapers(List<String> arxivIds) throws ArXivException {
        if (arxivIds == null || arxivIds.isEmpty()) {
            throw new IllegalArgumentException("ArXiv IDs list cannot be empty");
        }
        
        List<ArXivPaper> papers = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        
        for (String arxivId : arxivIds) {
            try {
                ArXivPaper paper = getPaper(arxivId);
                papers.add(paper);
            } catch (ArXivException e) {
                log.warn("Failed to get paper '{}': {}", arxivId, e.getMessage());
                failedIds.add(arxivId);
            }
        }
        
        if (!failedIds.isEmpty()) {
            log.warn("Failed to retrieve {} papers: {}", failedIds.size(), failedIds);
        }
        
        return papers;
    }
    
    @Override
    public byte[] downloadPdf(String arxivId) throws ArXivException {
        if (arxivId == null || arxivId.trim().isEmpty()) {
            throw new IllegalArgumentException("ArXiv ID cannot be empty");
        }
        
        String cleanId = cleanArxivId(arxivId.trim());
        String pdfUrl = getPdfUrl(cleanId);
        
        try {
            log.info("Downloading PDF for ArXiv paper: {}", cleanId);
            
            Request request = new Request.Builder()
                    .url(pdfUrl)
                    .addHeader("User-Agent", "ArXiv-Java-Client/1.0")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ArXivException("PDF download failed: " + response.code() + " " + response.message());
                }
                
                if (response.body() == null) {
                    throw new ArXivException("PDF download returned empty response");
                }
                
                byte[] pdfContent = response.body().bytes();
                log.info("Successfully downloaded PDF for paper '{}': {} bytes", cleanId, pdfContent.length);
                return pdfContent;
            }
            
        } catch (IOException e) {
            log.error("Error downloading PDF for paper '{}': {}", arxivId, e.getMessage(), e);
            throw new ArXivException("PDF download failed", e);
        }
    }
    
    @Override
    public String getPdfUrl(String arxivId) {
        String cleanId = cleanArxivId(arxivId);
        return "https://arxiv.org/pdf/" + cleanId + ".pdf";
    }
    
    @Override
    public String getArxivUrl(String arxivId) {
        String cleanId = cleanArxivId(arxivId);
        return "https://arxiv.org/abs/" + cleanId;
    }
    
    @Override
    public ArXivSearchResponse searchByCategory(String category, int maxResults) throws ArXivException {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        if (maxResults <= 0) {
            throw new IllegalArgumentException("Max results must be positive");
        }
        
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("cat:" + category.trim())
                .maxResults(maxResults)
                .sortBy("lastUpdatedDate")
                .sortOrder("descending")
                .build();
        
        return searchPapers(request);
    }
    
    @Override
    public ArXivSearchResponse searchByAuthor(String author, int maxResults) throws ArXivException {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author name cannot be empty");
        }
        
        if (maxResults <= 0) {
            throw new IllegalArgumentException("Max results must be positive");
        }
        
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("au:\"" + author.trim() + "\"")
                .maxResults(maxResults)
                .sortBy("lastUpdatedDate")
                .sortOrder("descending")
                .build();
        
        return searchPapers(request);
    }
    
    @Override
    public ArXivSearchResponse getRecentPapers(String category, int days, int maxResults) throws ArXivException {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        if (maxResults <= 0) {
            throw new IllegalArgumentException("Max results must be positive");
        }
        
        // Calculate date range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        ArXivSearchRequest request = ArXivSearchRequest.builder()
                .query("cat:" + category.trim())
                .maxResults(maxResults)
                .sortBy("submittedDate")
                .sortOrder("descending")
                .dateRange(startDate.format(formatter), endDate.format(formatter))
                .build();
        
        return searchPapers(request);
    }
    
    /**
     * Clean ArXiv ID by removing version and prefix
     */
    private String cleanArxivId(String arxivId) {
        if (arxivId == null) {
            return null;
        }
        
        String cleaned = arxivId.trim();
        
        // Remove arxiv: prefix if present
        if (cleaned.toLowerCase().startsWith("arxiv:")) {
            cleaned = cleaned.substring(6);
        }
        
        // Remove version (e.g., v1, v2) if present
        int vIndex = cleaned.lastIndexOf('v');
        if (vIndex > 0 && vIndex < cleaned.length() - 1) {
            String versionPart = cleaned.substring(vIndex + 1);
            if (versionPart.matches("\\d+")) {
                cleaned = cleaned.substring(0, vIndex);
            }
        }
        
        return cleaned;
    }
    
    /**
     * URL encode a string
     * @param value string to encode
     * @return encoded string
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Set ArXiv client for testing
     * @param client ArXiv client
     */
    public void setArXivClient(ArXivClient client) {
        this.arxivClient = client;
    }
    
    /**
     * Build search URL for testing
     * @param request search request
     * @return search URL
     */
    public String buildSearchUrl(ArXivSearchRequest request) {
        StringBuilder url = new StringBuilder(ArXivConstant.ARXIV_API_BASE_URL);
        url.append("?search_query=all%3A").append(urlEncode(request.getQuery()));
        url.append("&start=").append(request.getStart());
        url.append("&max_results=").append(request.getMaxResults());
        url.append("&sortBy=").append(request.getSortBy());
        url.append("&sortOrder=").append(request.getSortOrder());
        return url.toString();
    }
}
