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
package com.alibaba.langengine.tinkerpop.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.tinkerpop.vectorstore.service.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.langengine.tinkerpop.TinkerPopConfiguration;
import java.util.regex.Pattern;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class TinkerPop extends VectorStore {

    // Collection ID validation pattern
    private static final Pattern COLLECTION_ID_PATTERN = Pattern.compile("^[a-z0-9]([a-z0-9._-]*[a-z0-9])?$");
    private static final int MIN_COLLECTION_ID_LENGTH = 3;
    private static final int MAX_COLLECTION_ID_LENGTH = 63;

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 标识一个唯一的仓库，可以看做是某个业务，向量内容的集合标识；某个知识库内容，这个知识库所有的内容都应该是相同的collectionId
     *
     * 名称的长度必须在 3 到 63 个字符之间。
     * 名称必须以小写字母或数字开头和结尾，并且可以在中间包含点、破折号和下划线。
     * 名称不能包含两个连续的点。
     * 名称不能是有效的 IP 地址。
     */
    private String collectionId;

    /**
     * 内部使用的client，不希望对外暴露
     */
    private TinkerPopClient _client;

    /**
     * 内部使用的服务，不希望对外暴露
     */
    private TinkerPopService _service;

    public TinkerPop(Embeddings embedding, String collectionId) {
        this(TinkerPopConfiguration.getTinkerPopServerUrl(), embedding, collectionId,
             TinkerPopConfiguration.getTinkerPopConnectionTimeout(),
             TinkerPopConfiguration.getTinkerPopRequestTimeout());
    }

    public TinkerPop(String serverUrl, Embeddings embedding, String collectionId) {
        this(serverUrl, embedding, collectionId,
             TinkerPopConfiguration.getTinkerPopConnectionTimeout(),
             TinkerPopConfiguration.getTinkerPopRequestTimeout());
    }

    public TinkerPop(String serverUrl, Embeddings embedding, String collectionId,
                     int connectionTimeout, int requestTimeout) {
        if (embedding == null) {
            throw new IllegalArgumentException("Embedding model cannot be null");
        }
        if (StringUtils.isBlank(serverUrl)) {
            throw new IllegalArgumentException("Server URL cannot be null or empty");
        }
        
        this.collectionId = validateAndSetCollectionId(collectionId);
        this.embedding = embedding;
        
        try {
            this._client = new TinkerPopClient(serverUrl, connectionTimeout, requestTimeout);
            this._service = new TinkerPopService(_client);
            this._service.connect();
            log.info("Successfully initialized TinkerPop with collection: {}", this.collectionId);
        } catch (Exception e) {
            log.error("Failed to initialize TinkerPop service for collection: {}", this.collectionId, e);
            throw new RuntimeException("Failed to initialize TinkerPop service", e);
        }
    }

    /**
     * Validate and set collection ID with proper validation rules
     */
    private String validateAndSetCollectionId(String collectionId) {
        if (StringUtils.isBlank(collectionId)) {
            String generated = "collection_" + UUID.randomUUID().toString().replace("-", "_");
            log.info("No collection ID provided, generated: {}", generated);
            return generated;
        }
        
        if (collectionId.length() < MIN_COLLECTION_ID_LENGTH || collectionId.length() > MAX_COLLECTION_ID_LENGTH) {
            throw new IllegalArgumentException(String.format(
                "Collection ID length must be between %d and %d characters: %s",
                MIN_COLLECTION_ID_LENGTH, MAX_COLLECTION_ID_LENGTH, collectionId));
        }
        
        if (!COLLECTION_ID_PATTERN.matcher(collectionId).matches()) {
            throw new IllegalArgumentException(
                "Collection ID must start and end with lowercase letter or number, " +
                "and can contain dots, dashes, and underscores in the middle: " + collectionId);
        }
        
        if (collectionId.contains("..")) {
            throw new IllegalArgumentException("Collection ID cannot contain consecutive dots: " + collectionId);
        }
        
        // Check if it's a valid IP address (not allowed)
        if (isValidIPAddress(collectionId)) {
            throw new IllegalArgumentException("Collection ID cannot be a valid IP address: " + collectionId);
        }
        
        return collectionId;
    }

    /**
     * Check if a string is a valid IP address
     */
    private boolean isValidIPAddress(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) return false;
            
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 添加文本向量，如果没有向量，系统会自动的使用embedding生成向量
     *
     * @param documents
     */
    @Override
    public void addDocuments(List<Document> documents) {
        try {
            List<String> ids = new ArrayList<>();
            List<String> texts = new ArrayList<>();
            List<List<Double>> embeddings = new ArrayList<>();
            List<Map<String, Object>> metadatas = new ArrayList<>();

            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
                if (StringUtils.isEmpty(document.getPageContent())) {
                    log.warn("Skipping document with empty content, ID: {}", document.getUniqueId());
                    continue;
                }
                if (MapUtils.isEmpty(document.getMetadata())) {
                    document.setMetadata(new HashMap<>());
                }

                ids.add(document.getUniqueId());
                texts.add(document.getPageContent());

                List<Double> embedding = document.getEmbedding();
                if (CollectionUtils.isNotEmpty(embedding)) {
                    embeddings.add(embedding);
                } else {
                    embeddings.add(new ArrayList<>());
                }

                metadatas.add(document.getMetadata());
            }

            // Generate embeddings if not present
            if (this.embedding != null) {
                List<Document> embeddedDocs = this.embedding.embedTexts(texts);
                for (int i = 0; i < embeddedDocs.size(); i++) {
                    if (CollectionUtils.isEmpty(embeddings.get(i))) {
                        embeddings.set(i, embeddedDocs.get(i).getEmbedding());
                    }
                }
            }

            TinkerPopAddRequest request = new TinkerPopAddRequest(
                    collectionId, ids, texts, embeddings, metadatas);
            _service.addDocuments(request);

        } catch (Exception e) {
            log.error("Error adding documents to TinkerPop", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加文本向量
     *
     * @param texts
     * @param metadatas
     * @param ids
     * @return
     */
    public List<String> addTexts(
            Iterable<String> texts,
            List<Map<String, Object>> metadatas,
            List<String> ids
    ) {
        // Handle the case where the user doesn't provide ids on the Collection
        if (ids == null) {
            ids = new ArrayList<>();
            Iterator<String> textIterator = texts.iterator();
            while (textIterator.hasNext()) {
                textIterator.next(); // Iterate through texts
                ids.add(UUID.randomUUID().toString());
            }
        }

        List<Document> embeddingDocs = null;
        List<String> textsList = new ArrayList<>();
        texts.forEach(textsList::add);

        if (this.embedding != null) {
            embeddingDocs = this.embedding.embedTexts(textsList);
        }

        List<List<Double>> embeddings = embeddingDocs != null
                ? embeddingDocs.stream()
                .map(Document::getEmbedding)
                .collect(Collectors.toList())
                : new ArrayList<>();

        try {
            TinkerPopAddRequest request = new TinkerPopAddRequest(
                    collectionId, ids, textsList, embeddings, metadatas);
            _service.addDocuments(request);
        } catch (Exception e) {
            log.error("Error adding texts to TinkerPop", e);
            throw new RuntimeException(e);
        }

        return ids;
    }

    /**
     * TinkerPop向量库查询
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        try {
            List<Double> queryEmbedding = null;
            if (this.embedding != null) {
                List<Document> embeddedDocs = this.embedding.embedTexts(Arrays.asList(query));
                if (CollectionUtils.isNotEmpty(embeddedDocs)) {
                    queryEmbedding = embeddedDocs.get(0).getEmbedding();
                }
            }

            TinkerPopQueryRequest request = new TinkerPopQueryRequest(collectionId, query, k);
            request.setQueryEmbedding(queryEmbedding);
            request.setMaxDistance(maxDistanceValue);
            request.setType(type);

            TinkerPopQueryResponse response = _service.queryDocuments(request);

            List<Document> documents = new ArrayList<>();
            List<String> ids = response.getIds();
            List<String> texts = response.getTexts();
            List<Double> distances = response.getDistances();
            List<Map<String, Object>> metadatas = response.getMetadatas();

            for (int i = 0; i < ids.size(); i++) {
                if (maxDistanceValue != null && distances.get(i) > maxDistanceValue) {
                    continue;
                }

                Document document = new Document();
                document.setUniqueId(ids.get(i));
                document.setPageContent(filter(texts.get(i)));
                document.setScore(distances.get(i));
                document.setMetadata(metadatas.get(i));

                documents.add(document);
            }

            return documents;
        } catch (Exception e) {
            log.error("Error performing similarity search in TinkerPop", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (_service != null) {
            _service.close();
        }
    }

    private String filter(String value) {
        if (value == null) {
            return "";
        }
        // 去掉所有HTML标签 (简单的正则表达式过滤)
        value = value.replaceAll("<[^>]+>", "");
        // 去掉HTML实体 (基本的实体处理)
        value = value.replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'")
                    .replace("&nbsp;", " ");
        return value;
    }
}