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
import java.util.Map;


@Data
public class TinkerPopQueryResponse {

    /**
     * Document IDs
     */
    private List<String> ids;

    /**
     * Document content texts
     */
    private List<String> texts;

    /**
     * Similarity distances/scores
     */
    private List<Double> distances;

    /**
     * Document metadata
     */
    private List<Map<String, Object>> metadatas;

    /**
     * Query execution time in milliseconds
     */
    private Long executionTime;

    public TinkerPopQueryResponse() {
    }

    public TinkerPopQueryResponse(List<String> ids, List<String> texts,
                                  List<Double> distances, List<Map<String, Object>> metadatas) {
        this.ids = ids;
        this.texts = texts;
        this.distances = distances;
        this.metadatas = metadatas;
    }
}