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
package com.alibaba.langengine.deeplake.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.deeplake.vectorstore.DeepLakeException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class DeepLakeService extends RetrofitInitService<DeepLakeApi> {

    private String datasetName;
    private String apiToken;

    public DeepLakeService(String serverUrl, String apiToken, String datasetName, Duration timeout) {
        super(serverUrl, timeout, false, null);
        this.apiToken = apiToken;
        this.datasetName = datasetName;
    }

    private static Map<String, String> createHeaders(String apiToken) {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.isNotEmpty(apiToken)) {
            headers.put("Authorization", "Bearer " + apiToken);
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public Class<DeepLakeApi> getServiceApiClass() {
        return DeepLakeApi.class;
    }

    /**
     * Initialize dataset
     */
    public void initializeDataset(Map<String, Object> datasetConfig) {
        try {
            log.info("Initializing Deep Lake dataset: {}", datasetName);
            
            // Check if dataset exists
            try {
                getDatasetInfo();
                log.info("Dataset {} already exists", datasetName);
                return;
            } catch (DeepLakeException e) {
                if (!DeepLakeException.ErrorCodes.DATASET_NOT_FOUND.equals(e.getErrorCode())) {
                    throw e;
                }
                // Dataset doesn't exist, create it
                log.info("Dataset {} not found, creating new dataset", datasetName);
            }

            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("name", datasetName);
            createRequest.putAll(datasetConfig);

            DeepLakeDatasetInfo result = execute(getApi().createDataset(createRequest));
            log.info("Successfully created dataset: {}", result.getName());
        } catch (Exception e) {
            log.error("Failed to initialize dataset: {}", datasetName, e);
            throw DeepLakeException.operationFailed("Failed to initialize dataset: " + datasetName, e);
        }
    }

    /**
     * Get dataset information
     */
    public DeepLakeDatasetInfo getDatasetInfo() {
        try {
            return execute(getApi().getDataset(datasetName));
        } catch (Exception e) {
            log.error("Failed to get dataset info: {}", datasetName, e);
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw DeepLakeException.datasetNotFound(datasetName);
            }
            throw DeepLakeException.operationFailed("Failed to get dataset info: " + datasetName, e);
        }
    }

    /**
     * Add documents to dataset
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("No documents to add");
            return;
        }

        try {
            log.info("Adding {} documents to dataset: {}", documents.size(), datasetName);
            
            List<DeepLakeInsertRequest.DeepLakeVector> vectors = documents.stream()
                .map(this::convertDocumentToVector)
                .collect(Collectors.toList());

            DeepLakeInsertRequest request = DeepLakeInsertRequest.builder()
                .vectors(vectors)
                .build();

            Map<String, Object> result = execute(getApi().insertVectors(datasetName, request));
            log.info("Successfully added documents, result: {}", result);
        } catch (Exception e) {
            log.error("Failed to add documents to dataset: {}", datasetName, e);
            throw DeepLakeException.operationFailed("Failed to add documents", e);
        }
    }

    /**
     * Search similar vectors
     */
    public List<Document> similaritySearch(List<Float> queryVector, int k, Double maxDistanceValue) {
        if (CollectionUtils.isEmpty(queryVector)) {
            throw DeepLakeException.invalidParameter("Query vector cannot be empty");
        }

        try {
            log.info("Performing similarity search with k={}, maxDistance={}", k, maxDistanceValue);
            
            DeepLakeQueryRequest request = DeepLakeQueryRequest.builder()
                .queryVector(queryVector)
                .topK(k)
                .distanceThreshold(maxDistanceValue)
                .includeMetadata(true)
                .includeValues(false)
                .build();

            DeepLakeQueryResponse response = execute(getApi().queryVectors(datasetName, request));
            
            if (response == null || CollectionUtils.isEmpty(response.getResults())) {
                log.info("No similar vectors found");
                return new ArrayList<>();
            }

            return response.getResults().stream()
                .map(this::convertVectorRecordToDocument)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw DeepLakeException.operationFailed("Failed to perform similarity search", e);
        }
    }

    /**
     * Delete vectors by IDs
     */
    public void deleteVectors(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            log.warn("No IDs provided for deletion");
            return;
        }

        try {
            log.info("Deleting {} vectors from dataset: {}", ids.size(), datasetName);
            String idsParam = String.join(",", ids);
            execute(getApi().deleteVectors(datasetName, idsParam));
            log.info("Successfully deleted vectors");
        } catch (Exception e) {
            log.error("Failed to delete vectors", e);
            throw DeepLakeException.operationFailed("Failed to delete vectors", e);
        }
    }

    /**
     * Get dataset statistics
     */
    public Map<String, Object> getDatasetStats() {
        try {
            return execute(getApi().getDatasetStats(datasetName));
        } catch (Exception e) {
            log.error("Failed to get dataset statistics", e);
            throw DeepLakeException.operationFailed("Failed to get dataset statistics", e);
        }
    }

    /**
     * Convert Document to DeepLakeVector
     */
    private DeepLakeInsertRequest.DeepLakeVector convertDocumentToVector(Document document) {
        if (document == null) {
            throw DeepLakeException.invalidParameter("Document cannot be null");
        }

        String id = StringUtils.isNotEmpty(document.getUniqueId()) ? 
            document.getUniqueId() : UUID.randomUUID().toString();

        List<Float> values = null;
        if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
            values = document.getEmbedding().stream()
                .map(Double::floatValue)
                .collect(Collectors.toList());
        }

        Map<String, Object> metadata = new HashMap<>();
        if (MapUtils.isNotEmpty(document.getMetadata())) {
            metadata.putAll(document.getMetadata());
        }

        return DeepLakeInsertRequest.DeepLakeVector.builder()
            .id(id)
            .values(values)
            .content(document.getPageContent())
            .metadata(metadata)
            .build();
    }

    /**
     * Convert DeepLakeVectorRecord to Document
     */
    private Document convertVectorRecordToDocument(DeepLakeQueryResponse.DeepLakeVectorRecord record) {
        Document document = new Document();
        document.setUniqueId(record.getId());
        document.setPageContent(record.getContent());
        document.setScore(record.getScore());
        
        if (MapUtils.isNotEmpty(record.getMetadata())) {
            document.setMetadata(record.getMetadata());
        }

        if (CollectionUtils.isNotEmpty(record.getValues())) {
            List<Double> embedding = record.getValues().stream()
                .map(Float::doubleValue)
                .collect(Collectors.toList());
            document.setEmbedding(embedding);
        }

        return document;
    }
}
