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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Search request for MetaSo API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {
    
    /**
     * The search query
     */
    @JsonProperty("q")
    private String query;
    
    /**
     * The search scope, e.g. "webpage", "academic"
     */
    @JsonProperty("scope")
    private String scope = MetaSoConstant.DEFAULT_SCOPE;
    
    /**
     * Whether to include summary in the response
     */
    @JsonProperty("includeSummary")
    private Boolean includeSummary = false;
    
    /**
     * Whether to include raw content in the response
     */
    @JsonProperty("includeRawContent")
    private Boolean includeRawContent = false;
    
    /**
     * Number of results to return
     */
    @JsonProperty("size")
    private Integer size = 10;
    
    // Getters and Setters
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public Boolean getIncludeSummary() {
        return includeSummary;
    }
    
    public void setIncludeSummary(Boolean includeSummary) {
        this.includeSummary = includeSummary;
    }
    
    public Boolean getIncludeRawContent() {
        return includeRawContent;
    }
    
    public void setIncludeRawContent(Boolean includeRawContent) {
        this.includeRawContent = includeRawContent;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
}