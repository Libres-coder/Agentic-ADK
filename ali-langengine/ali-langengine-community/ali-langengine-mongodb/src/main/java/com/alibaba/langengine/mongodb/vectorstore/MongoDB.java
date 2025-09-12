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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.alibaba.langengine.mongodb.MongoDBConfiguration.MONGODB_CONNECTION_STRING;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class MongoDB extends VectorStore {

    private Embeddings embedding;

    private final String databaseName;
    private final String collectionName;

    private final MongoDBService mongoDBService;

    public MongoDB(String databaseName, String collectionName) {
        this(databaseName, collectionName, null);
    }

    public MongoDB(String databaseName, String collectionName, MongoDBParam mongoDBParam) {
        this.databaseName = databaseName;
        this.collectionName = collectionName;

        String connectionString = MONGODB_CONNECTION_STRING;
        if (connectionString == null) {
            connectionString = "mongodb://localhost:27017";
        }

        this.mongoDBService = new MongoDBService(connectionString, databaseName, collectionName, mongoDBParam);
    }

    public void init() {
        try {
            mongoDBService.init();
        } catch (Exception e) {
            log.error("init mongodb failed", e);
        }
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents = embedding.embedDocument(documents);
        mongoDBService.addDocuments(documents);
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (CollectionUtils.isEmpty(embeddingStrings) || !embeddingStrings.get(0).startsWith("[")) {
            return Lists.newArrayList();
        }
        List<Float> vec = JSON.parseArray(embeddingStrings.get(0), Float.class);
        return mongoDBService.similaritySearch(vec, k, maxDistanceValue, type);
    }
}
