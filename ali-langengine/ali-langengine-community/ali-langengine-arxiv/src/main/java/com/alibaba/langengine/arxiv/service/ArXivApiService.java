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
package com.alibaba.langengine.arxiv.service;

import com.alibaba.langengine.arxiv.model.ArXivPaper;
import com.alibaba.langengine.arxiv.model.ArXivSearchRequest;
import com.alibaba.langengine.arxiv.model.ArXivSearchResponse;
import com.alibaba.langengine.arxiv.sdk.ArXivException;

import java.util.List;


public interface ArXivApiService {
    
    /**
     * Search for papers using the ArXiv API
     *
     * @param request the search request parameters
     * @return the search response containing papers
     * @throws ArXivException if the search fails
     */
    ArXivSearchResponse searchPapers(ArXivSearchRequest request) throws ArXivException;
    
    /**
     * Search for papers with a simple query string
     *
     * @param query the search query
     * @return the search response containing papers
     * @throws ArXivException if the search fails
     */
    ArXivSearchResponse searchPapers(String query) throws ArXivException;
    
    /**
     * Search for papers with query and max results
     *
     * @param query the search query
     * @param maxResults maximum number of results
     * @return the search response containing papers
     * @throws ArXivException if the search fails
     */
    ArXivSearchResponse searchPapers(String query, int maxResults) throws ArXivException;
    
    /**
     * Get a specific paper by its ArXiv ID
     *
     * @param arxivId the ArXiv paper ID (e.g., "2301.12345")
     * @return the paper details
     * @throws ArXivException if the paper retrieval fails
     */
    ArXivPaper getPaper(String arxivId) throws ArXivException;
    
    /**
     * Get multiple papers by their ArXiv IDs
     *
     * @param arxivIds list of ArXiv paper IDs
     * @return list of paper details
     * @throws ArXivException if the paper retrieval fails
     */
    List<ArXivPaper> getPapers(List<String> arxivIds) throws ArXivException;
    
    /**
     * Download PDF for a given paper
     *
     * @param arxivId the ArXiv paper ID
     * @return byte array containing the PDF content
     * @throws ArXivException if the download fails
     */
    byte[] downloadPdf(String arxivId) throws ArXivException;
    
    /**
     * Get the PDF download URL for a paper
     *
     * @param arxivId the ArXiv paper ID
     * @return the PDF download URL
     */
    String getPdfUrl(String arxivId);
    
    /**
     * Get the ArXiv page URL for a paper
     *
     * @param arxivId the ArXiv paper ID
     * @return the ArXiv page URL
     */
    String getArxivUrl(String arxivId);
    
    /**
     * Search papers by category
     *
     * @param category the subject category (e.g., "cs.AI", "math.CO")
     * @param maxResults maximum number of results
     * @return the search response containing papers
     * @throws ArXivException if the search fails
     */
    ArXivSearchResponse searchByCategory(String category, int maxResults) throws ArXivException;
    
    /**
     * Search papers by author
     *
     * @param author the author name
     * @param maxResults maximum number of results
     * @return the search response containing papers
     * @throws ArXivException if the search fails
     */
    ArXivSearchResponse searchByAuthor(String author, int maxResults) throws ArXivException;
    
    /**
     * Get recent papers from a specific category
     *
     * @param category the subject category
     * @param days number of days to look back
     * @param maxResults maximum number of results
     * @return the search response containing recent papers
     * @throws ArXivException if the search fails
     */
    ArXivSearchResponse getRecentPapers(String category, int days, int maxResults) throws ArXivException;
}
