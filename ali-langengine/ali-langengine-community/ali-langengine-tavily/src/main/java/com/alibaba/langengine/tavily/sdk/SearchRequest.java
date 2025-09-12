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
package com.alibaba.langengine.tavily.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {
    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("query")
    private String query;

    @JsonProperty("search_depth")
    private String searchDepth; // "basic" or "advanced"

    @JsonProperty("max_results")
    private Integer maxResults;

    @JsonProperty("include_answer")
    private Boolean includeAnswer;

    @JsonProperty("include_raw_content")
    private Boolean includeRawContent;

    @JsonProperty("include_images")
    private Boolean includeImages;

    @JsonProperty("filter")
    private String filter;

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchDepth() {
        return searchDepth;
    }

    public void setSearchDepth(String searchDepth) {
        this.searchDepth = searchDepth;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Boolean getIncludeAnswer() {
        return includeAnswer;
    }

    public void setIncludeAnswer(Boolean includeAnswer) {
        this.includeAnswer = includeAnswer;
    }

    public Boolean getIncludeRawContent() {
        return includeRawContent;
    }

    public void setIncludeRawContent(Boolean includeRawContent) {
        this.includeRawContent = includeRawContent;
    }

    public Boolean getIncludeImages() {
        return includeImages;
    }

    public void setIncludeImages(Boolean includeImages) {
        this.includeImages = includeImages;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}

