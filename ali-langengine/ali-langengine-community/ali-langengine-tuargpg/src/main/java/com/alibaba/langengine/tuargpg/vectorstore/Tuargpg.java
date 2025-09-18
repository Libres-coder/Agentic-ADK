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
package com.alibaba.langengine.tuargpg.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.tuargpg.TuargpgConfiguration;
import com.alibaba.langengine.tuargpg.vectorstore.service.TuargpgService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.alibaba.langengine.tuargpg.TuargpgConfiguration.*;

@Slf4j
@Data
public class Tuargpg extends VectorStore {

    private Embeddings embedding;

    private String collectionName;

    private TuargpgClient _client;

    private TuargpgService _service;

    private TuargpgVectorStoreParam param;

    public Tuargpg(Embeddings embedding, String collectionName) {
        this(buildDefaultParam(collectionName), embedding, collectionName);
    }

    public Tuargpg(String serverUrl, Embeddings embedding, String collectionName) {
        TuargpgVectorStoreParam customParam = TuargpgVectorStoreParam.builder()
            .serverUrl(serverUrl)
            .username(TUARGPG_USERNAME)
            .password(TUARGPG_PASSWORD)
            .database(TUARGPG_DATABASE)
            .schema(StringUtils.isEmpty(TUARGPG_SCHEMA) ? "public" : TUARGPG_SCHEMA)
            .tableName(StringUtils.isEmpty(TUARGPG_TABLE) ? collectionName : TUARGPG_TABLE)
            .build();

        this.initTuargpg(customParam, embedding, collectionName);
    }

    public Tuargpg(TuargpgVectorStoreParam param, Embeddings embedding, String collectionName) {
        this.initTuargpg(param, embedding, collectionName);
    }

    private void initTuargpg(TuargpgVectorStoreParam param, Embeddings embedding, String collectionName) {
        this.param = param;
        this.embedding = embedding;
        this.collectionName = StringUtils.isEmpty(collectionName) ?
            UUID.randomUUID().toString() : collectionName;

        if (StringUtils.isEmpty(param.getTableName())) {
            param.setTableName(this.collectionName);
        }

        try {
            this._client = new TuargpgClient(param);
            this._service = new TuargpgService(_client);
            this._service.initialize();
        } catch (Exception e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.UNKNOWN_ERROR,
                "Failed to initialize Tuargpg vector store",
                e
            );
        }
    }

    private static TuargpgVectorStoreParam buildDefaultParam(String tableName) {
        return TuargpgVectorStoreParam.builder()
            .serverUrl(TUARGPG_SERVER_URL)
            .username(TUARGPG_USERNAME)
            .password(TUARGPG_PASSWORD)
            .database(TUARGPG_DATABASE)
            .schema(StringUtils.isEmpty(TUARGPG_SCHEMA) ? "public" : TUARGPG_SCHEMA)
            .tableName(StringUtils.isEmpty(TUARGPG_TABLE) ? tableName : TUARGPG_TABLE)
            .build();
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            List<Document> documentsWithEmbeddings = new ArrayList<>();

            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }

                if (StringUtils.isEmpty(document.getPageContent())) {
                    continue;
                }

                if (document.getMetadata() == null) {
                    document.setMetadata(new HashMap<>());
                }

                if (CollectionUtils.isEmpty(document.getEmbedding()) && embedding != null) {
                    List<Document> embeddedDocs = embedding.embedTexts(List.of(document.getPageContent()));
                    if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                        document.setEmbedding(embeddedDocs.get(0).getEmbedding());
                    }
                }

                documentsWithEmbeddings.add(document);
            }

            _service.addDocuments(documentsWithEmbeddings);
        } catch (Exception e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.UNKNOWN_ERROR,
                "Failed to add documents",
                e
            );
        }
    }

    public List<String> addTexts(
        Iterable<String> texts,
        List<java.util.Map<String, Object>> metadatas,
        List<String> ids
    ) {
        List<String> textsList = new ArrayList<>();
        texts.forEach(textsList::add);

        if (ids == null) {
            ids = new ArrayList<>();
            for (String text : textsList) {
                ids.add(UUID.randomUUID().toString());
            }
        }

        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < textsList.size(); i++) {
            Document document = new Document();
            document.setUniqueId(ids.get(i));
            document.setPageContent(textsList.get(i));

            if (metadatas != null && i < metadatas.size()) {
                document.setMetadata(metadatas.get(i));
            } else {
                document.setMetadata(new HashMap<>());
            }

            documents.add(document);
        }

        addDocuments(documents);
        return ids;
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            List<Float> queryVector = null;

            if (embedding != null) {
                List<Document> embeddedDocs = embedding.embedTexts(List.of(query));
                if (CollectionUtils.isNotEmpty(embeddedDocs) &&
                    CollectionUtils.isNotEmpty(embeddedDocs.get(0).getEmbedding())) {
                    queryVector = embeddedDocs.get(0).getEmbedding().stream()
                        .map(Double::floatValue)
                        .collect(Collectors.toList());
                }
            }

            if (queryVector == null) {
                throw new TuargpgVectorStoreException(
                    TuargpgVectorStoreException.ErrorCodes.INVALID_PARAMETER,
                    "Cannot perform similarity search without query embedding"
                );
            }

            List<Document> results = _service.similaritySearch(query, queryVector, k, maxDistanceValue);

            if (maxDistanceValue != null) {
                results = results.stream()
                    .filter(doc -> doc.getScore() != null && (1.0 - doc.getScore()) <= maxDistanceValue)
                    .collect(Collectors.toList());
            }

            return results;

        } catch (Exception e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.QUERY_EXECUTION_FAILED,
                "Failed to execute similarity search",
                e
            );
        }
    }

    public void deleteById(String id) {
        try {
            _service.deleteById(id);
        } catch (Exception e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.QUERY_EXECUTION_FAILED,
                "Failed to delete document by id",
                e
            );
        }
    }

    public void deleteByIds(List<String> ids) {
        try {
            _service.deleteByIds(ids);
        } catch (Exception e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.QUERY_EXECUTION_FAILED,
                "Failed to delete documents by ids",
                e
            );
        }
    }

    public void close() {
        try {
            if (_service != null) {
                _service.close();
            }
        } catch (Exception e) {
            log.warn("Failed to close Tuargpg vector store", e);
        }
    }
}