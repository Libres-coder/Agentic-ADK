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
public class TuargpgQueryRequest {

    private List<Float> queryVector;

    private Integer topK = 10;

    private Double maxDistance;

    private Map<String, Object> metadataFilter;

    private String whereClause;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TuargpgQueryRequest request = new TuargpgQueryRequest();

        public Builder queryVector(List<Float> queryVector) {
            request.setQueryVector(queryVector);
            return this;
        }

        public Builder topK(Integer topK) {
            request.setTopK(topK);
            return this;
        }

        public Builder maxDistance(Double maxDistance) {
            request.setMaxDistance(maxDistance);
            return this;
        }

        public Builder metadataFilter(Map<String, Object> metadataFilter) {
            request.setMetadataFilter(metadataFilter);
            return this;
        }

        public Builder whereClause(String whereClause) {
            request.setWhereClause(whereClause);
            return this;
        }

        public TuargpgQueryRequest build() {
            return request;
        }
    }
}