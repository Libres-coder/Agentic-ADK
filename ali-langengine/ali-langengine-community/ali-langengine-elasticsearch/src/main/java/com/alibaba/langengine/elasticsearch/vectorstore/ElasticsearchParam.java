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
package com.alibaba.langengine.elasticsearch.vectorstore;

import lombok.Data;


@Data
public class ElasticsearchParam {
    
    /**
     * Index configuration parameters
     */
    private IndexParam indexParam = new IndexParam();
    
    /**
     * Field name for page content
     */
    private String fieldNamePageContent = "page_content";
    
    /**
     * Field name for unique ID
     */
    private String fieldNameUniqueId = "content_id";
    
    /**
     * Field name for vector embeddings
     */
    private String fieldNameVector = "vector";
    
    /**
     * Field name for metadata
     */
    private String fieldNameMetadata = "metadata";

    @Data
    public static class IndexParam {
        /**
         * Number of shards for the index
         */
        private int numberOfShards = 1;
        
        /**
         * Number of replicas for the index
         */
        private int numberOfReplicas = 0;
        
        /**
         * Vector dimension
         */
        private int vectorDimension = 1536;
        
        /**
         * Vector similarity function: cosine, dot_product, l2_norm
         */
        private String vectorSimilarity = "cosine";
        
        /**
         * Index for vector field type: int8_hnsw, int4_hnsw, hnsw, flat
         */
        private String vectorIndexType = "hnsw";
        
        /**
         * M parameter for HNSW algorithm
         */
        private int hnswM = 16;
        
        /**
         * ef_construction parameter for HNSW algorithm
         */
        private int hnswEfConstruction = 100;
        
        /**
         * Whether to enable refresh after operations
         */
        private boolean refreshAfterWrite = true;
    }
}
