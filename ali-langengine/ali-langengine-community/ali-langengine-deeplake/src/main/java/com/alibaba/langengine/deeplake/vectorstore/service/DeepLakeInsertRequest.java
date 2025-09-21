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
public class DeepLakeInsertRequest {

    @JsonProperty("vectors")
    private List<DeepLakeVector> vectors;

    @JsonProperty("namespace")
    private String namespace;

    @Data
    public static class DeepLakeVector {
        @JsonProperty("id")
        private String id;

        @JsonProperty("values")
        private List<Float> values;

        @JsonProperty("metadata")
        private Map<String, Object> metadata;

        @JsonProperty("content")
        private String content;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private DeepLakeVector vector = new DeepLakeVector();

            public Builder id(String id) {
                vector.setId(id);
                return this;
            }

            public Builder values(List<Float> values) {
                vector.setValues(values);
                return this;
            }

            public Builder metadata(Map<String, Object> metadata) {
                vector.setMetadata(metadata);
                return this;
            }

            public Builder content(String content) {
                vector.setContent(content);
                return this;
            }

            public DeepLakeVector build() {
                return vector;
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DeepLakeInsertRequest request = new DeepLakeInsertRequest();

        public Builder vectors(List<DeepLakeVector> vectors) {
            request.setVectors(vectors);
            return this;
        }

        public Builder namespace(String namespace) {
            request.setNamespace(namespace);
            return this;
        }

        public DeepLakeInsertRequest build() {
            return request;
        }
    }
}
