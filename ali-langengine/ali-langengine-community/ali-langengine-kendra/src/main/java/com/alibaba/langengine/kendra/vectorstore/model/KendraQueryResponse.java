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
package com.alibaba.langengine.kendra.vectorstore.model;

import lombok.Data;
import java.util.List;
import java.util.Map;


@Data
public class KendraQueryResponse {

    /**
     * Query ID for tracking
     */
    private String queryId;

    /**
     * List of query results
     */
    private List<KendraResult> results;

    /**
     * Facet results for the query
     */
    private Map<String, Object> facetResults;

    /**
     * Total number of results found
     */
    private Integer totalNumberOfResults;

    /**
     * Warnings from the query
     */
    private List<String> warnings;

    /**
     * Spell corrected query (if applicable)
     */
    private String spellCorrectedQuery;

    /**
     * Featured results summary
     */
    private Map<String, Object> featuredResultsSummary;

    /**
     * Query execution time in milliseconds
     */
    private Long executionTimeMs;

    /**
     * Response metadata
     */
    private Map<String, Object> responseMetadata;
}