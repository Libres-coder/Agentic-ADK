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
package com.alibaba.langengine.marqo.vectorstore;

import lombok.Data;


@Data
public class MarqoParam {
    private InitParam initParam = new InitParam();
    private String fieldNamePageContent = "page_content";
    private String fieldNameUniqueId = "content_id";
    private String fieldMeta = "meta_data";

    @Data
    public static class InitParam {
        /**
         * The model used for embedding documents and queries
         */
        private String model = "hf/all_datasets_v4_MiniLM-L6";
        
        /**
         * The distance metric for vector similarity
         */
        private String metric = "cosine";
        
        /**
         * Number of vectors to use for creating the index
         */
        private Integer numberOfVectors = 1000;
        
        /**
         * Number of shards for the index
         */
        private Integer numberOfShards = 1;
        
        /**
         * Number of replicas for the index
         */
        private Integer numberOfReplicas = 0;
        
        /**
         * Treat URLs as images for multi-modal search
         */
        private Boolean treatUrlsAndPointersAsImages = false;
        
        /**
         * Normalize embeddings
         */
        private Boolean normalizeEmbeddings = true;
        
        /**
         * Text preprocessing settings
         */
        private TextPreprocessing textPreprocessing = new TextPreprocessing();
        
        /**
         * Image preprocessing settings
         */
        private ImagePreprocessing imagePreprocessing = new ImagePreprocessing();
    }

    @Data
    public static class TextPreprocessing {
        /**
         * Split length for text chunks
         */
        private Integer splitLength = 2;
        
        /**
         * Split overlap for text chunks
         */
        private Integer splitOverlap = 0;
        
        /**
         * Split method for text preprocessing
         */
        private String splitMethod = "sentence";
    }

    @Data
    public static class ImagePreprocessing {
        /**
         * Patch method for image processing
         */
        private String patchMethod = "simple";
    }
}
