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
package com.alibaba.langengine.deeplake.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class DeepLakeQueryRequest {

    @JsonProperty("query_vector")
    private List<Float> queryVector;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("distance_threshold")
    private Double distanceThreshold;

    @JsonProperty("filter")
    private Map<String, Object> filter;

    @JsonProperty("include_metadata")
    private Boolean includeMetadata = true;

    @JsonProperty("include_values")
    private Boolean includeValues = true;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DeepLakeQueryRequest request = new DeepLakeQueryRequest();

        public Builder queryVector(List<Float> queryVector) {
            request.setQueryVector(queryVector);
            return this;
        }

        public Builder topK(Integer topK) {
            request.setTopK(topK);
            return this;
        }

        public Builder distanceThreshold(Double distanceThreshold) {
            request.setDistanceThreshold(distanceThreshold);
            return this;
        }

        public Builder filter(Map<String, Object> filter) {
            request.setFilter(filter);
            return this;
        }

        public Builder includeMetadata(Boolean includeMetadata) {
            request.setIncludeMetadata(includeMetadata);
            return this;
        }

        public Builder includeValues(Boolean includeValues) {
            request.setIncludeValues(includeValues);
            return this;
        }

        public DeepLakeQueryRequest build() {
            return request;
        }
    }
}
