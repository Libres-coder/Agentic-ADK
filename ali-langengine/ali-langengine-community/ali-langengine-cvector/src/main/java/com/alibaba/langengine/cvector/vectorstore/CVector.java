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
package com.alibaba.langengine.cvector.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.cvector.CVectorConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class CVector extends VectorStore {

    private Embeddings embedding;

    private final String collection;

    private static final String CVECTOR_SERVER_URL = CVectorConfiguration.CVECTOR_SERVER_URL;

    private static final String CVECTOR_API_KEY = CVectorConfiguration.CVECTOR_API_KEY;

    private String upsertUrl;

    private String queryUrl;

    private AsyncHttpClient httpClient;

    public CVector(String collection) {
        this.collection = collection == null ? "default" : collection;
        validateAndSetUrls(CVECTOR_SERVER_URL);
        this.httpClient = new DefaultAsyncHttpClient();
    }

    public CVector(String serverUrl, String collection) {
        this.collection = collection == null ? "default" : collection;
        validateAndSetUrls(serverUrl);
        this.httpClient = new DefaultAsyncHttpClient();
    }

    private void validateAndSetUrls(String serverUrl) {
        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            throw new CVectorException("Server URL cannot be null or empty");
        }
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            throw new CVectorException("Server URL must use HTTP or HTTPS protocol");
        }
        this.upsertUrl = serverUrl + "/vectors/upsert";
        this.queryUrl = serverUrl + "/vectors/query";
    }

    @Override
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        if (embedding == null) {
            throw new CVectorException("Embedding is not configured");
        }

        documents = embedding.embedDocument(documents);
        if (documents.isEmpty()) {
            return;
        }

        CVectorUpsertRequest request = buildUpsertRequest(documents);
        executeUpsertRequest(request);
    }

    private CVectorUpsertRequest buildUpsertRequest(List<Document> documents) {
        CVectorUpsertRequest request = new CVectorUpsertRequest();
        request.setCollection(collection);
        request.setDocuments(new ArrayList<>());

        for (Document document : documents) {
            if (document.getUniqueId() == null) {
                document.setUniqueId(UUID.randomUUID().toString());
            }

            CVectorUpsertRequest.CVectorDocument cvectorDoc = new CVectorUpsertRequest.CVectorDocument();
            cvectorDoc.setId(document.getUniqueId());
            cvectorDoc.setVector(document.getEmbedding());
            cvectorDoc.setContent(document.getPageContent());
            cvectorDoc.setMetadata(document.getMetadata() != null ? document.getMetadata() : new HashMap<>());

            request.getDocuments().add(cvectorDoc);
        }
        return request;
    }

    private void executeUpsertRequest(CVectorUpsertRequest request) {
        try {
            String body = JSON.toJSONString(request);
            ListenableFuture<Response> whenResponse = httpClient.preparePost(upsertUrl)
                    .setHeader("accept", "application/json")
                    .setHeader("content-type", "application/json")
                    .setHeader("Authorization", "Bearer " + CVECTOR_API_KEY)
                    .setBody(body)
                    .execute();

            Response response = whenResponse.get();
            if (response.getStatusCode() != 200) {
                throw new CVectorException("Failed to upsert documents: " + response.getResponseBody());
            }
            log.debug("Upserted {} documents to cVector", request.getDocuments().size());
        } catch (CVectorException e) {
            throw e;
        } catch (Exception e) {
            throw new CVectorException("Error adding documents to cVector", e);
        }
    }

    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (embedding == null) {
            throw new CVectorException("Embedding is not configured");
        }
        
        List<String> embeddingStrings = embedding.embedQuery(query, k);
        if (embeddingStrings.isEmpty() || !embeddingStrings.get(0).startsWith("[")) {
            return new ArrayList<>();
        }

        String embeddingString = embeddingStrings.get(0);
        List<String> embeddings = JSON.parseArray(embeddingString, String.class);
        
        CVectorQueryRequest request = buildQueryRequest(k, maxDistanceValue, embeddings);
        return executeQueryRequest(request);
    }

    private CVectorQueryRequest buildQueryRequest(int k, Double maxDistanceValue, List<String> embeddings) {
        CVectorQueryRequest request = new CVectorQueryRequest();
        request.setCollection(collection);
        request.setTopK(k);
        request.setVector(embeddings.stream().map(Double::parseDouble).collect(Collectors.toList()));
        request.setIncludeMetadata(true);
        if (maxDistanceValue != null) {
            request.setThreshold(maxDistanceValue);
        }
        return request;
    }

    private List<Document> executeQueryRequest(CVectorQueryRequest request) {
        try {
            String body = JSON.toJSONString(request);
            ListenableFuture<Response> whenResponse = httpClient.preparePost(queryUrl)
                    .setHeader("accept", "application/json")
                    .setHeader("content-type", "application/json")
                    .setHeader("Authorization", "Bearer " + CVECTOR_API_KEY)
                    .setBody(body)
                    .execute();

            Response response = whenResponse.get();
            if (response.getStatusCode() != 200) {
                throw new CVectorException("Failed to query cVector: " + response.getResponseBody());
            }

            CVectorQueryResponse queryResponse = JSON.parseObject(response.getResponseBody(), CVectorQueryResponse.class);
            if (queryResponse == null || queryResponse.getMatches() == null) {
                return new ArrayList<>();
            }

            return queryResponse.getMatches().stream().map(this::convertToDocument).collect(Collectors.toList());
        } catch (CVectorException e) {
            throw e;
        } catch (Exception e) {
            throw new CVectorException("Error querying cVector", e);
        }
    }

    private Document convertToDocument(CVectorQueryResponse.CVectorMatch match) {
        Document document = new Document();
        document.setUniqueId(match.getId());
        document.setPageContent(match.getContent());
        document.setScore(match.getScore());
        document.setMetadata(match.getMetadata());
        return document;
    }

    public void close() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.warn("Error closing HTTP client", e);
            }
        }
    }

    // For testing purposes
    protected AsyncHttpClient getHttpClient() {
        return httpClient;
    }
}