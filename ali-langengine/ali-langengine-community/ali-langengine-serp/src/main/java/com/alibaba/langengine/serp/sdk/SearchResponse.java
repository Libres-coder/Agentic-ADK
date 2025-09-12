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
package com.alibaba.langengine.serp.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class SearchResponse {
    @JsonProperty("search_metadata")
    private Map<String, Object> searchMetadata;

    @JsonProperty("search_parameters")
    private Map<String, Object> searchParameters;

    @JsonProperty("search_information")
    private Map<String, Object> searchInformation;

    @JsonProperty("organic_results")
    private List<SearchResult> organicResults;

    @JsonProperty("related_questions")
    private List<Map<String, Object>> relatedQuestions;

    @JsonProperty("related_searches")
    private List<Map<String, Object>> relatedSearches;

    // Getters and Setters
    public Map<String, Object> getSearchMetadata() {
        return searchMetadata;
    }

    public void setSearchMetadata(Map<String, Object> searchMetadata) {
        this.searchMetadata = searchMetadata;
    }

    public Map<String, Object> getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(Map<String, Object> searchParameters) {
        this.searchParameters = searchParameters;
    }

    public Map<String, Object> getSearchInformation() {
        return searchInformation;
    }

    public void setSearchInformation(Map<String, Object> searchInformation) {
        this.searchInformation = searchInformation;
    }

    public List<SearchResult> getOrganicResults() {
        return organicResults;
    }

    public void setOrganicResults(List<SearchResult> organicResults) {
        this.organicResults = organicResults;
    }

    public List<Map<String, Object>> getRelatedQuestions() {
        return relatedQuestions;
    }

    public void setRelatedQuestions(List<Map<String, Object>> relatedQuestions) {
        this.relatedQuestions = relatedQuestions;
    }

    public List<Map<String, Object>> getRelatedSearches() {
        return relatedSearches;
    }

    public void setRelatedSearches(List<Map<String, Object>> relatedSearches) {
        this.relatedSearches = relatedSearches;
    }
}