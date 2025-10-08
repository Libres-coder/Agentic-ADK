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
package com.alibaba.langengine.atlas.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Data
public class AtlasService {

    private String connectionString;
    private String databaseName;
    private String collectionName;
    private AtlasParam atlasParam;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public AtlasService(String connectionString, String databaseName, String collectionName, AtlasParam atlasParam) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.atlasParam = atlasParam;
        
        try {
            this.mongoClient = MongoClients.create(connectionString);
            this.database = mongoClient.getDatabase(databaseName);
            this.collection = database.getCollection(collectionName, Document.class);
            log.info("AtlasService initialized: database={}, collection={}", databaseName, collectionName);
        } catch (Exception e) {
            throw new AtlasException("Failed to initialize Atlas connection", e);
        }
    }

    /**
     * Add documents to Atlas Vector Search
     */
    public void addDocuments(List<com.alibaba.langengine.core.indexes.Document> documents) {
        try {
            AtlasParam param = loadParam();
            String fieldNameUniqueId = param.getFieldNameUniqueId();
            String fieldNamePageContent = param.getFieldNamePageContent();
            String fieldNameEmbedding = param.getFieldNameEmbedding();

            List<Document> bsonDocuments = Lists.newArrayList();
            for (com.alibaba.langengine.core.indexes.Document document : documents) {
                Document bsonDoc = new Document();
                bsonDoc.append(fieldNameUniqueId, NumberUtils.toLong(document.getUniqueId()));
                bsonDoc.append(fieldNamePageContent, document.getPageContent());
                
                List<Double> embeddings = document.getEmbedding();
                bsonDoc.append(fieldNameEmbedding, embeddings);
                
                bsonDocuments.add(bsonDoc);
            }

            collection.insertMany(bsonDocuments);
            log.info("Added {} documents to Atlas collection", documents.size());
        } catch (Exception e) {
            throw new AtlasException("Failed to add documents", e);
        }
    }

    /**
     * Vector similarity search
     */
    public List<com.alibaba.langengine.core.indexes.Document> similaritySearch(List<Double> embeddings, int k) {
        try {
            AtlasParam param = loadParam();
            String fieldNameUniqueId = param.getFieldNameUniqueId();
            String fieldNamePageContent = param.getFieldNamePageContent();
            String fieldNameEmbedding = param.getFieldNameEmbedding();
            String vectorIndexName = param.getVectorIndexName();
            int numCandidates = param.getNumCandidates();

            List<Document> pipeline = Lists.newArrayList();
            pipeline.add(new Document("$vectorSearch", new Document()
                .append("index", vectorIndexName)
                .append("path", fieldNameEmbedding)
                .append("queryVector", embeddings)
                .append("numCandidates", numCandidates)
                .append("limit", k)));
            pipeline.add(new Document("$project", new Document()
                .append(fieldNameUniqueId, 1)
                .append(fieldNamePageContent, 1)
                .append("score", new Document("$meta", "vectorSearchScore"))));

            List<com.alibaba.langengine.core.indexes.Document> documents = Lists.newArrayList();
            for (Document result : collection.aggregate(pipeline)) {
                com.alibaba.langengine.core.indexes.Document document = new com.alibaba.langengine.core.indexes.Document();
                document.setUniqueId(String.valueOf(result.get(fieldNameUniqueId)));
                document.setPageContent((String) result.get(fieldNamePageContent));
                Object scoreObj = result.get("score");
                if (scoreObj != null) {
                    document.setScore(((Number) scoreObj).doubleValue());
                }
                documents.add(document);
            }

            return documents;
        } catch (Exception e) {
            throw new AtlasException("Failed to perform similarity search", e);
        }
    }

    /**
     * Initialize collection and vector search index
     */
    public void init(Embeddings embedding) {
        try {
            AtlasParam param = loadParam();
            AtlasParam.InitParam initParam = param.getInitParam();
            
            if (initParam.getFieldEmbeddingsDimension() <= 0) {
                List<com.alibaba.langengine.core.indexes.Document> embeddingDocuments = embedding.embedTexts(Lists.newArrayList("test"));
                com.alibaba.langengine.core.indexes.Document document = embeddingDocuments.get(0);
                initParam.setFieldEmbeddingsDimension(document.getEmbedding().size());
            }
            
            log.info("Atlas collection initialized with embedding dimension: {}", initParam.getFieldEmbeddingsDimension());
        } catch (Exception e) {
            throw new AtlasException("Failed to initialize Atlas collection", e);
        }
    }

    /**
     * Load parameters with defaults
     */
    private AtlasParam loadParam() {
        if (atlasParam == null) {
            atlasParam = new AtlasParam();
        }
        return atlasParam;
    }

    /**
     * Close MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

}