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
public class StackOverflowSearchResult {
    
    /**
     * Question ID
     */
    @JsonProperty("question_id")
    private Long questionId;
    
    /**
     * Question title
     */
    private String title;
    
    /**
     * Question body (excerpt)
     */
    private String body;
    
    /**
     * Question URL
     */
    private String link;
    
    /**
     * Question score (upvotes - downvotes)
     */
    private Integer score;
    
    /**
     * View count
     */
    @JsonProperty("view_count")
    private Integer viewCount;
    
    /**
     * Answer count
     */
    @JsonProperty("answer_count")
    private Integer answerCount;
    
    /**
     * Is answered (has accepted answer)
     */
    @JsonProperty("is_answered")
    private Boolean isAnswered;
    
    /**
     * Has accepted answer
     */
    @JsonProperty("accepted_answer_id")
    private Long acceptedAnswerId;
    
    /**
     * Creation date (Unix timestamp)
     */
    @JsonProperty("creation_date")
    private Long creationDate;
    
    /**
     * Last activity date (Unix timestamp)
     */
    @JsonProperty("last_activity_date")
    private Long lastActivityDate;
    
    /**
     * Question tags
     */
    private List<String> tags;
    
    /**
     * Question owner
     */
    private Owner owner;
    
    /**
     * Best answer (if available)
     */
    private Answer bestAnswer;
    
    /**
     * Relevance score (for ranking)
     */
    private Double relevanceScore;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        @JsonProperty("user_id")
        private Long userId;
        
        @JsonProperty("display_name")
        private String displayName;
        
        private Integer reputation;
        
        @JsonProperty("profile_image")
        private String profileImage;
        
        private String link;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Answer {
        @JsonProperty("answer_id")
        private Long answerId;
        
        private String body;
        
        private Integer score;
        
        @JsonProperty("is_accepted")
        private Boolean isAccepted;
        
        @JsonProperty("creation_date")
        private Long creationDate;
        
        private Owner owner;
    }
}
