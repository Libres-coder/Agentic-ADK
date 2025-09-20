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

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class StackOverflowSearchRequest {
    
    /**
     * Search query/keywords
     */
    private String query;
    
    /**
     * Tags to filter by
     */
    private List<String> tags;
    
    /**
     * Sort order (activity, votes, creation, relevance)
     */
    private String sort;
    
    /**
     * Sort order direction (asc, desc)
     */
    private String order;
    
    /**
     * Maximum number of results
     */
    private Integer pageSize;
    
    /**
     * Page number (for pagination)
     */
    private Integer page;
    
    /**
     * Minimum score filter
     */
    private Integer minScore;
    
    /**
     * Maximum score filter
     */
    private Integer maxScore;
    
    /**
     * Only answered questions
     */
    private Boolean answeredOnly;
    
    /**
     * Only questions with accepted answers
     */
    private Boolean acceptedAnswerOnly;
    
    /**
     * Site to search (stackoverflow, superuser, etc.)
     */
    private String site;
    
    /**
     * Date from (Unix timestamp)
     */
    private Long fromDate;
    
    /**
     * Date to (Unix timestamp)
     */
    private Long toDate;
    
    /**
     * Include question body in response
     */
    private Boolean includeBody;
    
    /**
     * Include answers in response
     */
    private Boolean includeAnswers;
}
