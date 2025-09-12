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
package com.alibaba.langengine.arxiv.sdk;


public final class ArXivConstant {
    
    /**
     * Private constructor to prevent instantiation
     */
    private ArXivConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    /**
     * The base URL for the ArXiv API
     */
    public static final String DEFAULT_BASE_URL = "http://export.arxiv.org/api/";
    
    /**
     * The endpoint for querying papers
     */
    public static final String QUERY_ENDPOINT = "query";
    
    /**
     * The default timeout in seconds for API requests
     */
    public static final int DEFAULT_TIMEOUT = 30;
    
    /**
     * Maximum results per request allowed by ArXiv API
     */
    public static final int MAX_RESULTS_LIMIT = 100;
    
    /**
     * ArXiv API base URL
     */
    public static final String ARXIV_API_BASE_URL = "http://export.arxiv.org/api/query";
    
    /**
     * ArXiv abstract base URL
     */
    public static final String ARXIV_ABSTRACT_BASE_URL = "https://arxiv.org/abs/";
    
    /**
     * ArXiv PDF base URL
     */
    public static final String ARXIV_PDF_BASE_URL = "https://arxiv.org/pdf/";
    
    /**
     * Default max results per query
     */
    public static final int DEFAULT_MAX_RESULTS = 10;
    
    /**
     * Default start index
     */
    public static final int DEFAULT_START = 0;
    
    /**
     * Default sort by
     */
    public static final String DEFAULT_SORT_BY = "relevance";
    
    /**
     * Default sort order
     */
    public static final String DEFAULT_SORT_ORDER = "descending";
    
    /**
     * ArXiv namespace for parsing XML responses
     */
    public static final String ARXIV_NAMESPACE = "http://www.w3.org/2005/Atom";
    
    /**
     * OpenSearch namespace for parsing XML responses
     */
    public static final String OPENSEARCH_NAMESPACE = "http://a9.com/-/spec/opensearch/1.1/";
    
    /**
     * Sort orders supported by ArXiv API
     */
    public enum SortOrder {
        RELEVANCE("relevance"),
        LAST_UPDATED_DATE("lastUpdatedDate"),
        SUBMITTED_DATE("submittedDate");

        private final String value;

        SortOrder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    
    /**
     * Sort directions supported by ArXiv API
     */
    public enum SortDirection {
        ASCENDING("ascending"),
        DESCENDING("descending");

        private final String value;

        SortDirection(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    
    /**
     * Common ArXiv subject categories
     */
    public enum Categories {
        COMPUTER_SCIENCE("cs"),
        MATHEMATICS("math"),
        PHYSICS("physics"),
        QUANTITATIVE_BIOLOGY("q-bio"),
        QUANTITATIVE_FINANCE("q-fin"),
        STATISTICS("stat"),
        ELECTRICAL_ENGINEERING("eess"),
        ECONOMICS("econ");

        private final String value;

        Categories(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
