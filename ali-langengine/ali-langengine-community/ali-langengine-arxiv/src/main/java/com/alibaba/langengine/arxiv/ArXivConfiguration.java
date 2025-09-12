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
package com.alibaba.langengine.arxiv;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

import static com.alibaba.langengine.arxiv.sdk.ArXivConstant.DEFAULT_BASE_URL;


public class ArXivConfiguration {
    
    /**
     * ArXiv API base URL, defaults to the constant BASE_URL if not configured
     */
    public static final String ARXIV_API_URL = WorkPropertiesUtils.get("arxiv_api_url", DEFAULT_BASE_URL);
    
    /**
     * Default maximum number of results per search
     */
    public static final int DEFAULT_MAX_RESULTS = parseIntWithDefault(
        WorkPropertiesUtils.get("arxiv_max_results", "10"), 10);
    
    /**
     * Default sort order for search results (relevance, lastUpdatedDate, submittedDate)
     */
    public static final String DEFAULT_SORT_ORDER = WorkPropertiesUtils.get("arxiv_sort_order", "relevance");
    
    /**
     * Default sort direction (ascending, descending)
     */
    public static final String DEFAULT_SORT_DIRECTION = WorkPropertiesUtils.get("arxiv_sort_direction", "descending");
    
    /**
     * Default timeout for HTTP requests in seconds
     */
    public static final int DEFAULT_TIMEOUT = parseIntWithDefault(
        WorkPropertiesUtils.get("arxiv_timeout", "30"), 30);
    
    /**
     * Maximum allowed results per search request
     */
    public static final int MAX_ALLOWED_RESULTS = 100;
    
    /**
     * Private constructor to prevent instantiation
     */
    private ArXivConfiguration() {
        throw new UnsupportedOperationException("This is a configuration class and cannot be instantiated");
    }
    
    /**
     * Get the default max results as integer with bounds checking
     */
    public static int getDefaultMaxResults() {
        return Math.min(DEFAULT_MAX_RESULTS, MAX_ALLOWED_RESULTS);
    }
    
    /**
     * Get the default timeout as integer
     */
    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }
    
    /**
     * Get the default sort order
     */
    public static String getDefaultSortOrder() {
        return DEFAULT_SORT_ORDER;
    }
    
    /**
     * Get the default sort direction
     */
    public static String getDefaultSortDirection() {
        return DEFAULT_SORT_DIRECTION;
    }
    
    /**
     * Helper method to parse integer with default fallback
     */
    private static int parseIntWithDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
