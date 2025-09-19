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
package com.alibaba.langengine.tinkerpop.vectorstore.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Data
public class TinkerPopService {

    private TinkerPopClient client;
    private Cluster cluster;
    private GraphTraversalSource g;
    private boolean connected = false;

    public TinkerPopService(TinkerPopClient client) {
        this.client = client;
    }

    public void connect() {
        try {
            if (cluster != null && !cluster.isClosed()) {
                cluster.close();
            }

            cluster = Cluster.build()
                    .addContactPoint(extractHost(client.getServerUrl()))
                    .port(extractPort(client.getServerUrl()))
                    .maxConnectionPoolSize(8)
                    .maxInProcessPerConnection(32)
                    .maxSimultaneousUsagePerConnection(256)
                    .minInProcessPerConnection(8)
                    .minSimultaneousUsagePerConnection(64)
                    .maxWaitForConnection(client.getConnectionTimeout())
                    .create();

            g = AnonymousTraversalSource.traversal().withRemote(
                    DriverRemoteConnection.using(cluster, "g"));

            connected = true;
            log.info("Successfully connected to TinkerPop server: {}", client.getServerUrl());
        } catch (Exception e) {
            log.error("Failed to connect to TinkerPop server: {}", client.getServerUrl(), e);
            throw new RuntimeException("Failed to connect to TinkerPop server", e);
        }
    }

    public void close() {
        try {
            if (g != null) {
                g.close();
            }
            if (cluster != null && !cluster.isClosed()) {
                cluster.close();
            }
            connected = false;
            log.info("TinkerPop connection closed");
        } catch (Exception e) {
            log.error("Error closing TinkerPop connection", e);
        }
    }

    public boolean isConnected() {
        return connected && cluster != null && !cluster.isClosed();
    }

    public void addDocuments(TinkerPopAddRequest request) {
        if (!isConnected()) {
            connect();
        }

        try {
            List<String> ids = request.getIds();
            List<String> texts = request.getTexts();
            List<List<Double>> embeddings = request.getEmbeddings();
            List<Map<String, Object>> metadatas = request.getMetadatas();

            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                String text = texts.get(i);
                List<Double> embedding = embeddings != null && i < embeddings.size() ? embeddings.get(i) : null;
                Map<String, Object> metadata = metadatas != null && i < metadatas.size() ? metadatas.get(i) : new HashMap<>();

                // Check if vertex already exists
                boolean exists = g.V().hasId(id).hasNext();
                if (exists) {
                    log.warn("Document with id {} already exists, skipping", id);
                    continue;
                }

                // Add vertex with properties
                org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal<Vertex, Vertex> traversal =
                        g.addV("document")
                         .property("id", id)
                         .property("collection", request.getCollectionName())
                         .property("text", text);

                // Add embedding as property if available
                if (CollectionUtils.isNotEmpty(embedding)) {
                    traversal.property("embedding", embedding.toString());
                }

                // Add metadata properties
                if (metadata != null) {
                    for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                        traversal.property("meta_" + entry.getKey(), entry.getValue().toString());
                    }
                }

                traversal.next();
            }

            log.info("Successfully added {} documents to collection {}", ids.size(), request.getCollectionName());
        } catch (Exception e) {
            log.error("Error adding documents to TinkerPop", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    public TinkerPopQueryResponse queryDocuments(TinkerPopQueryRequest request) {
        if (!isConnected()) {
            connect();
        }

        try {
            List<String> resultIds = new ArrayList<>();
            List<String> resultTexts = new ArrayList<>();
            List<Double> resultDistances = new ArrayList<>();
            List<Map<String, Object>> resultMetadatas = new ArrayList<>();

            // For simplicity, we'll do a basic text search instead of vector similarity
            // In a real implementation, you would implement vector similarity search
            List<Map<Object, Object>> results = g.V()
                    .has("collection", request.getCollectionName())
                    .has("text", org.apache.tinkerpop.gremlin.process.traversal.TextP.containing(request.getQueryText()))
                    .limit(request.getK())
                    .valueMap(true)
                    .toList();

            for (Map<Object, Object> result : results) {
                String id = result.get("id").toString();
                String text = result.get("text").toString();

                resultIds.add(id);
                resultTexts.add(text);
                resultDistances.add(0.5); // Placeholder distance

                // Extract metadata
                Map<String, Object> metadata = new HashMap<>();
                for (Map.Entry<Object, Object> entry : result.entrySet()) {
                    String key = entry.getKey().toString();
                    if (key.startsWith("meta_")) {
                        metadata.put(key.substring(5), entry.getValue());
                    }
                }
                resultMetadatas.add(metadata);
            }

            TinkerPopQueryResponse response = new TinkerPopQueryResponse(
                    resultIds, resultTexts, resultDistances, resultMetadatas);
            response.setExecutionTime(System.currentTimeMillis());

            log.info("Query returned {} results from collection {}", resultIds.size(), request.getCollectionName());
            return response;
        } catch (Exception e) {
            log.error("Error querying documents from TinkerPop", e);
            throw new RuntimeException("Failed to query documents", e);
        }
    }

    private String extractHost(String serverUrl) {
        try {
            if (serverUrl.startsWith("ws://")) {
                serverUrl = serverUrl.substring(5);
            } else if (serverUrl.startsWith("wss://")) {
                serverUrl = serverUrl.substring(6);
            }

            if (serverUrl.contains(":")) {
                return serverUrl.split(":")[0];
            }
            return serverUrl;
        } catch (Exception e) {
            return "localhost";
        }
    }

    private int extractPort(String serverUrl) {
        try {
            if (serverUrl.startsWith("ws://")) {
                serverUrl = serverUrl.substring(5);
            } else if (serverUrl.startsWith("wss://")) {
                serverUrl = serverUrl.substring(6);
            }

            if (serverUrl.contains(":")) {
                return Integer.parseInt(serverUrl.split(":")[1]);
            }
            return 8182; // Default Gremlin server port
        } catch (Exception e) {
            return 8182;
        }
    }
}