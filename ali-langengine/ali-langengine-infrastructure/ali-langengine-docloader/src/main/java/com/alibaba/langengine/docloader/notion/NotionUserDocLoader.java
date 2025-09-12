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
package com.alibaba.langengine.docloader.notion;

import com.alibaba.langengine.core.docloader.BaseLoader;
import com.alibaba.langengine.core.indexes.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Notion User Document Loader
 * 用于从 Notion API 加载当前用户信息
 *
 * @author disaster
 * @since 1.0
 */
@Slf4j
@Data
public class NotionUserDocLoader extends BaseLoader {

    /**
     * Notion API base URL
     */
    private static final String BASE_URL = "https://api.notion.com/v1";

    /**
     * Notion API 版本
     */
    private static final String NOTION_VERSION = "2022-06-28";

    /**
     * Notion API token
     */
    private String apiKey;

    /**
     * HTTP client for making requests
     */
    private OkHttpClient client;

    /**
     * JSON mapper for parsing responses
     */
    private ObjectMapper objectMapper;

    /**
     * 默认构造函数
     */
    public NotionUserDocLoader() {
        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 构造函数，使用指定的 API 密钥
     *
     * @param apiKey Notion API 密钥
     */
    public NotionUserDocLoader(String apiKey) {
        this();
        this.apiKey = apiKey;
    }

    /**
     * 构造函数，使用指定的 API 密钥和 OkHttpClient
     *
     * @param apiKey Notion API 密钥
     * @param client OkHttpClient 实例
     */
    public NotionUserDocLoader(String apiKey, OkHttpClient client) {
        this.apiKey = apiKey;
        this.client = client;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 从 Notion API 加载当前用户信息
     *
     * @return 包含用户信息的文档列表
     */
    @Override
    public List<Document> load() {
        try {
            // 构建请求 URL
            String url = BASE_URL + "/users/me";

            // 创建请求
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Notion-Version", NOTION_VERSION)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Notion API request failed with code: " + response.code() + ", message: " + response.message());
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new IOException("Notion API returned empty response body");
                }

                // 解析响应
                String jsonResponse = responseBody.string();
                JsonNode jsonNode = objectMapper.readTree(jsonResponse);

                // 创建文档
                return createDocuments(jsonNode, url);
            }
        } catch (Exception e) {
            log.error("Error loading Notion user information", e);
            throw new RuntimeException("Failed to load Notion user information", e);
        }
    }

    /**
     * 根据 JSON 响应创建文档列表
     *
     * @param jsonNode JSON 响应节点
     * @param source   数据源 URL
     * @return 文档列表
     */
    private List<Document> createDocuments(JsonNode jsonNode, String source) {
        List<Document> documents = new ArrayList<>();

        Document document = new Document();
        document.setPageContent(jsonNode.toString());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put("type", "notion_user");

        // 提取用户信息作为元数据
        if (jsonNode.has("object") && jsonNode.get("object").asText().equals("user")) {
            metadata.put("user_id", jsonNode.has("id") ? jsonNode.get("id").asText() : "");
            metadata.put("user_type", jsonNode.has("type") ? jsonNode.get("type").asText() : "");
            metadata.put("user_name", jsonNode.has("name") ? jsonNode.get("name").asText() : "");

            // 如果是 person 类型用户
            if (jsonNode.has("type") && jsonNode.get("type").asText().equals("person") && jsonNode.has("person")) {
                JsonNode personNode = jsonNode.get("person");
                if (personNode.has("email")) {
                    metadata.put("email", personNode.get("email").asText());
                }
            }

            // 如果是 bot 类型用户
            if (jsonNode.has("type") && jsonNode.get("type").asText().equals("bot") && jsonNode.has("bot")) {
                JsonNode botNode = jsonNode.get("bot");
                if (botNode.has("owner")) {
                    metadata.put("bot_owner", botNode.get("owner").toString());
                }
            }
        }

        document.setMetadata(metadata);
        documents.add(document);

        return documents;
    }

    @Override
    public List<Document> fetchContent(Map<String, Object> documentMeta) {
        // 支持通过 API 密钥参数加载
        if (documentMeta.containsKey("apiKey")) {
            String tempApiKey = (String) documentMeta.get("apiKey");
            String originalApiKey = this.apiKey;
            try {
                this.apiKey = tempApiKey;
                return load();
            } finally {
                this.apiKey = originalApiKey;
            }
        }
        return load();
    }
}