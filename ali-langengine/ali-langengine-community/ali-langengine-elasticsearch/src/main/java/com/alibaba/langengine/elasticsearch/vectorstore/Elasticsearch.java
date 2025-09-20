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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.net.URI;
import java.util.List;

import static com.alibaba.langengine.elasticsearch.ElasticsearchConfiguration.*;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Elasticsearch extends VectorStore {

    private Embeddings embedding;

    private final String indexName;

    private final ElasticsearchService elasticsearchService;


    public Elasticsearch(String indexName) {
        this(indexName, null);
    }


    public Elasticsearch(String indexName, ElasticsearchParam elasticsearchParam) {
        this.indexName = indexName;

        String serverUrl = ELASTICSEARCH_SERVER_URL;
        String username = ELASTICSEARCH_USERNAME;
        String password = ELASTICSEARCH_PASSWORD;
        String apiKey = ELASTICSEARCH_API_KEY;

        this.elasticsearchService = new ElasticsearchService(
            serverUrl, username, password, apiKey, indexName, elasticsearchParam);
    }


    public Elasticsearch(String serverUrl, String indexName, ElasticsearchParam elasticsearchParam) {
        this.indexName = indexName;
        this.elasticsearchService = new ElasticsearchService(serverUrl, indexName, elasticsearchParam);
    }


    public Elasticsearch(String serverUrl, String username, String password, String apiKey,
                        String indexName, ElasticsearchParam elasticsearchParam) {
        this.indexName = indexName;
        this.elasticsearchService = new ElasticsearchService(
            serverUrl, username, password, apiKey, indexName, elasticsearchParam);
    }


    public void init() {
        try {
            elasticsearchService.init();
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch index: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Elasticsearch index", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        
        try {
            // Embed documents if they don't have embeddings
            documents = embedding.embedDocument(documents);
            elasticsearchService.addDocuments(documents);
        } catch (Exception e) {
            log.error("Failed to add documents to Elasticsearch: {}", e.getMessage());
            throw new RuntimeException("Failed to add documents to Elasticsearch", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (query == null || query.trim().isEmpty()) {
            return Lists.newArrayList();
        }

        try {
            // Generate query embedding
            List<String> embeddingStrings = embedding.embedQuery(query, k);
            if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
                return Lists.newArrayList();
            }

            List<Float> queryVector = JSON.parseArray(embeddingStrings.get(0), Float.class);
            return elasticsearchService.similaritySearch(queryVector, k, maxDistanceValue, type);
        } catch (Exception e) {
            log.error("Failed to perform similarity search: {}", e.getMessage());
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    /**
     * Perform similarity search with vector embedding directly
     */
    public List<Document> similaritySearchByVector(List<Float> queryVector, int k, 
                                                  Double maxDistanceValue, Integer type) {
        try {
            return elasticsearchService.similaritySearch(queryVector, k, maxDistanceValue, type);
        } catch (Exception e) {
            log.error("Failed to perform similarity search by vector: {}", e.getMessage());
            throw new RuntimeException("Failed to perform similarity search by vector", e);
        }
    }


    public void deleteIndex() {
        try {
            elasticsearchService.deleteIndex();
        } catch (Exception e) {
            log.error("Failed to delete Elasticsearch index: {}", e.getMessage());
            throw new RuntimeException("Failed to delete Elasticsearch index", e);
        }
    }


    public void close() {
        try {
            elasticsearchService.close();
        } catch (Exception e) {
            log.error("Failed to close Elasticsearch service: {}", e.getMessage());
        }
    }
}
