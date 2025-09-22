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
package com.alibaba.langengine.rockset.vectorstore;

import lombok.Data;


@Data
public class RocksetParam {
        
    private InitParam initParam = new InitParam();
    private String fieldNamePageContent = "page_content";
    private String fieldNameUniqueId = "content_id";
    private String fieldMeta = "meta_data";
    private String fieldNameVector = "vector";

    @Data
    public static class InitParam {
        private String workspace = "commons";
        private String collectionName = "langengine_rockset_collection";
        private String vectorDistance = "cosine";
        private Integer dimension = 1536;
        private String indexType = "hnsw";
        private Integer maxConnections = 16;
        private Integer efConstruction = 200;
        private Integer efSearch = 10;
        private String description = "LangEngine Rockset Vector Store";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RocksetParam param = new RocksetParam();

        public Builder workspace(String workspace) {
            param.getInitParam().setWorkspace(workspace);
            return this;
        }

        public Builder collectionName(String collectionName) {
            param.getInitParam().setCollectionName(collectionName);
            return this;
        }

        public Builder vectorDistance(String vectorDistance) {
            param.getInitParam().setVectorDistance(vectorDistance);
            return this;
        }

        public Builder dimension(Integer dimension) {
            param.getInitParam().setDimension(dimension);
            return this;
        }

        public Builder indexType(String indexType) {
            param.getInitParam().setIndexType(indexType);
            return this;
        }

        public Builder maxConnections(Integer maxConnections) {
            param.getInitParam().setMaxConnections(maxConnections);
            return this;
        }

        public Builder efConstruction(Integer efConstruction) {
            param.getInitParam().setEfConstruction(efConstruction);
            return this;
        }

        public Builder efSearch(Integer efSearch) {
            param.getInitParam().setEfSearch(efSearch);
            return this;
        }

        public Builder description(String description) {
            param.getInitParam().setDescription(description);
            return this;
        }

        public Builder fieldNamePageContent(String fieldNamePageContent) {
            param.setFieldNamePageContent(fieldNamePageContent);
            return this;
        }

        public Builder fieldNameUniqueId(String fieldNameUniqueId) {
            param.setFieldNameUniqueId(fieldNameUniqueId);
            return this;
        }

        public Builder fieldMeta(String fieldMeta) {
            param.setFieldMeta(fieldMeta);
            return this;
        }

        public Builder fieldNameVector(String fieldNameVector) {
            param.setFieldNameVector(fieldNameVector);
            return this;
        }

        public RocksetParam build() {
            return param;
        }
    }
}