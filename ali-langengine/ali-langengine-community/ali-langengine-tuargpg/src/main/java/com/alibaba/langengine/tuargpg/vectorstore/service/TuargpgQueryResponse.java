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
package com.alibaba.langengine.tuargpg.vectorstore.service;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TuargpgQueryResponse {

    private List<TuargpgVectorRecord> records;

    private Integer totalCount;

    @Data
    public static class TuargpgVectorRecord {
        private String id;
        private String content;
        private List<Float> vector;
        private Map<String, Object> metadata;
        private Double distance;
        private Double score;
    }
}