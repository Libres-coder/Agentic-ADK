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
package com.alibaba.langengine.mongodb.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class MongoDBService {

    private String databaseName;
    private String collectionName;
    private String connectionString;
    private MongoDBParam mongoDBParam;
    private final MongoClient client;
    private final MongoDatabase database;
    private final MongoCollection<org.bson.Document> collection;

    public MongoDBService(String connectionString,
                          String databaseName,
                          String collectionName,
                          MongoDBParam mongoDBParam) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.mongoDBParam = mongoDBParam != null ? mongoDBParam : new MongoDBParam();

        this.client = MongoClients.create(this.connectionString);
        this.database = client.getDatabase(this.databaseName);
        this.collection = database.getCollection(this.collectionName);
    }

    public void init() {
        try {
            MongoDBParam.InitParam initParam = mongoDBParam.getInitParam();
            String contentField = mongoDBParam.getFieldNamePageContent();
            String idField = mongoDBParam.getFieldNameUniqueId();

            org.bson.Document indexDefinition = new org.bson.Document()
                    .append("definition", new org.bson.Document()
                            .append("fields", new org.bson.Document()
                                    .append("embedding", new org.bson.Document()
                                            .append("type", "vector")
                                            .append("path", "embedding")
                                            .append("numDimensions", initParam.getDimension())
                                            .append("similarity", initParam.getMetricType()))));

            log.info("Successfully initialized MongoDB collection: {}", collectionName);
        } catch (Exception e) {
            log.error("MongoDB Service init failed", e);
        }
    }

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        String contentField = mongoDBParam.getFieldNamePageContent();
        String idField = mongoDBParam.getFieldNameUniqueId();
        String metaField = mongoDBParam.getFieldMeta();

        List<org.bson.Document> bsonDocuments = new ArrayList<>();

        for (Document d : documents) {
            org.bson.Document bsonDoc = new org.bson.Document();

            if (StringUtils.isNotBlank(d.getPageContent())) {
                bsonDoc.append(contentField, d.getPageContent());
            }

            if (StringUtils.isNotBlank(d.getUniqueId())) {
                bsonDoc.append(idField, d.getUniqueId());
            }

            if (d.getMetadata() != null && !d.getMetadata().isEmpty()) {
                bsonDoc.append(metaField, new org.bson.Document(d.getMetadata()));
            }

            List<Double> emb = d.getEmbedding();
            if (emb != null && !emb.isEmpty()) {
                bsonDoc.append("embedding", emb);
            }

            bsonDocuments.add(bsonDoc);
        }

        if (!bsonDocuments.isEmpty()) {
            collection.insertMany(bsonDocuments);
        }
    }

    public List<Document> similaritySearch(List<Float> query, int k, Double maxDistanceValue, Integer type) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyList();
        }

        MongoDBParam.InitParam initParam = mongoDBParam.getInitParam();
        String contentField = mongoDBParam.getFieldNamePageContent();
        String idField = mongoDBParam.getFieldNameUniqueId();
        String metaField = mongoDBParam.getFieldMeta();

        List<Double> queryVector = new ArrayList<>();
        for (Float f : query) {
            queryVector.add(f.doubleValue());
        }

        org.bson.Document vectorSearchStage = new org.bson.Document("$vectorSearch", new org.bson.Document()
                .append("index", "vector_index")
                .append("path", "embedding")
                .append("queryVector", queryVector)
                .append("numCandidates", initParam.getNumCandidates())
                .append("limit", k));

        org.bson.Document projectStage = new org.bson.Document("$project", new org.bson.Document()
                .append(contentField, 1)
                .append(idField, 1)
                .append(metaField, 1)
                .append("score", new org.bson.Document("$meta", "vectorSearchScore")));

        List<org.bson.Document> pipeline = Arrays.asList(vectorSearchStage, projectStage);

        List<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).forEach(doc -> {
            Document result = new Document();

            Object uniqueIdVal = doc.get(idField);
            if (uniqueIdVal != null) {
                result.setUniqueId(String.valueOf(uniqueIdVal));
            }

            Object pageContentVal = doc.get(contentField);
            if (pageContentVal != null) {
                result.setPageContent(String.valueOf(pageContentVal));
            }

            Object metadataVal = doc.get(metaField);
            if (metadataVal instanceof org.bson.Document) {
                Map<String, Object> metadata = new HashMap<>();
                ((org.bson.Document) metadataVal).forEach((key, value) -> 
                    metadata.put(key, value));
                result.setMetadata(metadata);
            }

            Object scoreVal = doc.get("score");
            if (scoreVal instanceof Number) {
                double score = ((Number) scoreVal).doubleValue();
                if (maxDistanceValue == null || score <= maxDistanceValue) {
                    result.setScore(score);
                    results.add(result);
                }
            } else {
                results.add(result);
            }
        });

        return results;
    }

    public void dropClass() {
        collection.drop();
    }
}
