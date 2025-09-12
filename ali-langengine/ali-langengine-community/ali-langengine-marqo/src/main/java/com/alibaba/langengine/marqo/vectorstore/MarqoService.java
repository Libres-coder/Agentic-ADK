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
package com.alibaba.langengine.marqo.vectorstore;

import com.alibaba.langengine.core.indexes.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Data
public class MarqoService {

    private String indexName;
    private String url;
    private String apiKey;
    private MarqoParam marqoParam;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public MarqoService(String url, String indexName, MarqoParam marqoParam) {
        this(url, null, indexName, marqoParam);
    }

    public MarqoService(String url, String apiKey, String indexName, MarqoParam marqoParam) {
        this.url = url;
        this.apiKey = apiKey;
        this.indexName = indexName;
        this.marqoParam = marqoParam != null ? marqoParam : new MarqoParam();
        this.objectMapper = new ObjectMapper();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        if (StringUtils.isNotBlank(apiKey)) {
            clientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("x-api-key", apiKey);
                return chain.proceed(requestBuilder.build());
            });
        }

        this.client = clientBuilder.build();
    }

    public void init() {
        try {
            // Check if index exists
            if (indexExists()) {
                log.info("Index {} already exists", indexName);
                return;
            }

            // Create new index
            createIndex();
            log.info("Successfully created Marqo index: {}", indexName);
        } catch (Exception e) {
            log.error("Marqo Service init failed", e);
            throw new RuntimeException("Failed to initialize Marqo service", e);
        }
    }

    private boolean indexExists() throws IOException {
        Request request = new Request.Builder()
                .url(url + "/indexes/" + indexName)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    private void createIndex() throws IOException {
        MarqoParam.InitParam initParam = marqoParam.getInitParam();
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("model", initParam.getModel());
        settings.put("metric", initParam.getMetric());
        settings.put("numberOfVectors", initParam.getNumberOfVectors());
        settings.put("numberOfShards", initParam.getNumberOfShards());
        settings.put("numberOfReplicas", initParam.getNumberOfReplicas());
        settings.put("treatUrlsAndPointersAsImages", initParam.getTreatUrlsAndPointersAsImages());
        settings.put("normalizeEmbeddings", initParam.getNormalizeEmbeddings());

        Map<String, Object> textPreprocessing = new HashMap<>();
        textPreprocessing.put("splitLength", initParam.getTextPreprocessing().getSplitLength());
        textPreprocessing.put("splitOverlap", initParam.getTextPreprocessing().getSplitOverlap());
        textPreprocessing.put("splitMethod", initParam.getTextPreprocessing().getSplitMethod());
        settings.put("textPreprocessing", textPreprocessing);

        Map<String, Object> imagePreprocessing = new HashMap<>();
        imagePreprocessing.put("patchMethod", initParam.getImagePreprocessing().getPatchMethod());
        settings.put("imagePreprocessing", imagePreprocessing);

        Map<String, Object> indexSettings = new HashMap<>();
        indexSettings.put("settings", settings);

        String json = objectMapper.writeValueAsString(indexSettings);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url + "/indexes/" + indexName)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to create index: " + response);
            }
        }
    }

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            String contentField = marqoParam.getFieldNamePageContent();
            String idField = marqoParam.getFieldNameUniqueId();
            String metaField = marqoParam.getFieldMeta();

            List<Map<String, Object>> marqoDocuments = new ArrayList<>();

            for (Document doc : documents) {
                Map<String, Object> marqoDoc = new HashMap<>();

                if (StringUtils.isNotBlank(doc.getUniqueId())) {
                    marqoDoc.put("_id", doc.getUniqueId());
                    marqoDoc.put(idField, doc.getUniqueId());
                }

                if (StringUtils.isNotBlank(doc.getPageContent())) {
                    marqoDoc.put(contentField, doc.getPageContent());
                }

                if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                    marqoDoc.put(metaField, doc.getMetadata());
                }

                marqoDocuments.add(marqoDoc);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("documents", marqoDocuments);
            requestBody.put("tensorFields", Collections.singletonList(contentField));

            String json = objectMapper.writeValueAsString(requestBody);

            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url + "/indexes/" + indexName + "/documents")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to add documents: " + response);
                }
            }

            log.info("Successfully added {} documents to Marqo index {}", documents.size(), indexName);
        } catch (Exception e) {
            log.error("Failed to add documents to Marqo", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> searchRequest = new HashMap<>();
            searchRequest.put("q", query);
            searchRequest.put("limit", k);
            searchRequest.put("searchMethod", "TENSOR");

            if (maxDistanceValue != null) {
                // Marqo uses score-based filtering
                searchRequest.put("filter", "_score:[" + (1.0 - maxDistanceValue) + " TO 1.0]");
            }

            String json = objectMapper.writeValueAsString(searchRequest);

            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url + "/indexes/" + indexName + "/search")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to perform search: " + response);
                }

                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                JsonNode hits = jsonResponse.get("hits");

                if (hits == null || !hits.isArray()) {
                    return Collections.emptyList();
                }

                String contentField = marqoParam.getFieldNamePageContent();
                String idField = marqoParam.getFieldNameUniqueId();
                String metaField = marqoParam.getFieldMeta();

                List<Document> results = new ArrayList<>();
                for (JsonNode hit : hits) {
                    Document doc = new Document();
                    
                    // Set unique ID
                    JsonNode idNode = hit.get("_id");
                    if (idNode != null) {
                        doc.setUniqueId(idNode.asText());
                    } else {
                        JsonNode docIdNode = hit.get(idField);
                        if (docIdNode != null) {
                            doc.setUniqueId(docIdNode.asText());
                        }
                    }

                    // Set page content
                    JsonNode contentNode = hit.get(contentField);
                    if (contentNode != null) {
                        doc.setPageContent(contentNode.asText());
                    }

                    // Set metadata
                    JsonNode metaNode = hit.get(metaField);
                    if (metaNode != null && metaNode.isObject()) {
                        Map<String, Object> metadata = objectMapper.convertValue(metaNode, Map.class);
                        doc.setMetadata(metadata);
                    }

                    // Set score (convert from Marqo score to distance-like metric)
                    JsonNode scoreNode = hit.get("_score");
                    if (scoreNode != null) {
                        double score = scoreNode.asDouble();
                        doc.setScore(1.0 - score); // Convert to distance-like metric
                    }

                    results.add(doc);
                }

                return results;
            }

        } catch (Exception e) {
            log.error("Failed to perform similarity search in Marqo", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    public void dropIndex() {
        try {
            Request request = new Request.Builder()
                    .url(url + "/indexes/" + indexName)
                    .delete()
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to delete index: " + response);
                }
            }
            
            log.info("Successfully deleted Marqo index: {}", indexName);
        } catch (Exception e) {
            log.error("Failed to delete Marqo index: {}", indexName, e);
            throw new RuntimeException("Failed to delete index", e);
        }
    }
}
