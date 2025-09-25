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
package com.alibaba.langengine.relevance.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.relevance.exception.RelevanceException;
import com.alibaba.langengine.relevance.model.RelevanceDocument;
import com.alibaba.langengine.relevance.model.RelevanceParam;
import com.alibaba.langengine.relevance.model.RelevanceQueryRequest;
import com.alibaba.langengine.relevance.model.RelevanceQueryResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class RelevanceService {

    private final RelevanceParam param;
    private final OkHttpClient httpClient;

    public RelevanceService(RelevanceParam param) {
        this.param = param;
        this.httpClient = createHttpClient();
        validateConfiguration();
    }

    /**
     * 创建 HTTP 客户端
     */
    private OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(param.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(param.getRequestTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(param.getRequestTimeout(), TimeUnit.MILLISECONDS);

        // 添加认证拦截器
        if (param.getApiKey() != null) {
            builder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", "Bearer " + param.getApiKey())
                        .header("Content-Type", "application/json");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }

        return builder.build();
    }

    /**
     * 验证配置参数
     */
    private void validateConfiguration() {
        if (param.getApiKey() == null || param.getApiKey().trim().isEmpty()) {
            throw RelevanceException.configurationError("API Key is required");
        }
        if (param.getProjectId() == null || param.getProjectId().trim().isEmpty()) {
            throw RelevanceException.configurationError("Project ID is required");
        }
        if (param.getDatasetName() == null || param.getDatasetName().trim().isEmpty()) {
            throw RelevanceException.configurationError("Dataset name is required");
        }
    }

    /**
     * 检查数据集是否存在
     */
    public boolean datasetExists() {
        try {
            String url = String.format("%s/v1/projects/%s/datasets/%s",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return true;
                } else if (response.code() == 404) {
                    return false;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to check dataset existence: " + e.getMessage(), e);
        }
    }

    /**
     * 创建数据集
     */
    public boolean createDataset() {
        try {
            String url = String.format("%s/v1/projects/%s/datasets",
                    param.getApiUrl(), param.getProjectId());

            JSONObject requestBody = new JSONObject();
            requestBody.put("dataset_name", param.getDatasetName());
            requestBody.put("description", "Vector dataset created by ali-langengine");

            JSONObject schema = new JSONObject();
            schema.put(param.getTextField(), new JSONObject() {{
                put("type", "text");
            }});
            schema.put(param.getVectorField(), new JSONObject() {{
                put("type", "vector");
                put("vector_length", param.getVectorSize());
            }});
            requestBody.put("schema", schema);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("Dataset '{}' created successfully", param.getDatasetName());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to create dataset: " + e.getMessage(), e);
        }
    }

    /**
     * 插入文档
     */
    public boolean insertDocuments(List<RelevanceDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return true;
        }

        try {
            String url = String.format("%s/v1/projects/%s/datasets/%s/documents/bulk_insert",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONArray documentsArray = new JSONArray();
            for (RelevanceDocument doc : documents) {
                JSONObject docJson = new JSONObject();
                docJson.put("_id", doc.getId());
                docJson.put(param.getTextField(), doc.getText());
                if (doc.getVector() != null) {
                    docJson.put(param.getVectorField(), doc.getVector());
                }
                if (doc.getMetadata() != null) {
                    for (Map.Entry<String, Object> entry : doc.getMetadata().entrySet()) {
                        docJson.put(entry.getKey(), entry.getValue());
                    }
                }
                documentsArray.add(docJson);
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("documents", documentsArray);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.debug("Successfully inserted {} documents", documents.size());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to insert documents: " + e.getMessage(), e);
        }
    }

    /**
     * 查询相似文档
     */
    public RelevanceQueryResponse queryDocuments(RelevanceQueryRequest queryRequest) {
        try {
            String url = String.format("%s/v1/projects/%s/datasets/%s/vector_search",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONObject requestBody = new JSONObject();
            requestBody.put("vector", queryRequest.getVector());
            requestBody.put("k", queryRequest.getK());
            requestBody.put("vector_field", param.getVectorField());

            if (queryRequest.getScoreThreshold() != null) {
                requestBody.put("score_threshold", queryRequest.getScoreThreshold());
            }

            if (queryRequest.getFilter() != null) {
                requestBody.put("filters", queryRequest.getFilter());
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return parseQueryResponse(response.body().string());
                } else {
                    handleErrorResponse(response);
                    return new RelevanceQueryResponse(Collections.emptyList());
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to query documents: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文档
     */
    public boolean deleteDocuments(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return true;
        }

        try {
            String url = String.format("%s/v1/projects/%s/datasets/%s/documents/bulk_delete",
                    param.getApiUrl(), param.getProjectId(), param.getDatasetName());

            JSONObject requestBody = new JSONObject();
            requestBody.put("document_ids", documentIds);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.debug("Successfully deleted {} documents", documentIds.size());
                    return true;
                } else {
                    handleErrorResponse(response);
                    return false;
                }
            }
        } catch (IOException e) {
            throw RelevanceException.connectionFailed("Failed to delete documents: " + e.getMessage(), e);
        }
    }

    /**
     * 解析查询响应
     */
    private RelevanceQueryResponse parseQueryResponse(String responseBody) {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray results = jsonResponse.getJSONArray("results");

            List<RelevanceDocument> documents = new ArrayList<>();
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    RelevanceDocument doc = new RelevanceDocument();
                    doc.setId(item.getString("_id"));
                    doc.setText(item.getString(param.getTextField()));
                    doc.setScore(item.getDouble("_vector_distance"));

                    Map<String, Object> metadata = new HashMap<>();
                    for (String key : item.keySet()) {
                        if (!key.startsWith("_") && !key.equals(param.getTextField()) && !key.equals(param.getVectorField())) {
                            metadata.put(key, item.get(key));
                        }
                    }
                    if (!metadata.isEmpty()) {
                        doc.setMetadata(metadata);
                    }

                    documents.add(doc);
                }
            }

            RelevanceQueryResponse response = new RelevanceQueryResponse(documents);
            response.setRequestId(jsonResponse.getString("request_id"));
            response.setTook(jsonResponse.getLong("took"));
            return response;

        } catch (Exception e) {
            log.error("Failed to parse query response: {}", e.getMessage(), e);
            return new RelevanceQueryResponse(Collections.emptyList());
        }
    }

    /**
     * 处理错误响应
     */
    private void handleErrorResponse(Response response) throws IOException {
        String responseBody = response.body() != null ? response.body().string() : "";
        String errorMessage = "API request failed with status: " + response.code();

        try {
            JSONObject errorJson = JSON.parseObject(responseBody);
            if (errorJson.containsKey("error")) {
                errorMessage = errorJson.getJSONObject("error").getString("message");
            }
        } catch (Exception e) {
            // 无法解析错误响应，使用默认错误信息
        }

        switch (response.code()) {
            case 400:
                throw RelevanceException.invalidParameter(errorMessage);
            case 401:
                throw RelevanceException.authenticationFailed(errorMessage);
            case 403:
                throw RelevanceException.authorizationFailed(errorMessage);
            case 404:
                throw RelevanceException.resourceNotFound(errorMessage);
            case 429:
                throw RelevanceException.rateLimitExceeded(errorMessage);
            case 500:
            case 502:
            case 503:
                throw RelevanceException.serverError(errorMessage);
            default:
                throw new RelevanceException("API_ERROR", errorMessage, response.code());
        }
    }

    /**
     * 关闭HTTP客户端
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}