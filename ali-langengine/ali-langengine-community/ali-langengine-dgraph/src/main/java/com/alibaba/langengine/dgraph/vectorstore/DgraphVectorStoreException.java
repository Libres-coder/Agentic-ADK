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
package com.alibaba.langengine.dgraph.vectorstore;


public class DgraphVectorStoreException extends RuntimeException {

    public DgraphVectorStoreException(String message) {
        super(message);
    }

    public DgraphVectorStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 向量化失败异常
     */
    public static class EmbeddingGenerationException extends DgraphVectorStoreException {
        public EmbeddingGenerationException(String message) {
            super("Failed to generate embeddings: " + message);
        }

        public EmbeddingGenerationException(String message, Throwable cause) {
            super("Failed to generate embeddings: " + message, cause);
        }
    }

    /**
     * 向量搜索失败异常
     */
    public static class VectorSearchException extends DgraphVectorStoreException {
        public VectorSearchException(String message) {
            super("Vector search failed: " + message);
        }

        public VectorSearchException(String message, Throwable cause) {
            super("Vector search failed: " + message, cause);
        }
    }

    /**
     * 模式初始化失败异常
     */
    public static class SchemaInitializationException extends DgraphVectorStoreException {
        public SchemaInitializationException(String message) {
            super("Schema initialization failed: " + message);
        }

        public SchemaInitializationException(String message, Throwable cause) {
            super("Schema initialization failed: " + message, cause);
        }
    }

    /**
     * 配置错误异常
     */
    public static class ConfigurationException extends DgraphVectorStoreException {
        public ConfigurationException(String message) {
            super("Configuration error: " + message);
        }
    }
}
