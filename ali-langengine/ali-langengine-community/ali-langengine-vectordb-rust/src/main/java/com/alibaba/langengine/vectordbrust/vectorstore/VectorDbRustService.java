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
package com.alibaba.langengine.vectordbrust.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.Collection;
import com.tencent.tcvectordb.model.Database;
import com.tencent.tcvectordb.model.DocField;
import com.tencent.tcvectordb.model.param.collection.CreateCollectionParam;
import com.tencent.tcvectordb.model.param.collection.FieldType;
import com.tencent.tcvectordb.model.param.collection.IndexType;
import com.tencent.tcvectordb.model.param.collection.MetricType;
import com.tencent.tcvectordb.model.param.database.ConnectParam;
import com.tencent.tcvectordb.model.param.dml.InsertParam;
import com.tencent.tcvectordb.model.param.dml.SearchByVectorParam;
import com.tencent.tcvectordb.model.param.enums.ReadConsistencyEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public class VectorDbRustService {

    private String databaseName;
    private String collectionName;
    private VectorDbRustParam param;
    private final VectorDBClient client;
    private Database database;

    public VectorDbRustService(String url, String apiKey, String databaseName, String collectionName, VectorDbRustParam param) {
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.param = param != null ? param : new VectorDbRustParam();
        
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withUrl(url)
                .withUsername("root")
                .withKey(apiKey)
                .withTimeout(30)
                .build();
        this.client = new VectorDBClient(connectParam, ReadConsistencyEnum.EVENTUAL_CONSISTENCY);
        log.info("VectorDbRustService initialized: url={}, database={}, collection={}", url, databaseName, collectionName);
    }

    public void init(Embeddings embedding) {
        try {
            if (!hasDatabase()) {
                createDatabase();
            }
            database = client.database(databaseName);
            
            if (param.isAutoCreateCollection() && !hasCollection()) {
                createCollection(embedding);
            }
        } catch (Exception e) {
            log.error("init VectorDB failed", e);
            throw new VectorDbRustException("Failed to initialize VectorDB", e);
        }
    }

    public void addDocuments(List<Document> documents) {
        try {
            List<com.tencent.tcvectordb.model.Document> docs = new ArrayList<>();
            for (Document doc : documents) {
                List<Double> vector = doc.getEmbedding();
                
                com.tencent.tcvectordb.model.Document.Builder builder = com.tencent.tcvectordb.model.Document.newBuilder();
                if (StringUtils.isNotBlank(doc.getUniqueId())) {
                    builder.withId(doc.getUniqueId());
                }
                builder.withVector(vector);
                builder.addDocField(new DocField(param.getFieldNameContent(), doc.getPageContent()));
                docs.add(builder.build());
            }

            Collection collection = database.describeCollection(collectionName);
            InsertParam insertParam = InsertParam.newBuilder().withDocuments(docs).build();
            collection.upsert(insertParam);
            log.info("Added {} documents to collection {}", documents.size(), collectionName);
        } catch (Exception e) {
            log.error("addDocuments failed", e);
            throw new VectorDbRustException("Failed to add documents", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Document> similaritySearch(List<Double> embeddings, int k) {
        try {
            Collection collection = database.describeCollection(collectionName);
            
            SearchByVectorParam searchParam = SearchByVectorParam.newBuilder()
                    .withVectors(Collections.singletonList(embeddings))
                    .withLimit(k)
                    .withOutputFields(Arrays.asList(param.getFieldNameContent()))
                    .build();
            
            List<List<com.tencent.tcvectordb.model.Document>> results = collection.search(searchParam);
            
            if (results == null || results.isEmpty() || results.get(0).isEmpty()) {
                return Lists.newArrayList();
            }
            
            return results.get(0).stream().map(tcDoc -> {
                Document doc = new Document();
                doc.setScore(tcDoc.getScore());
                try {
                    Object contentObj = tcDoc.getDoc();
                    if (contentObj instanceof Map) {
                        Map<String, Object> docMap = (Map<String, Object>) contentObj;
                        Object content = docMap.get(param.getFieldNameContent());
                        if (content != null) {
                            doc.setPageContent(content.toString());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to get content from document", e);
                }
                doc.setUniqueId(tcDoc.getId());
                return doc;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("similaritySearch failed", e);
            throw new VectorDbRustException("Failed to search", e);
        }
    }

    public void createDatabase() {
        try {
            client.createDatabase(databaseName);
            log.info("Created database: {}", databaseName);
        } catch (Exception e) {
            log.error("createDatabase failed", e);
            throw new VectorDbRustException("Failed to create database", e);
        }
    }

    public void createCollection(Embeddings embedding) {
        try {
            int vectorSize = param.getVectorSize();
            if (vectorSize <= 0 && embedding != null) {
                List<Document> testDocs = embedding.embedTexts(Lists.newArrayList("test"));
                vectorSize = testDocs.get(0).getEmbedding().size();
            }

            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withName(collectionName)
                    .withShardNum(param.getShardNumber())
                    .withReplicaNum(param.getReplicationFactor())
                    .withDescription("Created by ali-langengine")
                    .build();

            database.createCollection(createParam);
            log.info("Created collection: {}", collectionName);
        } catch (Exception e) {
            log.error("createCollection failed", e);
            throw new VectorDbRustException("Failed to create collection", e);
        }
    }

    public boolean hasDatabase() {
        try {
            database = client.database(databaseName);
            return database != null;
        } catch (Exception e) {
            log.debug("Database not found: {}", databaseName);
            return false;
        }
    }

    public boolean hasCollection() {
        try {
            Collection collection = database.describeCollection(collectionName);
            return collection != null;
        } catch (Exception e) {
            log.debug("Collection not found: {}", collectionName);
            return false;
        }
    }

    public void deleteCollection() {
        try {
            database.dropCollection(collectionName);
            log.info("Deleted collection: {}", collectionName);
        } catch (Exception e) {
            log.error("deleteCollection failed", e);
            throw new VectorDbRustException("Failed to delete collection", e);
        }
    }

    public void close() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            log.error("close client failed", e);
        }
    }
}
