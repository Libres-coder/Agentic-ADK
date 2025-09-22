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
package com.alibaba.langengine.tinkerpop.vectorstore.service;

import lombok.Data;

import java.util.List;


@Data
public class TinkerPopQueryRequest {

    /**
     * Collection name to query
     */
    private String collectionName;

    /**
     * Query text for embedding generation
     */
    private String queryText;

    /**
     * Query embedding vector
     */
    private List<Double> queryEmbedding;

    /**
     * Number of results to return
     */
    private int k;

    /**
     * Maximum distance value for filtering results
     */
    private Double maxDistance;

    /**
     * Query type identifier
     */
    private Integer type;

    public TinkerPopQueryRequest() {
    }

    public TinkerPopQueryRequest(String collectionName, String queryText, int k) {
        this.collectionName = collectionName;
        this.queryText = queryText;
        this.k = k;
    }

    public TinkerPopQueryRequest(String collectionName, List<Double> queryEmbedding, int k) {
        this.collectionName = collectionName;
        this.queryEmbedding = queryEmbedding;
        this.k = k;
    }
}