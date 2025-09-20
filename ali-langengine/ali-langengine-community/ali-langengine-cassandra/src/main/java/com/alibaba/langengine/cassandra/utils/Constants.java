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
package com.alibaba.langengine.cassandra.utils;


public class Constants {
    
    /**
     * Default table name for vector storage
     */
    public static final String DEFAULT_TABLE_NAME = "langengine_documents";
    
    /**
     * Default keyspace name
     */
    public static final String DEFAULT_KEYSPACE = "langengine";
    
    /**
     * Default contact point
     */
    public static final String DEFAULT_CONTACT_POINT = "127.0.0.1";
    
    /**
     * Default port
     */
    public static final int DEFAULT_PORT = 9042;
    
    /**
     * Default datacenter
     */
    public static final String DEFAULT_DATACENTER = "datacenter1";
    
    /**
     * Default vector dimensions
     */
    public static final int DEFAULT_VECTOR_DIMENSIONS = 1536;
    
    /**
     * Default similarity function
     */
    public static final String DEFAULT_SIMILARITY_FUNCTION = "cosine";
    
    /**
     * Default replication factor
     */
    public static final int DEFAULT_REPLICATION_FACTOR = 1;
    
    /**
     * Field names
     */
    public static final String DEFAULT_FIELD_NAME_UNIQUE_ID = "id";
    public static final String DEFAULT_FIELD_NAME_PAGE_CONTENT = "content";
    public static final String DEFAULT_FIELD_NAME_VECTOR = "vector";
    public static final String DEFAULT_FIELD_META = "metadata";
    
    /**
     * Similarity functions supported by Cassandra
     */
    public static final String SIMILARITY_FUNCTION_COSINE = "cosine";
    public static final String SIMILARITY_FUNCTION_DOT_PRODUCT = "dot_product";
    public static final String SIMILARITY_FUNCTION_EUCLIDEAN = "euclidean";
    
    private Constants() {
        // Utility class
    }
}
