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
package com.alibaba.langengine.vertexaivectorsearch.vectorstore;

import lombok.Data;


@Data
public class VertexAiVectorSearchParam {

    /**
     * Field name for unique ID
     */
    private String fieldNameUniqueId = "id";

    /**
     * Field name for embedding vector
     */
    private String fieldNameEmbedding = "embedding";

    /**
     * Field name for page content
     */
    private String fieldNamePageContent = "content";

    /**
     * Field name for metadata
     */
    private String fieldNameMetadata = "metadata";

    /**
     * Vector search index name
     */
    private String indexDisplayName = "vector_search_index";

    /**
     * Vector search endpoint name
     */
    private String endpointDisplayName = "vector_search_endpoint";

    /**
     * Number of neighbors to return in similarity search
     */
    private int neighborsCount = 10;

    /**
     * Initialization parameters for creating index
     */
    private IndexInitParam indexInitParam = new IndexInitParam();

    @Data
    public static class IndexInitParam {

        /**
         * Embedding vector dimension
         */
        private int dimensions = 1536;

        /**
         * Distance measure type for vector search
         * Supported values: DOT_PRODUCT_DISTANCE, COSINE_DISTANCE, SQUARED_L2_DISTANCE
         */
        private String distanceMeasureType = "COSINE_DISTANCE";

        /**
         * Algorithm configuration for approximate nearest neighbor
         */
        private String algorithmConfig = "TREE_AH";

        /**
         * Number of leaf nodes to search
         */
        private int leafNodeEmbeddingCount = 500;

        /**
         * Fraction of the in-memory hnsw index that is loaded into GPU memory
         */
        private double fractionLeafNodesToSearchPercent = 0.1;

        /**
         * Whether to enable auto scaling for the deployed index
         */
        private boolean enableAutoScaling = true;

        /**
         * Minimum number of replicas for auto scaling
         */
        private int minReplicaCount = 1;

        /**
         * Maximum number of replicas for auto scaling
         */
        private int maxReplicaCount = 10;

    }

}
