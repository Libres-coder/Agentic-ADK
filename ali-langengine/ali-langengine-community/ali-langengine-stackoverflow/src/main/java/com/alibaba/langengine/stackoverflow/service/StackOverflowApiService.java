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
package com.alibaba.langengine.stackoverflow.service;

import com.alibaba.langengine.stackoverflow.model.StackOverflowApiResponse;
import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchRequest;
import com.alibaba.langengine.stackoverflow.model.StackOverflowSearchResult;

import java.util.List;


public interface StackOverflowApiService {
    
    /**
     * Search questions on Stack Overflow
     * 
     * @param request Search request parameters
     * @return List of search results
     * @throws Exception if search fails
     */
    List<StackOverflowSearchResult> searchQuestions(StackOverflowSearchRequest request) throws Exception;
    
    /**
     * Get detailed question information with answers
     * 
     * @param questionId Question ID
     * @param includeAnswers Whether to include answers
     * @return Detailed question information
     * @throws Exception if retrieval fails
     */
    StackOverflowSearchResult getQuestion(Long questionId, boolean includeAnswers) throws Exception;
    
    /**
     * Search using Stack Exchange API
     * 
     * @param request Search request parameters
     * @return API response
     * @throws Exception if API call fails
     */
    StackOverflowApiResponse searchWithApi(StackOverflowSearchRequest request) throws Exception;
    
    /**
     * Search using web scraping (fallback method)
     * 
     * @param request Search request parameters
     * @return List of search results
     * @throws Exception if scraping fails
     */
    List<StackOverflowSearchResult> searchWithScraping(StackOverflowSearchRequest request) throws Exception;
    
    /**
     * Check if API is available and functional
     * 
     * @return true if API is available, false otherwise
     */
    boolean isApiAvailable();
    
    /**
     * Get remaining API quota
     * 
     * @return remaining quota, -1 if unknown
     */
    int getRemainingQuota();
}
