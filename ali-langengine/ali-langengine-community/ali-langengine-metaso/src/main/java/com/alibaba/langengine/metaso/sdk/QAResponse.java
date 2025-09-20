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
 * QA response from MetaSo API
 */
public class QAResponse {
    
    /**
     * The question
     */
    @JsonProperty("question")
    private String question;
    
    /**
     * The answer
     */
    @JsonProperty("answer")
    private String answer;
    
    /**
     * Response time in seconds
     */
    @JsonProperty("response_time")
    private Double responseTime;
    
    // Getters and Setters
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public Double getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(Double responseTime) {
        this.responseTime = responseTime;
    }
}