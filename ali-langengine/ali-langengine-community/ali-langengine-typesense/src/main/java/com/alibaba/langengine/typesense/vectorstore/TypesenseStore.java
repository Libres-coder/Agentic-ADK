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
package com.alibaba.langengine.typesense.vectorstore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.typesense.exception.TypesenseException;
import com.alibaba.langengine.typesense.vectorstore.service.TypesenseService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.typesense.model.SearchResult;

import static com.alibaba.langengine.typesense.TypesenseConfiguration.TYPESENSE_API_KEY;
import static com.alibaba.langengine.typesense.TypesenseConfiguration.TYPESENSE_SERVER_URL;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class TypesenseStore extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 标识一个唯一的集合，可以看做是某个业务，向量内容的集合标识；某个知识库内容，这个知识库所有的内容都应该是相同的collectionName
     *
     * 名称的长度必须在 3 到 63 个字符之间。
     * 名称必须以小写字母或数字开头和结尾，并且可以在中间包含点、破折号和下划线。
     * 名称不能包含两个连续的点。
     * 名称不能是有效的 IP 地址。
     */
    private String collectionName;

    /**
     * 内部使用的service，不希望对外暴露
     */
    private TypesenseService service;

    /**
     * 向量维度，默认为1536（OpenAI embedding维度）
     */
    private int dimensions = 1536;

    public TypesenseStore(Embeddings embedding, String collectionName) {
        try {
            String serverUrl = TYPESENSE_SERVER_URL;
            String apiKey = TYPESENSE_API_KEY;

            if (StringUtils.isBlank(serverUrl)) {
                throw TypesenseException.configurationError("Typesense服务器URL未配置");
            }
            if (StringUtils.isBlank(apiKey)) {
                throw TypesenseException.configurationError("Typesense API Key未配置");
            }

            this.collectionName = validateAndGenerateCollectionName(collectionName);
            this.embedding = embedding;
            this.service = new TypesenseService(serverUrl, apiKey, this.collectionName);
            initializeCollection();

            log.info("TypesenseStore初始化成功，集合名称: {}", this.collectionName);
        } catch (TypesenseException e) {
            throw e;
        } catch (Exception e) {
            throw TypesenseException.initializationError("TypesenseStore初始化失败", e);
        }
    }

    public TypesenseStore(String serverUrl, String apiKey, Embeddings embedding, String collectionName) {
        try {
            if (StringUtils.isBlank(serverUrl)) {
                throw TypesenseException.configurationError("服务器URL不能为空");
            }
            if (StringUtils.isBlank(apiKey)) {
                throw TypesenseException.configurationError("API Key不能为空");
            }

            this.collectionName = validateAndGenerateCollectionName(collectionName);
            this.embedding = embedding;
            this.service = new TypesenseService(serverUrl, apiKey, this.collectionName);
            initializeCollection();

            log.info("TypesenseStore初始化成功，集合名称: {}", this.collectionName);
        } catch (TypesenseException e) {
            throw e;
        } catch (Exception e) {
            throw TypesenseException.initializationError("TypesenseStore初始化失败", e);
        }
    }

    public TypesenseStore(String serverUrl, String apiKey, Embeddings embedding, String collectionName, int dimensions) {
        try {
            if (StringUtils.isBlank(serverUrl)) {
                throw TypesenseException.configurationError("服务器URL不能为空");
            }
            if (StringUtils.isBlank(apiKey)) {
                throw TypesenseException.configurationError("API Key不能为空");
            }
            if (dimensions <= 0) {
                throw TypesenseException.validationError("向量维度必须大于0");
            }

            this.collectionName = validateAndGenerateCollectionName(collectionName);
            this.embedding = embedding;
            this.dimensions = dimensions;
            this.service = new TypesenseService(serverUrl, apiKey, this.collectionName);
            initializeCollection();

            log.info("TypesenseStore初始化成功，集合名称: {}, 向量维度: {}", this.collectionName, dimensions);
        } catch (TypesenseException e) {
            throw e;
        } catch (Exception e) {
            throw TypesenseException.initializationError("TypesenseStore初始化失败", e);
        }
    }

    private String validateAndGenerateCollectionName(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            String generated = UUID.randomUUID().toString().replace("-", "").toLowerCase();
            log.info("集合名称为空，自动生成: {}", generated);
            return generated;
        }

        // 验证集合名称规则
        String cleaned = collectionName.toLowerCase();
        if (cleaned.length() < 3 || cleaned.length() > 63) {
            throw TypesenseException.validationError("集合名称长度必须在3到63个字符之间");
        }

        if (!cleaned.matches("^[a-z0-9][a-z0-9._-]*[a-z0-9]$")) {
            throw TypesenseException.validationError("集合名称格式不正确，必须以字母或数字开头和结尾");
        }

        if (cleaned.contains("..")) {
            throw TypesenseException.validationError("集合名称不能包含连续的点");
        }

        return cleaned;
    }

    private void initializeCollection() {
        try {
            service.initializeCollection(dimensions);
            log.debug("集合初始化完成: {}", collectionName);
        } catch (Exception e) {
            log.error("初始化Typesense集合失败: {}", collectionName, e);
            throw TypesenseException.initializationError("集合初始化失败", e);
        }
    }

    /**
     * 添加文本向量，如果没有向量，系统会自动的使用embedding生成向量
     *
     * @param documents
     */
    @Override
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            log.warn("文档列表为空，跳过添加操作");
            return;
        }

        try {
            List<Document> validDocuments = new ArrayList<>();

            for (Document document : documents) {
                if (StringUtils.isBlank(document.getPageContent())) {
                    log.warn("跳过空内容文档: {}", document.getUniqueId());
                    continue;
                }

                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
                if (MapUtils.isEmpty(document.getMetadata())) {
                    document.setMetadata(new java.util.HashMap<>());
                }

                List<Double> doubleList = document.getEmbedding();

                // 如果没有向量，生成向量
                if (CollectionUtils.isEmpty(doubleList) && embedding != null) {
                    try {
                        List<Document> embeddingResult = embedding.embedTexts(List.of(document.getPageContent()));
                        if (CollectionUtils.isNotEmpty(embeddingResult) &&
                            CollectionUtils.isNotEmpty(embeddingResult.get(0).getEmbedding())) {
                            doubleList = embeddingResult.get(0).getEmbedding();
                        }
                    } catch (Exception e) {
                        log.warn("为文档生成向量失败: {}", document.getUniqueId(), e);
                        continue;
                    }
                }

                // doubleList转成FloatList
                if (CollectionUtils.isNotEmpty(doubleList)) {
                    List<Float> floatList = doubleList.stream().map(Double::floatValue).collect(Collectors.toList());

                    service.addDocument(document.getUniqueId(), document.getPageContent(), floatList, document.getMetadata());
                    validDocuments.add(document);
                } else {
                    log.warn("文档缺少向量数据，跳过: {}", document.getUniqueId());
                }
            }

            log.info("成功添加 {} 个文档到Typesense", validDocuments.size());
        } catch (Exception e) {
            log.error("添加文档到Typesense失败", e);
            throw TypesenseException.operationError("添加文档失败", e);
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
        if (texts == null) {
            throw TypesenseException.validationError("文本列表不能为空");
        }

        try {
            List<String> textsList = new ArrayList<>();
            texts.forEach(textsList::add);

            if (textsList.isEmpty()) {
                log.warn("文本列表为空，跳过添加操作");
                return new ArrayList<>();
            }

            // Handle the case where the user doesn't provide ids on the Collection
            if (ids == null) {
                ids = new ArrayList<>();
                for (String text : textsList) {
                    ids.add(UUID.randomUUID().toString());
                }
            }

            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < textsList.size(); i++) {
                Document document = new Document();
                document.setUniqueId(ids.get(i));
                document.setPageContent(textsList.get(i));
                if (metadatas != null && i < metadatas.size()) {
                    document.setMetadata(metadatas.get(i));
                }
                documents.add(document);
            }

            addDocuments(documents);
            return ids;
        } catch (TypesenseException e) {
            throw e;
        } catch (Exception e) {
            throw TypesenseException.operationError("添加文本失败", e);
        }
    }

    /**
     * Typesense向量库查询
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        if (StringUtils.isBlank(query)) {
            throw TypesenseException.validationError("查询文本不能为空");
        }
        if (k <= 0) {
            throw TypesenseException.validationError("返回结果数量k必须大于0");
        }

        try {
            // 为查询生成向量
            List<Float> queryVector = null;
            if (embedding != null) {
                List<Document> embeddingResult = embedding.embedTexts(List.of(query));
                if (CollectionUtils.isNotEmpty(embeddingResult) &&
                    CollectionUtils.isNotEmpty(embeddingResult.get(0).getEmbedding())) {
                    queryVector = embeddingResult.get(0).getEmbedding().stream()
                            .map(Double::floatValue)
                            .collect(Collectors.toList());
                }
            }

            if (queryVector == null) {
                log.warn("无法为查询文本生成向量");
                return new ArrayList<>();
            }

            SearchResult searchResult = service.searchSimilar(queryVector, k);

            List<Document> documents = new ArrayList<>();
            if (searchResult.getHits() != null) {
                for (int i = 0; i < searchResult.getHits().size(); i++) {
                    Map<String, Object> hit = searchResult.getHits().get(i).getDocument();

                    String id = (String) hit.get("id");
                    String content = (String) hit.get("content");
                    // 使用文档匹配分数，如果不存在则使用默认值
                    Double score = searchResult.getHits().get(i).getTextMatch() != null ?
                        searchResult.getHits().get(i).getTextMatch().doubleValue() : 1.0;

                    Document document = new Document();
                    document.setUniqueId(id);
                    document.setPageContent(filter(content));
                    document.setScore(score != null ? score : 0.0);

                    // 提取metadata
                    Map<String, Object> metadata = new java.util.HashMap<>();
                    for (Map.Entry<String, Object> entry : hit.entrySet()) {
                        if (!"id".equals(entry.getKey()) && !"content".equals(entry.getKey()) && !"embedding".equals(entry.getKey())) {
                            metadata.put(entry.getKey(), entry.getValue());
                        }
                    }
                    document.setMetadata(metadata);

                    documents.add(document);
                }
            }

            // 过滤距离
            if (maxDistanceValue != null) {
                documents = documents.stream()
                        .filter(doc -> doc.getScore() <= maxDistanceValue)
                        .collect(Collectors.toList());
            }

            log.debug("相似性搜索返回 {} 个结果", documents.size());
            return documents;
        } catch (TypesenseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Typesense相似性搜索失败", e);
            throw TypesenseException.operationError("相似性搜索失败", e);
        }
    }

    private String filter(String value) {
        if (value == null) {
            return "";
        }
        value = value.replaceAll("<[^>]+>", ""); // 去掉所有HTML标签
        value = StringEscapeUtils.unescapeHtml4(value); // 去掉HTML实体
        return value;
    }

    /**
     * 删除指定ID的文档
     *
     * @param documentId 文档ID
     */
    public void deleteDocument(String documentId) {
        if (StringUtils.isBlank(documentId)) {
            throw TypesenseException.validationError("文档ID不能为空");
        }

        try {
            service.deleteDocument(documentId);
            log.debug("删除文档成功: {}", documentId);
        } catch (Exception e) {
            log.error("删除文档失败: {}", documentId, e);
            throw TypesenseException.operationError("删除文档失败", e);
        }
    }

    /**
     * 删除整个集合
     */
    public void deleteCollection() {
        try {
            service.deleteCollection();
            log.info("删除集合成功: {}", collectionName);
        } catch (Exception e) {
            log.error("删除集合失败: {}", collectionName, e);
            throw TypesenseException.operationError("删除集合失败", e);
        }
    }
}