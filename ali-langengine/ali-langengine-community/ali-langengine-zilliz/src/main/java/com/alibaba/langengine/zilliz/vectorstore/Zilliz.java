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
package com.alibaba.langengine.zilliz.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.zilliz.ZillizConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Zilliz extends VectorStore {

    /**
     * Embedding model
     */
    private Embeddings embedding;

    /**
     * Collection name
     */
    private final String collection;

    /**
     * Partition name
     */
    private final String partition;

    /**
     * Zilliz service
     */
    private final ZillizService zillizService;

    public Zilliz(String collection) {
        this(collection, null);
    }

    public Zilliz(String collection, String partition) {
        this(collection, partition, null);
    }

    public Zilliz(String collection, String partition, ZillizParam zillizParam) {
        this.collection = collection;
        this.partition = partition;
        
        String clusterEndpoint = ZILLIZ_CLUSTER_ENDPOINT;
        String apiKey = ZILLIZ_API_KEY;
        String databaseName = ZILLIZ_DATABASE_NAME;
        
        if (clusterEndpoint == null || apiKey == null) {
            throw new IllegalArgumentException("Zilliz cluster endpoint and API key must be configured");
        }
        
        this.zillizService = new ZillizService(clusterEndpoint, apiKey, databaseName, collection, partition, zillizParam);
    }

    /**
     * Initialize Zilliz collection
     * Creates collection with:
     * 1. Vector field based on embedding model dimension
     * 2. String content_id field as primary key
     * 3. String row_content field with configurable length
     * 4. Index on vector field
     */
    public void init() {
        try {
            zillizService.init(embedding);
        } catch (Exception e) {
            log.error("Failed to initialize Zilliz", e);
            throw new RuntimeException("Failed to initialize Zilliz", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents = embedding.embedDocument(documents);
        zillizService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Float> embeddings = JSON.parseArray(embeddingStrings.get(0), Float.class);
        return zillizService.similaritySearch(embeddings, k);
    }

    /**
     * Close Zilliz connection
     */
    public void close() {
        if (zillizService != null) {
            zillizService.close();
        }
    }

}