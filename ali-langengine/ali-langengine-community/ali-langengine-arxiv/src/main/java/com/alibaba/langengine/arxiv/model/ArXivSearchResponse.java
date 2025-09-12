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

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode
public class ArXivSearchResponse {
    
    /**
     * List of papers returned by the search
     */
    private List<ArXivPaper> papers;
    
    /**
     * Total number of results available (may be larger than returned papers)
     */
    private Integer totalResults;
    
    /**
     * Starting index of this result set
     */
    private Integer startIndex;
    
    /**
     * Number of results per page requested
     */
    private Integer itemsPerPage;
    
    /**
     * The original search query
     */
    private String query;
    
    /**
     * Search execution time in milliseconds
     */
    private Long executionTimeMs;
    
    /**
     * Whether there are more results available
     */
    private Boolean hasMoreResults;
    
    /**
     * Error message if search failed
     */
    private String errorMessage;
    
    /**
     * Constructor for successful response
     */
    public ArXivSearchResponse(List<ArXivPaper> papers, Integer totalResults, 
                               Integer startIndex, Integer itemsPerPage, String query) {
        this.papers = papers;
        this.totalResults = totalResults;
        this.startIndex = startIndex;
        this.itemsPerPage = itemsPerPage;
        this.query = query;
        this.hasMoreResults = (startIndex + papers.size()) < totalResults;
    }
    
    /**
     * Constructor for error response
     */
    public ArXivSearchResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.papers = null;
        this.totalResults = 0;
        this.hasMoreResults = false;
    }
    
    /**
     * Default constructor
     */
    public ArXivSearchResponse() {
    }
    
    /**
     * Check if the search was successful
     */
    public boolean isSuccessful() {
        return errorMessage == null && papers != null;
    }
    
    /**
     * Get the number of papers returned in this response
     */
    public int getReturnedCount() {
        return papers != null ? papers.size() : 0;
    }
    
    /**
     * Check if this is an empty result set
     */
    public boolean isEmpty() {
        return papers == null || papers.isEmpty();
    }
    
    /**
     * Get the next start index for pagination
     */
    public Integer getNextStartIndex() {
        if (!hasMoreResults || startIndex == null || itemsPerPage == null) {
            return null;
        }
        return startIndex + itemsPerPage;
    }
    
    /**
     * Get the previous start index for pagination
     */
    public Integer getPreviousStartIndex() {
        if (startIndex == null || startIndex == 0 || itemsPerPage == null) {
            return null;
        }
        return Math.max(0, startIndex - itemsPerPage);
    }
    
    /**
     * Get current page number (1-based)
     */
    public Integer getCurrentPage() {
        if (startIndex == null || itemsPerPage == null || itemsPerPage == 0) {
            return 1;
        }
        return (startIndex / itemsPerPage) + 1;
    }
    
    /**
     * Get total number of pages
     */
    public Integer getTotalPages() {
        if (totalResults == null || itemsPerPage == null || itemsPerPage == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalResults / itemsPerPage);
    }
    
    /**
     * Create a summary string of the search results
     */
    public String getSummary() {
        if (!isSuccessful()) {
            return "Search failed: " + errorMessage;
        }
        
        if (isEmpty()) {
            return "No papers found for query: " + query;
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Found ").append(totalResults).append(" papers");
        
        if (totalResults > getReturnedCount()) {
            summary.append(" (showing ").append(getReturnedCount()).append(")");
        }
        
        summary.append(" for query: ").append(query);
        
        if (executionTimeMs != null) {
            summary.append(" (").append(executionTimeMs).append("ms)");
        }
        
        return summary.toString();
    }
    
    /**
     * Get formatted string representation
     */
    @Override
    public String toString() {
        if (!isSuccessful()) {
            return "ArXivSearchResponse{error='" + errorMessage + "'}";
        }
        
        return "ArXivSearchResponse{" +
                "papers=" + getReturnedCount() +
                ", totalResults=" + totalResults +
                ", startIndex=" + startIndex +
                ", query='" + query + '\'' +
                ", executionTime=" + executionTimeMs + "ms" +
                '}';
    }
}
