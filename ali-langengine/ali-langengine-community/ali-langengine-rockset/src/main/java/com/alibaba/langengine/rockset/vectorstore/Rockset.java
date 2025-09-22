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

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.rockset.vectorstore.service.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class Rockset extends VectorStore {

    private String workspace;
    private String collectionName;
    private RocksetParam rocksetParam;
    private RocksetService rocksetService;
    private Embeddings embedding;

    /**
     * Constructor
     */
    public Rockset(Embeddings embeddings, RocksetParam rocksetParam) {
        super();
        this.embedding = embeddings;
        this.rocksetParam = rocksetParam;
        
        // Get configuration values
        String serverUrl = RocksetConfiguration.getServerUrl();
        String apiKey = RocksetConfiguration.getApiKey();
        this.workspace = rocksetParam.getInitParam().getWorkspace();
        this.collectionName = rocksetParam.getInitParam().getCollectionName();

        // Initialize service
        Duration timeout = Duration.ofSeconds(60);
        this.rocksetService = new RocksetService(serverUrl, apiKey, workspace, timeout);

        // Initialize collection
        initializeCollection();
    }

    /**
     * Initialize collection if it doesn't exist
     */
    private void initializeCollection() {
        try {
            String description = rocksetParam.getInitParam().getDescription();
            
            // Check if collection exists, create if not
            RocksetCollectionData existingCollection = rocksetService.getCollection(collectionName);
            if (existingCollection == null) {
                log.info("Collection {} does not exist, creating...", collectionName);
                rocksetService.createCollection(collectionName, description);
                log.info("Collection {} created successfully", collectionName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize collection: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize collection", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        try {
            String fieldNamePageContent = rocksetParam.getFieldNamePageContent();
            String fieldNameUniqueId = rocksetParam.getFieldNameUniqueId();
            String fieldMeta = rocksetParam.getFieldMeta();
            String fieldNameVector = rocksetParam.getFieldNameVector();

            RocksetInsertResponse response = rocksetService.insertDocuments(
                    collectionName, documents, fieldNamePageContent, fieldNameUniqueId,
                    fieldMeta, fieldNameVector);

            log.info("Successfully inserted {} documents into collection {}", 
                    documents.size(), collectionName);
        } catch (Exception e) {
            log.error("Failed to add documents to collection {}: {}", collectionName, e.getMessage());
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        return similaritySearchWithFilter(query, k, null);
    }

    public List<Document> similaritySearchWithFilter(String query, int k, Map<String, Object> metadataFilter) {
        try {
            // Generate query embedding
            List<Double> queryVector = embedQuery(query);
            if (queryVector == null || queryVector.isEmpty()) {
                throw new RuntimeException("Failed to generate embedding for query");
            }

            return vectorSimilaritySearch(queryVector, k, metadataFilter);

        } catch (Exception e) {
            log.error("Error performing similarity search in collection {}: {}", collectionName, e.getMessage());
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    /**
     * Wrapper method for embedQuery to match expected signature
     */
    private List<Double> embedQuery(String query) {
        try {
            List<String> embeddingStrings = this.embedding.embedQuery(query, 1);
            if (embeddingStrings.isEmpty() || !embeddingStrings.get(0).startsWith("[")) {
                return null;
            }
            // Parse the JSON array string to List<Double>
            String embeddingStr = embeddingStrings.get(0);
            // Simple parsing - in production you might want to use a JSON library
            List<Double> embeddingResult = parseEmbeddingString(embeddingStr);
            return embeddingResult;
        } catch (Exception e) {
            log.error("Failed to generate embedding for query: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parse embedding string to List<Double>
     */
    private List<Double> parseEmbeddingString(String embeddingStr) {
        // Remove brackets and split by comma
        String cleaned = embeddingStr.trim().substring(1, embeddingStr.length() - 1);
        String[] parts = cleaned.split(",");
        List<Double> result = new ArrayList<>();
        for (String part : parts) {
            try {
                result.add(Double.parseDouble(part.trim()));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse embedding value: {}", part.trim());
            }
        }
        return result;
    }

    /**
     * Perform vector similarity search
     */
    private List<Document> vectorSimilaritySearch(List<Double> queryVector, int k, Map<String, Object> metadataFilter) {
        try {
            // Build SELECT clause
            String fieldNamePageContent = rocksetParam.getFieldNamePageContent();
            String fieldMeta = rocksetParam.getFieldMeta();
            String fieldNameUniqueId = rocksetParam.getFieldNameUniqueId();
            String fieldNameVector = rocksetParam.getFieldNameVector();

            // Build WHERE clause for metadata filter
            StringBuilder whereClause = new StringBuilder();
            if (metadataFilter != null && !metadataFilter.isEmpty()) {
                whereClause.append(" AND ");
                String metaFilter = rocksetParam.getFieldMeta();
                // Add metadata filter conditions here if needed
            }

            // Build SQL query
            String sqlQuery = String.format(
                    "SELECT %s, %s, %s, COSINE_SIM(%s, ARRAY[%s]) as score " +
                            "FROM %s.%s " +
                            "WHERE 1=1 %s " +
                            "ORDER BY score DESC " +
                            "LIMIT %d",
                    fieldNamePageContent, fieldMeta, fieldNameUniqueId, fieldNameVector,
                    queryVector.stream().map(String::valueOf).collect(Collectors.joining(",")),
                    workspace, collectionName, whereClause.toString(), k
            );

            // Create parameters list (empty for this case)
            List<RocksetQueryRequest.Parameter> parameters = new ArrayList<>();

            RocksetQueryResponse response = rocksetService.query(sqlQuery, parameters);

            // Convert results to Document objects
            List<Document> documents = new ArrayList<>();
            if (response.getResults() != null) {
                for (Map<String, Object> result : response.getResults()) {
                    String content = (String) result.get(rocksetParam.getFieldNamePageContent());
                    if (content != null) {
                        Document doc = new Document();
                        doc.setPageContent(content);

                        // Set unique ID if available
                        String uniqueId = (String) result.get(rocksetParam.getFieldNameUniqueId());
                        if (uniqueId != null) {
                            doc.getMetadata().put(rocksetParam.getFieldNameUniqueId(), uniqueId);
                        }

                        // Set metadata if available
                        Object metadata = result.get(rocksetParam.getFieldMeta());
                        if (metadata instanceof Map) {
                            doc.getMetadata().putAll((Map<String, Object>) metadata);
                        }

                        documents.add(doc);
                    }
                }
            }

            log.info("Found {} similar documents", documents.size());
            return documents;

        } catch (Exception e) {
            log.error("Error performing vector similarity search: {}", e.getMessage());
            throw new RuntimeException("Failed to perform vector similarity search", e);
        }
    }

    public List<String> delete(List<String> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                log.warn("No IDs provided for deletion");
                return new ArrayList<>();
            }

            log.info("Deleting {} documents from collection {}", ids.size(), collectionName);

            // Build WHERE clause for deletion
            String fieldNameVector = rocksetParam.getFieldNameVector();
            String whereClause = String.format("%s IN ('%s')", 
                    rocksetParam.getFieldNameUniqueId(),
                    String.join("','", ids));

            String deleteSql = String.format("DELETE FROM %s.%s WHERE %s",
                    workspace, collectionName, whereClause);

            // Create parameters list (empty for this case)
            List<RocksetQueryRequest.Parameter> parameters = new ArrayList<>();

            rocksetService.query(deleteSql, parameters);

            log.info("Successfully deleted documents with IDs: {}", String.join(", ", ids));
            return ids;

        } catch (Exception e) {
            log.error("Error deleting documents: {}", e.getMessage());
            throw new RuntimeException("Failed to delete documents", e);
        }
    }

    public void deleteCollection() {
        try {
            log.info("Deleting collection: {}", collectionName);
            
            // Check if collection exists
            RocksetCollectionData collection = rocksetService.getCollection(collectionName);
            if (collection == null) {
                log.warn("Collection {} does not exist", collectionName);
                return;
            }

            // Rockset doesn't have direct delete collection API in this implementation
            // You might need to implement this based on Rockset's actual API
            log.warn("Collection deletion not implemented in current Rockset API");

        } catch (Exception e) {
            log.error("Error deleting collection: {}", e.getMessage());
            throw new RuntimeException("Failed to delete collection", e);
        }
    }
}
