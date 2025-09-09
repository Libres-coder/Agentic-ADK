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
package com.alibaba.langengine.stackoverflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowApiResponse {
    
    /**
     * List of search results
     */
    private List<StackOverflowSearchResult> items;
    
    /**
     * Whether there are more results available
     */
    @JsonProperty("has_more")
    private Boolean hasMore;
    
    /**
     * Quota remaining for API calls
     */
    @JsonProperty("quota_remaining")
    private Integer quotaRemaining;
    
    /**
     * Quota maximum for API calls
     */
    @JsonProperty("quota_max")
    private Integer quotaMax;
    
    /**
     * Error information (if any)
     */
    @JsonProperty("error_message")
    private String errorMessage;
    
    /**
     * Error ID (if any)
     */
    @JsonProperty("error_id")
    private Integer errorId;
    
    /**
     * Error name (if any)
     */
    @JsonProperty("error_name")
    private String errorName;
    
    /**
     * Total count of available results
     */
    private Integer total;
    
    /**
     * Page number
     */
    private Integer page;
    
    /**
     * Page size
     */
    @JsonProperty("page_size")
    private Integer pageSize;
}
