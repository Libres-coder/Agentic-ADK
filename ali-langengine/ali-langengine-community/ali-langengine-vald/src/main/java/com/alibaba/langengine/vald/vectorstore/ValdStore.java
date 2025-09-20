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
package com.alibaba.langengine.vald.vectorstore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.vald.vectorstore.service.ValdInsertRequest;
import com.alibaba.langengine.vald.vectorstore.service.ValdSearchRequest;
import com.alibaba.langengine.vald.vectorstore.service.ValdSearchResponse;
import com.alibaba.langengine.vald.vectorstore.service.ValdService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import static com.alibaba.langengine.vald.ValdConfiguration.VALD_SERVER_HOST;
import static com.alibaba.langengine.vald.ValdConfiguration.VALD_SERVER_PORT;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class ValdStore extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 标识一个唯一的仓库前缀，用于区分不同业务的向量内容
     *
     * 名称规范：
     * - 长度必须在 3 到 63 个字符之间
     * - 必须以小写字母或数字开头和结尾
     * - 可以在中间包含点、破折号和下划线
     * - 不能包含两个连续的点
     * - 不能是有效的 IP 地址
     */
    private String collectionId;

    /**
     * 内部使用的服务客户端，不希望对外暴露
     */
    private ValdService _service;

    /**
     * 内部维护的文档内容映射，key为向量ID，value为文档内容
     * Vald本身不支持存储元数据，需要在客户端维护
     */
    private Map<String, String> _documentContentMap;

    /**
     * 内部维护的文档元数据映射，key为向量ID，value为元数据
     */
    private Map<String, Map<String, Object>> _documentMetadataMap;

    public ValdStore(Embeddings embedding, String collectionId) {
        String serverHost = (VALD_SERVER_HOST != null && !VALD_SERVER_HOST.trim().isEmpty()) ? VALD_SERVER_HOST : "localhost";
        String serverPort = (VALD_SERVER_PORT != null && !VALD_SERVER_PORT.trim().isEmpty()) ? VALD_SERVER_PORT : "8080";
        this.collectionId = collectionId == null ? UUID.randomUUID().toString() : collectionId;
        this.embedding = embedding;
        this._service = new ValdService(serverHost, Integer.parseInt(serverPort));
        this._documentContentMap = new HashMap<>();
        this._documentMetadataMap = new HashMap<>();
    }

    public ValdStore(String serverHost, int serverPort, Embeddings embedding, String collectionId) {
        this.collectionId = collectionId == null ? UUID.randomUUID().toString() : collectionId;
        this.embedding = embedding;
        this._service = new ValdService(serverHost, serverPort);
        this._documentContentMap = new HashMap<>();
        this._documentMetadataMap = new HashMap<>();
    }

    /**
     * 添加文本向量，如果没有向量，系统会自动的使用embedding生成向量
     *
     * @param documents
     */
    @Override
    public void addDocuments(List<Document> documents) {
        try {
            List<ValdInsertRequest> insertRequests = new ArrayList<>();

            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }
                if (StringUtils.isEmpty(document.getPageContent())) {
                    continue;
                }
                if (MapUtils.isEmpty(document.getMetadata())) {
                    document.setMetadata(new HashMap<>());
                }

                List<Double> doubleList = document.getEmbedding();
                List<Float> floatList = null;
                if (CollectionUtils.isNotEmpty(doubleList)) {
                    floatList = doubleList.stream().map(Double::floatValue).collect(Collectors.toList());
                }

                // 如果没有向量，使用embedding生成
                if (floatList == null && embedding != null) {
                    List<Document> embeddedDocs = embedding.embedTexts(Arrays.asList(document.getPageContent()));
                    if (CollectionUtils.isNotEmpty(embeddedDocs) && CollectionUtils.isNotEmpty(embeddedDocs.get(0).getEmbedding())) {
                        floatList = embeddedDocs.get(0).getEmbedding().stream()
                                .map(Double::floatValue).collect(Collectors.toList());
                    }
                }

                if (floatList != null) {
                    String vectorId = generateVectorId(document.getUniqueId());
                    // 转换Float列表为Double列表
                    List<Double> doubleVector = floatList.stream().map(Float::doubleValue).collect(Collectors.toList());
                    ValdInsertRequest request = new ValdInsertRequest(vectorId, doubleVector, null);
                    insertRequests.add(request);

                    // 保存文档内容和元数据到内部映射
                    _documentContentMap.put(vectorId, document.getPageContent());
                    _documentMetadataMap.put(vectorId, document.getMetadata());
                }
            }

            // 批量插入向量
            if (!insertRequests.isEmpty()) {
                try {
                    _service.multiInsert(insertRequests);
                    log.debug("Successfully inserted {} vectors to Vald", insertRequests.size());
                } catch (Exception e) {
                    log.error("Failed to insert vectors to Vald", e);
                    throw new RuntimeException("Failed to insert vectors to Vald", e);
                }
            }

        } catch (Exception e) {
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
        List<Map<String, String>> metadatas,
        List<String> ids
    ) {
        // 处理IDs，如果用户没有提供则自动生成
        if (ids == null) {
            ids = new ArrayList<>();
            for (String text : texts) {
                ids.add(UUID.randomUUID().toString());
            }
        }

        List<Document> embeddings = null;
        List<String> textsList = new ArrayList<>();
        texts.forEach(textsList::add);

        if (this.embedding != null) {
            embeddings = this.embedding.embedTexts(textsList);
        }

        List<ValdInsertRequest> insertRequests = new ArrayList<>();

        for (int i = 0; i < textsList.size(); i++) {
            String text = textsList.get(i);
            String id = ids.get(i);
            Map<String, String> metadata = metadatas != null && i < metadatas.size() ? metadatas.get(i) : new HashMap<>();

            List<Float> vector = null;
            if (embeddings != null && i < embeddings.size() && CollectionUtils.isNotEmpty(embeddings.get(i).getEmbedding())) {
                vector = embeddings.get(i).getEmbedding().stream()
                        .map(Double::floatValue).collect(Collectors.toList());
            }

            if (vector != null) {
                String vectorId = generateVectorId(id);
                // 转换Float列表为Double列表
                List<Double> doubleVector = vector.stream().map(Float::doubleValue).collect(Collectors.toList());
                ValdInsertRequest request = new ValdInsertRequest(vectorId, doubleVector, null);
                insertRequests.add(request);

                // 保存文档内容和元数据
                _documentContentMap.put(vectorId, text);
                Map<String, Object> metadataObj = new HashMap<>();
                if (metadata != null) {
                    metadataObj.putAll(metadata);
                }
                _documentMetadataMap.put(vectorId, metadataObj);
            }
        }

        // 批量插入
        if (!insertRequests.isEmpty()) {
            try {
                _service.multiInsert(insertRequests);
                log.debug("Successfully inserted {} texts to Vald", insertRequests.size());
            } catch (Exception e) {
                log.error("Failed to insert texts to Vald", e);
                throw new RuntimeException("Failed to insert texts to Vald", e);
            }
        }

        return ids;
    }

    /**
     * Vald向量库查询
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
            // 先将查询文本转换为向量
            List<Document> queryEmbeddings = null;
            if (embedding != null) {
                queryEmbeddings = embedding.embedTexts(Arrays.asList(query));
            }

            if (queryEmbeddings == null || queryEmbeddings.isEmpty() ||
                CollectionUtils.isEmpty(queryEmbeddings.get(0).getEmbedding())) {
                throw new RuntimeException("Failed to generate embedding for query: " + query);
            }

            List<Double> queryVector = queryEmbeddings.get(0).getEmbedding();

            // 构建搜索请求
            ValdSearchRequest searchRequest = new ValdSearchRequest(queryVector, k, maxDistanceValue);
            ValdSearchResponse response = _service.search(searchRequest);

            // 转换搜索结果为Document
            List<Document> documents = new ArrayList<>();
            if (response != null && CollectionUtils.isNotEmpty(response.getResults())) {
                for (ValdSearchResponse.ValdSearchResult result : response.getResults()) {
                    // 过滤距离阈值
                    if (maxDistanceValue != null && result.getDistance() > maxDistanceValue) {
                        continue;
                    }

                    Document document = new Document();
                    document.setUniqueId(extractOriginalId(result.getId()));
                    document.setScore(result.getDistance());

                    // 从内部映射获取文档内容和元数据
                    String content = _documentContentMap.get(result.getId());
                    Map<String, Object> metadata = _documentMetadataMap.get(result.getId());

                    document.setPageContent(content != null ? filter(content) : "");
                    document.setMetadata(metadata != null ? metadata : new HashMap<>());

                    documents.add(document);
                }
            }
            return documents;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成带有集合前缀的向量ID
     */
    private String generateVectorId(String originalId) {
        return collectionId + "_" + originalId;
    }

    /**
     * 从向量ID中提取原始ID
     */
    private String extractOriginalId(String vectorId) {
        if (vectorId != null && vectorId.startsWith(collectionId + "_")) {
            return vectorId.substring((collectionId + "_").length());
        }
        return vectorId;
    }

    private String filter(String value) {
        value = value.replaceAll("<[^>]+>", ""); // 去掉所有HTML标签
        value = StringEscapeUtils.unescapeHtml4(value); // 去掉HTML实体
        return value;
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (_service != null) {
            _service.close();
        }
    }

    public void set_service(ValdService service) {
        this._service = service;
    }

    public Map<String, String> get_documentContentMap() {
        return this._documentContentMap;
    }

    public Map<String, Map<String, Object>> get_documentMetadataMap() {
        return this._documentMetadataMap;
    }
}