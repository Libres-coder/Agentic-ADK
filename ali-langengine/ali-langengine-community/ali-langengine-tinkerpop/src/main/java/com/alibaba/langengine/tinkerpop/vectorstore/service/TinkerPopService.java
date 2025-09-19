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
                    // Store embedding as a more structured format
                    traversal.property("embedding", embedding.toString());
                    traversal.property("embedding_size", embedding.size());
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

            List<Double> queryEmbedding = request.getQueryEmbedding();
            
            if (queryEmbedding != null && !queryEmbedding.isEmpty()) {
                // Vector similarity search implementation
                log.debug("Performing vector similarity search for collection: {}", request.getCollectionName());
                
                List<Map<Object, Object>> allResults = g.V()
                        .has("collection", request.getCollectionName())
                        .has("embedding")
                        .valueMap()
                        .toList();

                // Calculate cosine similarity and sort results
                List<VectorSearchResult> vectorResults = new ArrayList<>();
                
                for (Map<Object, Object> result : allResults) {
                    try {
                        String embeddingStr = result.get("embedding").toString();
                        List<Double> docEmbedding = parseEmbedding(embeddingStr);
                        
                        if (docEmbedding != null && docEmbedding.size() == queryEmbedding.size()) {
                            double similarity = calculateCosineSimilarity(queryEmbedding, docEmbedding);
                            double distance = 1.0 - similarity; // Convert similarity to distance
                            
                            VectorSearchResult vectorResult = new VectorSearchResult(result, distance);
                            vectorResults.add(vectorResult);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to process embedding for document, skipping", e);
                    }
                }

                // Sort by distance (ascending) and limit results
                vectorResults.sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
                vectorResults = vectorResults.subList(0, Math.min(request.getK(), vectorResults.size()));

                // Build response from sorted results
                for (VectorSearchResult vectorResult : vectorResults) {
                    Map<Object, Object> result = vectorResult.getResult();
                    String id = result.get("id").toString();
                    String text = result.get("text").toString();

                    resultIds.add(id);
                    resultTexts.add(text);
                    resultDistances.add(vectorResult.getDistance());

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
                
                log.debug("Vector similarity search completed, found {} results", resultIds.size());
            } else {
                // Fallback to text search if no query embedding provided
                log.debug("No query embedding provided, performing text search for: {}", request.getQueryText());
                
                List<Map<Object, Object>> results = g.V()
                        .has("collection", request.getCollectionName())
                        .has("text", org.apache.tinkerpop.gremlin.process.traversal.TextP.containing(request.getQueryText()))
                        .limit(request.getK())
                        .valueMap()
                        .toList();

                for (Map<Object, Object> result : results) {
                    String id = result.get("id").toString();
                    String text = result.get("text").toString();

                    resultIds.add(id);
                    resultTexts.add(text);
                    resultDistances.add(1.0); // Default distance for text search

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
                String[] parts = serverUrl.split(":");
                if (parts.length >= 2) {
                    // Remove path part if exists (e.g., "localhost:8182/gremlin" -> "8182")
                    String portPart = parts[1].split("/")[0];
                    return Integer.parseInt(portPart);
                }
            }
            return 8182; // Default Gremlin server port
        } catch (Exception e) {
            log.warn("Failed to extract port from URL: {}, using default port 8182", serverUrl);
            return 8182;
        }
    }

    /**
     * Parse embedding from string representation
     */
    private List<Double> parseEmbedding(String embeddingStr) {
        try {
            // Remove brackets and split by comma
            embeddingStr = embeddingStr.trim();
            if (embeddingStr.startsWith("[") && embeddingStr.endsWith("]")) {
                embeddingStr = embeddingStr.substring(1, embeddingStr.length() - 1);
            }
            
            String[] parts = embeddingStr.split(",");
            List<Double> embedding = new ArrayList<>();
            
            for (String part : parts) {
                embedding.add(Double.parseDouble(part.trim()));
            }
            
            return embedding;
        } catch (Exception e) {
            log.warn("Failed to parse embedding: {}", embeddingStr, e);
            return null;
        }
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimensions");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (norm1 * norm2);
    }

    /**
     * Internal class to hold vector search results with distance
     */
    private static class VectorSearchResult {
        private final Map<Object, Object> result;
        private final double distance;

        public VectorSearchResult(Map<Object, Object> result, double distance) {
            this.result = result;
            this.distance = distance;
        }

        public Map<Object, Object> getResult() {
            return result;
        }

        public double getDistance() {
            return distance;
        }
    }
}