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
 * QA request for MetaSo API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QARequest {
    
    /**
     * The question to ask
     */
    @JsonProperty("q")
    private String question;
    
    /**
     * The search scope, e.g. "webpage", "academic"
     */
    @JsonProperty("scope")
    private String scope = MetaSoConstant.DEFAULT_SCOPE;
    
    /**
     * Number of results to return
     */
    @JsonProperty("size")
    private Integer size = 10;
    
    // Getters and Setters
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getScope() {
        return scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
}