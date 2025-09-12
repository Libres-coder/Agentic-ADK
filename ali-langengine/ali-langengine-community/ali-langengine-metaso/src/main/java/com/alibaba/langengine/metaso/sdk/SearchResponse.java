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
package com.alibaba.langengine.metaso.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Search response from MetaSo API
 */
public class SearchResponse {
    
    /**
     * The search query
     */
    @JsonProperty("query")
    private String query;
    
    /**
     * Response time in seconds
     */
    @JsonProperty("response_time")
    private Double responseTime;
    
    /**
     * The summary of search results
     */
    @JsonProperty("summary")
    private String summary;
    
    /**
     * The search results
     */
    @JsonProperty("results")
    private List<SearchResult> results;
    
    /**
     * The images from search results
     */
    @JsonProperty("images")
    private List<String> images;
    
    // Getters and Setters
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Double getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(Double responseTime) {
        this.responseTime = responseTime;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public List<SearchResult> getResults() {
        return results;
    }
    
    public void setResults(List<SearchResult> results) {
        this.results = results;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
}