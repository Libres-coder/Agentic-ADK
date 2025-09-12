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
package com.alibaba.langengine.cassandra.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.cassandra.utils.Constants;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class Cassandra extends VectorStore {
    
    /**
     * Embedding service for text-to-vector conversion
     */
    private Embeddings embedding;
    
    /**
     * Service for Cassandra operations
     */
    private CassandraService cassandraService;
    
    /**
     * Configuration for Cassandra connection
     */
    private CassandraConfiguration configuration;
    
    /**
     * Parameters for vector search operations
     */
    private CassandraParam cassandraParam;

    public Cassandra() {
        // Default constructor
    }

    public Cassandra(CassandraConfiguration configuration) {
        this(configuration, new CassandraParam());
    }

    public Cassandra(CassandraConfiguration configuration, CassandraParam cassandraParam) {
        this.configuration = configuration;
        this.cassandraParam = cassandraParam;
        
        String tableName = cassandraParam.getInitParam().getTableName() != null ? 
                cassandraParam.getInitParam().getTableName() : 
                Constants.DEFAULT_TABLE_NAME;
        
        this.cassandraService = new CassandraService(
                configuration.getKeyspace(),
                tableName,
                configuration.getContactPoints(),
                configuration.getLocalDatacenter(),
                configuration.getUsername(),
                configuration.getPassword(),
                cassandraParam
        );
        
        // Initialize Cassandra schema
        this.cassandraService.init();
    }

    @Override
    public void addDocuments(List<Document> documents) {
        cassandraService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        // Generate embedding for the query text
        if (embedding == null) {
            throw new UnsupportedOperationException("Embedding service is required for text-based similarity search. Please configure an embedding service or use vector-based search.");
        }
        
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        
        List<Float> queryVector = JSON.parseArray(embeddingStrings.get(0), Float.class);
        List<Double> queryVectorDouble = queryVector.stream().map(Float::doubleValue).collect(Collectors.toList());
        
        return similaritySearch(queryVectorDouble, k, maxDistanceValue);
    }

    public List<Document> similaritySearch(List<Double> embedding, int k) {
        return similaritySearch(embedding, k, null);
    }

    public List<Document> similaritySearch(List<Double> embedding, int k, Double maxDistanceValue) {
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("Embedding vector cannot be null or empty");
        }
        
        List<Float> floatEmbedding = embedding.stream()
                .map(Double::floatValue)
                .collect(java.util.stream.Collectors.toList());
        
        return cassandraService.similaritySearch(floatEmbedding, k, maxDistanceValue, null);
    }

    public List<Document> similaritySearchByVector(List<Double> embedding, int k) {
        return similaritySearch(embedding, k);
    }

    public List<Document> similaritySearchByVector(List<Double> embedding, int k, Map<String, Object> extraParams) {
        Double maxDistanceValue = null;
        if (extraParams != null && extraParams.containsKey("maxDistanceValue")) {
            maxDistanceValue = (Double) extraParams.get("maxDistanceValue");
        }
        
        return similaritySearch(embedding, k, maxDistanceValue);
    }

    public List<Document> maxMarginalRelevanceSearchByVector(List<Double> embedding, int k, double lambdaMult) {
        // For now, fallback to similarity search
        // TODO: Implement proper MMR algorithm
        log.info("Max marginal relevance search not yet implemented, falling back to similarity search");
        return similaritySearch(embedding, k);
    }

    public VectorStore fromDocuments(List<Document> documents, Object embeddings) {
        if (documents == null || documents.isEmpty()) {
            return this;
        }
        
        addDocuments(documents);
        return this;
    }

    public VectorStore fromTexts(List<String> texts, List<Map<String, Object>> metadatas, Object embeddings) {
        if (texts == null || texts.isEmpty()) {
            return this;
        }
        
        List<Document> documents = new java.util.ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            Document document = new Document();
            document.setPageContent(texts.get(i));
            
            if (metadatas != null && i < metadatas.size()) {
                document.setMetadata(metadatas.get(i));
            }
            
            documents.add(document);
        }
        
        return fromDocuments(documents, embeddings);
    }

    /**
     * Perform similarity search with additional type filter
     *
     * @param embedding the query embedding vector
     * @param k number of top results to return
     * @param maxDistanceValue maximum distance threshold
     * @param type additional type filter
     * @return list of similar documents
     */
    public List<Document> similaritySearchWithType(List<Double> embedding, int k, Double maxDistanceValue, Integer type) {
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("Embedding vector cannot be null or empty");
        }
        
        List<Float> floatEmbedding = embedding.stream()
                .map(Double::floatValue)
                .collect(java.util.stream.Collectors.toList());
        
        return cassandraService.similaritySearch(floatEmbedding, k, maxDistanceValue, type);
    }


    public CassandraService getCassandraService() {
        return cassandraService;
    }


    public CassandraConfiguration getConfiguration() {
        return configuration;
    }


    public CassandraParam getCassandraParam() {
        return cassandraParam;
    }


    public void close() {
        if (cassandraService != null) {
            cassandraService.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
