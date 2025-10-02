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
package com.alibaba.langengine.cosmosdb.vectorstore.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.cosmosdb.vectorstore.CosmosDBQueryException;
import com.alibaba.langengine.cosmosdb.vectorstore.client.CosmosDBClient;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBDocument;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBQueryRequest;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBQueryResponse;
import com.alibaba.langengine.cosmosdb.vectorstore.model.CosmosDBResult;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class CosmosDBService {

    private final CosmosDBClient client;

    public CosmosDBService(CosmosDBClient client) {
        this.client = client;
    }

    /**
     * Add documents to Cosmos DB container
     */
    public void addDocuments(List<CosmosDBDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            for (CosmosDBDocument document : documents) {
                client.getContainer().createItem(document);
            }

            log.info("Added {} documents to Cosmos DB container", documents.size());

        } catch (Exception e) {
            log.error("Failed to add documents to Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to add documents: " + e.getMessage(), e);
        }
    }

    /**
     * Update documents in Cosmos DB container
     */
    public void updateDocuments(List<CosmosDBDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            for (CosmosDBDocument document : documents) {
                document.setUpdatedAt(System.currentTimeMillis());
                client.getContainer().upsertItem(document);
            }

            log.info("Updated {} documents in Cosmos DB container", documents.size());

        } catch (Exception e) {
            log.error("Failed to update documents in Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to update documents: " + e.getMessage(), e);
        }
    }

    /**
     * Delete documents from Cosmos DB container
     */
    public void deleteDocuments(List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return;
        }

        try {
            for (String documentId : documentIds) {
                client.getContainer().deleteItem(documentId, new PartitionKey(documentId), new CosmosItemRequestOptions());
            }

            log.info("Deleted {} documents from Cosmos DB container", documentIds.size());

        } catch (Exception e) {
            log.error("Failed to delete documents from Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * Search documents using text query
     */
    public CosmosDBQueryResponse searchByText(CosmosDBQueryRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            String query = "SELECT * FROM c WHERE CONTAINS(c.content, @searchText)";
            List<SqlParameter> parameters = new ArrayList<>();
            parameters.add(new SqlParameter("@searchText", request.getQueryText()));
            SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            
            CosmosPagedIterable<CosmosDBDocument> results = client.getContainer()
                .queryItems(querySpec, options, CosmosDBDocument.class);

            List<CosmosDBResult> resultList = new ArrayList<>();
            int count = 0;
            for (CosmosDBDocument doc : results) {
                if (count >= request.getTop()) {
                    break;
                }
                CosmosDBResult result = CosmosDBResult.builder()
                    .document(doc)
                    .score(1.0)
                    .distance(0.0)
                    .build();
                resultList.add(result);
                count++;
            }

            long executionTime = System.currentTimeMillis() - startTime;

            return CosmosDBQueryResponse.builder()
                .results(resultList)
                .totalCount(resultList.size())
                .executionTimeMs(executionTime)
                .build();

        } catch (Exception e) {
            log.error("Failed to search by text in Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to search by text: " + e.getMessage(), e);
        }
    }

    /**
     * Search documents using vector similarity
     */
    public CosmosDBQueryResponse searchByVector(CosmosDBQueryRequest request) {
        try {
            long startTime = System.currentTimeMillis();

            if (CollectionUtils.isEmpty(request.getQueryVector())) {
                throw new CosmosDBQueryException("Query vector cannot be empty");
            }

            // Build vector search query using VectorDistance function
            String query = "SELECT TOP @topN c.id, c.content, c.contentVector, c.title, c.source, " +
                          "c.metadata, c.createdAt, c.updatedAt, c.category, c.tags, " +
                          "VectorDistance(c.contentVector, @embedding) AS SimilarityScore " +
                          "FROM c " +
                          "ORDER BY VectorDistance(c.contentVector, @embedding)";

            List<SqlParameter> parameters = new ArrayList<>();
            parameters.add(new SqlParameter("@topN", request.getTop()));
            parameters.add(new SqlParameter("@embedding", request.getQueryVector()));
            SqlQuerySpec querySpec = new SqlQuerySpec(query, parameters);

            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

            CosmosPagedIterable<Map> results = client.getContainer()
                .queryItems(querySpec, options, Map.class);

            List<CosmosDBResult> resultList = new ArrayList<>();
            for (Map item : results) {
                CosmosDBDocument doc = convertMapToDocument(item);
                Double score = item.get("SimilarityScore") != null ? 
                    ((Number) item.get("SimilarityScore")).doubleValue() : 0.0;

                // Filter by minimum score if specified
                if (request.getMinScore() != null && score < request.getMinScore()) {
                    continue;
                }

                // Convert similarity score to distance
                Double distance = 1.0 - score;

                CosmosDBResult result = CosmosDBResult.builder()
                    .document(doc)
                    .score(score)
                    .distance(distance)
                    .build();
                resultList.add(result);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            return CosmosDBQueryResponse.builder()
                .results(resultList)
                .totalCount(resultList.size())
                .executionTimeMs(executionTime)
                .build();

        } catch (Exception e) {
            log.error("Failed to search by vector in Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to search by vector: " + e.getMessage(), e);
        }
    }

    /**
     * Get document by ID
     */
    public CosmosDBDocument getDocumentById(String documentId) {
        try {
            return client.getContainer()
                .readItem(documentId, new PartitionKey(documentId), CosmosDBDocument.class)
                .getItem();

        } catch (Exception e) {
            log.error("Failed to get document by ID from Cosmos DB", e);
            throw new CosmosDBQueryException("Failed to get document by ID: " + e.getMessage(), e);
        }
    }

    /**
     * Convert LangEngine Document to CosmosDBDocument
     */
    public CosmosDBDocument convertFromDocument(Document document) {
        CosmosDBDocument cosmosDoc = new CosmosDBDocument();
        cosmosDoc.setId(document.getUniqueId());
        cosmosDoc.setContent(document.getPageContent());

        if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
            List<Float> floatVector = document.getEmbedding().stream()
                .map(Number::floatValue)
                .collect(Collectors.toList());
            cosmosDoc.setContentVector(floatVector);
        }

        // Extract title from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("title")) {
            cosmosDoc.setTitle(String.valueOf(document.getMetadata().get("title")));
        }

        // Extract source from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("source")) {
            cosmosDoc.setSource(String.valueOf(document.getMetadata().get("source")));
        }

        // Extract category from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("category")) {
            cosmosDoc.setCategory(String.valueOf(document.getMetadata().get("category")));
        }

        // Extract tags from metadata
        if (document.getMetadata() != null && document.getMetadata().containsKey("tags")) {
            Object tagsObj = document.getMetadata().get("tags");
            if (tagsObj instanceof List) {
                cosmosDoc.setTags((List<String>) tagsObj);
            }
        }

        // Serialize metadata to JSON string
        if (document.getMetadata() != null) {
            cosmosDoc.setMetadata(JSON.toJSONString(document.getMetadata()));
        }

        long currentTime = System.currentTimeMillis();
        cosmosDoc.setCreatedAt(currentTime);
        cosmosDoc.setUpdatedAt(currentTime);

        return cosmosDoc;
    }

    /**
     * Convert CosmosDBDocument to LangEngine Document
     */
    public Document convertToDocument(CosmosDBDocument cosmosDoc) {
        Document document = new Document();
        document.setUniqueId(cosmosDoc.getId());
        document.setPageContent(cosmosDoc.getContent());

        if (CollectionUtils.isNotEmpty(cosmosDoc.getContentVector())) {
            List<Double> embedding = cosmosDoc.getContentVector().stream()
                .map(Float::doubleValue)
                .collect(Collectors.toList());
            document.setEmbedding(embedding);
        }

        // Parse metadata from JSON string
        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.isNotBlank(cosmosDoc.getMetadata())) {
            try {
                metadata = JSON.parseObject(cosmosDoc.getMetadata(), Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse metadata JSON: {}", cosmosDoc.getMetadata(), e);
            }
        }

        // Add additional fields to metadata
        if (StringUtils.isNotBlank(cosmosDoc.getTitle())) {
            metadata.put("title", cosmosDoc.getTitle());
        }
        if (StringUtils.isNotBlank(cosmosDoc.getSource())) {
            metadata.put("source", cosmosDoc.getSource());
        }
        if (StringUtils.isNotBlank(cosmosDoc.getCategory())) {
            metadata.put("category", cosmosDoc.getCategory());
        }
        if (CollectionUtils.isNotEmpty(cosmosDoc.getTags())) {
            metadata.put("tags", cosmosDoc.getTags());
        }
        if (cosmosDoc.getCreatedAt() != null) {
            metadata.put("createdAt", cosmosDoc.getCreatedAt());
        }
        if (cosmosDoc.getUpdatedAt() != null) {
            metadata.put("updatedAt", cosmosDoc.getUpdatedAt());
        }

        document.setMetadata(metadata);
        return document;
    }

    /**
     * Convert Map to CosmosDBDocument
     */
    private CosmosDBDocument convertMapToDocument(Map item) {
        CosmosDBDocument doc = new CosmosDBDocument();
        doc.setId((String) item.get("id"));
        doc.setContent((String) item.get("content"));
        
        if (item.get("contentVector") instanceof List) {
            List<Float> vector = (List<Float>) item.get("contentVector");
            doc.setContentVector(vector);
        }
        
        doc.setTitle((String) item.get("title"));
        doc.setSource((String) item.get("source"));
        doc.setMetadata((String) item.get("metadata"));
        
        if (item.get("createdAt") != null) {
            doc.setCreatedAt(((Number) item.get("createdAt")).longValue());
        }
        if (item.get("updatedAt") != null) {
            doc.setUpdatedAt(((Number) item.get("updatedAt")).longValue());
        }
        
        doc.setCategory((String) item.get("category"));
        
        if (item.get("tags") instanceof List) {
            doc.setTags((List<String>) item.get("tags"));
        }
        
        return doc;
    }
}
