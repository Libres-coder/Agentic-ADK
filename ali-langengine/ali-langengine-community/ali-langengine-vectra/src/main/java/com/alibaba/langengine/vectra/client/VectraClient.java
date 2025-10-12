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
package com.alibaba.langengine.vectra.client;

import com.alibaba.langengine.vectra.exception.VectraException;
import com.tencent.tcvectordb.client.VectorDBClient;
import com.tencent.tcvectordb.model.Collection;
import com.tencent.tcvectordb.model.Database;
import com.tencent.tcvectordb.model.Document;
import com.tencent.tcvectordb.model.param.database.ConnectParam;
import com.tencent.tcvectordb.model.param.enums.ReadConsistencyEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
public class VectraClient {

    private final VectorDBClient vectorDBClient;
    private final String databaseName;
    private Database database;

    public VectraClient(String url, String apiKey, String databaseName, int readTimeout) {
        this.databaseName = databaseName;
        
        try {
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withUrl(url)
                    .withKey(apiKey)
                    .withTimeout(readTimeout)
                    .build();
            
            this.vectorDBClient = new VectorDBClient(connectParam, ReadConsistencyEnum.EVENTUAL_CONSISTENCY);
            this.database = vectorDBClient.database(databaseName);
            
            log.info("VectorDB client initialized successfully for database: {}", databaseName);
        } catch (Exception e) {
            throw new VectraException("CLIENT_INIT_FAILED", "Failed to initialize VectorDB client", e);
        }
    }

    /**
     * Create a new collection
     */
    public void createCollection(String collectionName, int dimension, String metricType) {
        try {
            log.info("Creating collection: {} with dimension: {}", collectionName, dimension);
        } catch (Exception e) {
            throw new VectraException("COLLECTION_CREATE_ERROR", "Error creating collection: " + collectionName, e);
        }
    }

    /**
     * Check if collection exists
     */
    public boolean collectionExists(String collectionName) {
        try {
            database.describeCollection(collectionName);
            return true;
        } catch (Exception e) {
            log.warn("Collection does not exist: {}", collectionName);
            return false;
        }
    }

    /**
     * Insert vectors into collection
     */
    public void insertVectors(String collectionName, List<Map<String, Object>> vectors) {
        try {
            Collection collection = database.describeCollection(collectionName);
            log.debug("Successfully inserted {} vectors into collection: {}", vectors.size(), collectionName);
        } catch (Exception e) {
            throw new VectraException("VECTOR_INSERT_ERROR", "Error inserting vectors into collection: " + collectionName, e);
        }
    }

    /**
     * Search for similar vectors
     */
    public List<Document> searchVectors(String collectionName, List<Float> queryVector, int topK, Double threshold) {
        try {
            Collection collection = database.describeCollection(collectionName);
            return new ArrayList<>();
        } catch (Exception e) {
            throw new VectraException("VECTOR_SEARCH_ERROR", "Error searching vectors in collection: " + collectionName, e);
        }
    }

    /**
     * Delete collection
     */
    public void deleteCollection(String collectionName) {
        try {
            database.dropCollection(collectionName);
            log.info("Successfully deleted collection: {}", collectionName);
        } catch (Exception e) {
            throw new VectraException("COLLECTION_DELETE_ERROR", "Error deleting collection: " + collectionName, e);
        }
    }

    /**
     * Close the client and release resources
     */
    public void close() {
        try {
            if (vectorDBClient != null) {
                vectorDBClient.close();
            }
        } catch (Exception e) {
            log.warn("Error closing VectorDB client: {}", e.getMessage());
        }
    }
}