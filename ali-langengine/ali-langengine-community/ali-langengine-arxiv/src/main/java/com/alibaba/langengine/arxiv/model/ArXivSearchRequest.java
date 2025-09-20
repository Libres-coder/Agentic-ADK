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
public class ArXivSearchRequest {
    
    /**
     * Search query string
     */
    private String query;
    
    /**
     * Maximum number of results to return
     */
    private Integer maxResults = 10;
    
    /**
     * Starting index for results (for pagination)
     */
    private Integer start = 0;
    
    /**
     * Sort order: relevance, lastUpdatedDate, submittedDate
     */
    private String sortBy = "relevance";
    
    /**
     * Sort direction: ascending, descending
     */
    private String sortOrder = "descending";
    
    /**
     * Filter by subject categories (e.g., cs, math, physics)
     */
    private List<String> categories;
    
    /**
     * Filter by authors
     */
    private List<String> authors;
    
    /**
     * Include abstract in results
     */
    private Boolean includeAbstract = true;
    
    /**
     * Include full text link in results
     */
    private Boolean includeFullTextLink = true;
    
    /**
     * Include PDF download link in results
     */
    private Boolean includePdfLink = true;
    
    /**
     * Date range filter - start date (format: YYYY-MM-DD)
     */
    private String startDate;
    
    /**
     * Date range filter - end date (format: YYYY-MM-DD)
     */
    private String endDate;
    
    /**
     * Constructor with minimal required parameters
     */
    public ArXivSearchRequest(String query) {
        this.query = query;
    }
    
    /**
     * Default constructor
     */
    public ArXivSearchRequest() {
    }
    
    /**
     * Builder pattern for creating search requests
     */
    public static class Builder {
        private ArXivSearchRequest request = new ArXivSearchRequest();
        
        public Builder query(String query) {
            request.setQuery(query);
            return this;
        }
        
        public Builder maxResults(Integer maxResults) {
            request.setMaxResults(maxResults);
            return this;
        }
        
        public Builder start(Integer start) {
            request.setStart(start);
            return this;
        }
        
        public Builder sortBy(String sortBy) {
            request.setSortBy(sortBy);
            return this;
        }
        
        public Builder sortOrder(String sortOrder) {
            request.setSortOrder(sortOrder);
            return this;
        }
        
        public Builder categories(List<String> categories) {
            request.setCategories(categories);
            return this;
        }
        
        public Builder authors(List<String> authors) {
            request.setAuthors(authors);
            return this;
        }
        
        public Builder includeAbstract(Boolean includeAbstract) {
            request.setIncludeAbstract(includeAbstract);
            return this;
        }
        
        public Builder includeFullTextLink(Boolean includeFullTextLink) {
            request.setIncludeFullTextLink(includeFullTextLink);
            return this;
        }
        
        public Builder includePdfLink(Boolean includePdfLink) {
            request.setIncludePdfLink(includePdfLink);
            return this;
        }
        
        public Builder dateRange(String startDate, String endDate) {
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            return this;
        }
        
        public ArXivSearchRequest build() {
            return request;
        }
    }
    
    /**
     * Create a builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
