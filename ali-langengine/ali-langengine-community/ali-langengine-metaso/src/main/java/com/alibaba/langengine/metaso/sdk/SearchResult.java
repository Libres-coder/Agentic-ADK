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

/**
 * Search result item from MetaSo API
 */
public class SearchResult {
    
    /**
     * The title of the search result
     */
    @JsonProperty("title")
    private String title;
    
    /**
     * The URL of the search result
     */
    @JsonProperty("url")
    private String url;
    
    /**
     * The content of the search result
     */
    @JsonProperty("content")
    private String content;
    
    /**
     * The score of the search result
     */
    @JsonProperty("score")
    private Double score;
    
    /**
     * The raw content of the search result
     */
    @JsonProperty("raw_content")
    private String rawContent;
    
    // Getters and Setters
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public String getRawContent() {
        return rawContent;
    }
    
    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }
}