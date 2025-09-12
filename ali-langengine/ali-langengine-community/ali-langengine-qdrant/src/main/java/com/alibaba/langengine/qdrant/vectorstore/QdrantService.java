/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.qdrant.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Qdrant service for vector operations
 *
 * @author zh_xiaoji
 */
@Slf4j
@Data
public class QdrantService {

    private String baseUrl;
    private String apiKey;
    private OkHttpClient httpClient;

    public QdrantService(String serverUrl) {
        this.baseUrl = parseServerUrl(serverUrl);
        initialize();
    }

    public QdrantService(String serverUrl, String apiKey) {
        this.baseUrl = parseServerUrl(serverUrl);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty when using authenticated constructor");
        }
        this.apiKey = apiKey;
        initialize();
    }

    /**
     * Parse server URL to ensure proper format
     */
    private String parseServerUrl(String serverUrl) {
        if (serverUrl.startsWith("http://") || serverUrl.startsWith("https://")) {
            return serverUrl;
        } else {
            // Assume host:port format
            return "http://" + serverUrl;
        }
    }

    /**
     * Initialize Qdrant client
     */
    public void initialize() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (apiKey != null && !apiKey.isEmpty()) {
            builder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("api-key", apiKey);
                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }

        this.httpClient = builder.build();
        log.info("Qdrant service initialized with endpoint: {}", baseUrl);
    }

    /**
     * Test connection to Qdrant
     */
    public boolean isHealthy() {
        try {
            // Use root path for health check since /health endpoint doesn't exist
            Request request = new Request.Builder()
                    .url(baseUrl + "/")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                if (success && response.body() != null) {
                    String responseBody = response.body().string();
                    log.info("Qdrant connection test: SUCCESS (status: {}, response: {})",
                            response.code(), responseBody);
                } else {
                    log.info("Qdrant connection test: {} (status: {})",
                            success ? "SUCCESS" : "FAILED", response.code());
                }
                return success;
            }
        } catch (IOException e) {
            log.error("Failed to connect to Qdrant at {}: {}", baseUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Get Qdrant cluster info
     */
    public String getClusterInfo() {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/cluster")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            log.error("Failed to get cluster info: {}", e.getMessage());
        }
        return null;
    }

    /**
     * List all collections
     */
    public String listCollections() {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/collections")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            log.error("Failed to list collections: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Check if collection exists
     */
    public boolean collectionExists(String collectionName) {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            log.error("Failed to check collection existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get collection info
     */
    public Map<String, Object> getCollectionInfo(String collectionName) {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = JSON.parseObject(responseBody);
                    if (jsonResponse.containsKey("result")) {
                        return jsonResponse.getJSONObject("result").getInnerMap();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get collection info: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Create collection
     */
    public boolean createCollection(String collectionName, int vectorSize, String distanceMetric) {
        try {
            JSONObject payload = new JSONObject();

            // Vector configuration
            JSONObject vectorConfig = new JSONObject();
            vectorConfig.put("size", vectorSize);
            vectorConfig.put("distance", distanceMetric != null ? distanceMetric : "Cosine");
            payload.put("vectors", vectorConfig);

            RequestBody body = RequestBody.create(
                    payload.toJSONString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName)
                    .put(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                if (success) {
                    log.info("Collection '{}' created successfully", collectionName);
                } else {
                    log.error("Failed to create collection '{}': {}", collectionName, response.code());
                }
                return success;
            }
        } catch (Exception e) {
            log.error("Failed to create collection '{}': {}", collectionName, e.getMessage());
            return false;
        }
    }

    /**
     * Delete collection
     */
    public boolean deleteCollection(String collectionName) {
        try {
            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName)
                    .delete()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                if (success) {
                    log.info("Collection '{}' deleted successfully", collectionName);
                } else {
                    log.error("Failed to delete collection '{}': {}", collectionName, response.code());
                }
                return success;
            }
        } catch (IOException e) {
            log.error("Failed to delete collection '{}': {}", collectionName, e.getMessage());
            return false;
        }
    }

    /**
     * Convert Document to Qdrant point JSON object
     */
    private JSONObject documentToPoint(Document document) {
        JSONObject point = new JSONObject();

        // Generate ID if not present
        String id = StringUtils.isNotEmpty(document.getUniqueId())
                ? document.getUniqueId()
                : UUID.randomUUID().toString();
        point.put("id", id);

        // Vector from embedding
        if (document.getEmbedding() != null && !document.getEmbedding().isEmpty()) {
            List<Float> vector = document.getEmbedding().stream()
                    .map(Double::floatValue)
                    .collect(Collectors.toList());
            point.put("vector", vector);
        }

        // Payload (metadata + content)
        JSONObject pointPayload = new JSONObject();
        pointPayload.put("content", document.getPageContent());
        if (document.getMetadata() != null) {
            pointPayload.putAll(document.getMetadata());
        }
        point.put("payload", pointPayload);

        return point;
    }

    /**
     * Convert Qdrant search result JSON to Document object
     */
    private Document jsonResultToDocument(JSONObject result) {
        Document document = new Document();

        // Set ID
        document.setUniqueId(result.getString("id"));

        // Set score
        document.setScore(result.getDouble("score"));

        // Set content and metadata from payload
        if (result.containsKey("payload")) {
            JSONObject payloadObj = result.getJSONObject("payload");
            if (payloadObj.containsKey("content")) {
                document.setPageContent(payloadObj.getString("content"));
            }

            // Set metadata (exclude content field) using stream
            Map<String, Object> metadata = payloadObj.keySet().stream()
                    .filter(key -> !"content".equals(key))
                    .collect(Collectors.toMap(key -> key, payloadObj::get));
            document.setMetadata(metadata);
        }

        return document;
    }

    /**
     * Add points (documents with vectors) to collection
     */
    public boolean addPoints(String collectionName, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return true;
        }

        try {
            JSONObject payload = new JSONObject();

            // Convert documents to points using stream
            JSONArray points = documents.stream()
                    .map(this::documentToPoint)
                    .collect(JSONArray::new, JSONArray::add, JSONArray::addAll);

            payload.put("points", points);

            RequestBody body = RequestBody.create(
                    payload.toJSONString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName + "/points")
                    .put(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                if (success) {
                    log.info("Added {} points to collection '{}'", documents.size(), collectionName);
                } else {
                    log.error("Failed to add points to collection '{}': {}", collectionName, response.code());
                    if (response.body() != null) {
                        log.error("Response body: {}", response.body().string());
                    }
                }
                return success;
            }
        } catch (Exception e) {
            log.error("Failed to add points to collection '{}': {}", collectionName, e.getMessage());
            return false;
        }
    }

    /**
     * Search for similar vectors
     */
    public List<Document> searchSimilar(String collectionName, List<Float> queryVector, int limit, Double scoreThreshold) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("vector", queryVector);
            payload.put("limit", limit);
            payload.put("with_payload", true);
            payload.put("with_vector", false);

            if (scoreThreshold != null) {
                payload.put("score_threshold", scoreThreshold);
            }

            RequestBody body = RequestBody.create(
                    payload.toJSONString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName + "/points/search")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = JSON.parseObject(responseBody);

                    if (jsonResponse.containsKey("result")) {
                        JSONArray results = jsonResponse.getJSONArray("result");

                        // Convert JSON results to Documents using stream
                        return IntStream.range(0, results.size())
                                .mapToObj(results::getJSONObject)
                                .map(this::jsonResultToDocument)
                                .collect(Collectors.toList());
                    }
                } else {
                    log.error("Search failed with status: {}", response.code());
                }
            }
        } catch (Exception e) {
            log.error("Failed to search in collection '{}': {}", collectionName, e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * Count points in collection
     */
    public long countPoints(String collectionName) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("exact", true);

            RequestBody body = RequestBody.create(
                    payload.toJSONString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(baseUrl + "/collections/" + collectionName + "/points/count")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = JSON.parseObject(responseBody);
                    if (jsonResponse.containsKey("result")) {
                        JSONObject result = jsonResponse.getJSONObject("result");
                        return result.getLongValue("count");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to count points in collection '{}': {}", collectionName, e.getMessage());
        }
        return 0;
    }
}
