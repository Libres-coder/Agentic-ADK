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
package com.alibaba.langengine.tuargpg.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.tuargpg.vectorstore.TuargpgClient;
import com.alibaba.langengine.tuargpg.vectorstore.TuargpgVectorStoreException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class TuargpgService {

    private final TuargpgClient client;

    public TuargpgService(TuargpgClient client) {
        this.client = client;
    }

    public void initialize() {
        try {
            client.createTableIfNotExists();
            log.info("Tuargpg service initialized successfully");
        } catch (Exception e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.UNKNOWN_ERROR,
                "Failed to initialize Tuargpg service",
                e
            );
        }
    }

    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        List<TuargpgEmbeddingsRequest.TuargpgEmbeddingRecord> records = new ArrayList<>();

        for (Document document : documents) {
            if (StringUtils.isEmpty(document.getPageContent())) {
                continue;
            }

            TuargpgEmbeddingsRequest.TuargpgEmbeddingRecord record = new TuargpgEmbeddingsRequest.TuargpgEmbeddingRecord();

            record.setId(StringUtils.isEmpty(document.getUniqueId()) ? UUID.randomUUID().toString() : document.getUniqueId());
            record.setContent(document.getPageContent());

            if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
                List<Float> floatVector = document.getEmbedding().stream()
                    .map(Double::floatValue)
                    .collect(Collectors.toList());
                record.setVector(floatVector);
            }

            Map<String, Object> metadata = document.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }
            record.setMetadata(metadata);

            records.add(record);
        }

        if (!records.isEmpty()) {
            TuargpgEmbeddingsRequest request = TuargpgEmbeddingsRequest.builder()
                .records(records)
                .build();

            client.addEmbeddings(request);
            log.info("Successfully added {} documents to Tuargpg", records.size());
        }
    }

    public List<Document> similaritySearch(String query, List<Float> queryVector, int k, Double maxDistance) {
        if (CollectionUtils.isEmpty(queryVector)) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.INVALID_PARAMETER,
                "Query vector is required for similarity search"
            );
        }

        TuargpgQueryRequest request = TuargpgQueryRequest.builder()
            .queryVector(queryVector)
            .topK(k)
            .maxDistance(maxDistance)
            .build();

        TuargpgQueryResponse response = client.queryByVector(request);

        List<Document> documents = new ArrayList<>();
        if (response.getRecords() != null) {
            for (TuargpgQueryResponse.TuargpgVectorRecord record : response.getRecords()) {
                Document document = new Document();
                document.setUniqueId(record.getId());
                document.setPageContent(record.getContent());
                document.setScore(record.getScore());

                if (CollectionUtils.isNotEmpty(record.getVector())) {
                    List<Double> doubleVector = record.getVector().stream()
                        .map(Float::doubleValue)
                        .collect(Collectors.toList());
                    document.setEmbedding(doubleVector);
                }

                if (record.getMetadata() != null) {
                    document.setMetadata(record.getMetadata());
                } else {
                    document.setMetadata(new HashMap<>());
                }

                documents.add(document);
            }
        }

        log.info("Retrieved {} documents from similarity search", documents.size());
        return documents;
    }

    public void deleteById(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.INVALID_PARAMETER,
                "Document ID is required for deletion"
            );
        }

        client.deleteById(id);
    }

    public void deleteByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        for (String id : ids) {
            deleteById(id);
        }

        log.info("Deleted {} documents from Tuargpg", ids.size());
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }
}