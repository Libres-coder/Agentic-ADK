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
package com.alibaba.langengine.txtai.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.txtai.vectorstore.service.TxtaiAddRequest;
import com.alibaba.langengine.txtai.vectorstore.service.TxtaiSearchRequest;
import com.alibaba.langengine.txtai.vectorstore.service.TxtaiSearchResponse;
import com.alibaba.langengine.txtai.vectorstore.service.TxtaiService;
import com.alibaba.langengine.txtai.exception.TxtaiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.alibaba.langengine.txtai.TxtaiConfiguration.TXTAI_SERVER_URL;


@Slf4j
@Data
public class Txtai extends VectorStore {

    /**
     * 向量库的embedding
     */
    private Embeddings embedding;

    /**
     * 索引名称标识
     */
    private String indexName;

    /**
     * 内部使用的service，封装业务逻辑
     */
    private TxtaiService service;

    public Txtai(Embeddings embedding, String indexName) {
        String serverUrl = TXTAI_SERVER_URL;
        validateServerUrl(serverUrl);
        this.indexName = validateIndexName(indexName);
        this.embedding = validateEmbedding(embedding);
        this.service = new TxtaiService(serverUrl);
    }

    public Txtai(String serverUrl, Embeddings embedding, String indexName) {
        validateServerUrl(serverUrl);
        this.indexName = validateIndexName(indexName);
        this.embedding = validateEmbedding(embedding);
        this.service = new TxtaiService(serverUrl);
    }

    /**
     * 添加文本向量，如果没有向量，系统会自动的使用embedding生成向量
     *
     * @param documents
     */
    @Override
    public void addDocuments(List<Document> documents) {
        validateDocuments(documents);

        try {
            TxtaiAddRequest request = new TxtaiAddRequest();
            List<TxtaiAddRequest.Document> txtaiDocs = new ArrayList<>();

            for (Document document : documents) {
                if (StringUtils.isEmpty(document.getPageContent())) {
                    log.warn("文档内容为空，跳过文档: {}", document.getUniqueId());
                    continue;
                }

                if (StringUtils.isEmpty(document.getUniqueId())) {
                    document.setUniqueId(UUID.randomUUID().toString());
                }

                if (MapUtils.isEmpty(document.getMetadata())) {
                    document.setMetadata(new HashMap<>());
                }

                TxtaiAddRequest.Document txtaiDoc = new TxtaiAddRequest.Document();
                txtaiDoc.setId(document.getUniqueId());
                txtaiDoc.setText(document.getPageContent());

                Map<String, Object> metadata = new HashMap<>(document.getMetadata());
                metadata.put("index_name", indexName);
                txtaiDoc.setMetadata(metadata);

                txtaiDocs.add(txtaiDoc);
            }

            if (CollectionUtils.isNotEmpty(txtaiDocs)) {
                request.setDocuments(txtaiDocs);
                service.addDocuments(request);

                service.index();
                log.info("成功添加 {} 个文档到txtai索引: {}", txtaiDocs.size(), indexName);
            }

        } catch (TxtaiException e) {
            log.error("添加文档到txtai失败", e);
            throw e;
        } catch (Exception e) {
            log.error("添加文档到txtai失败", e);
            throw TxtaiException.processingError("添加文档到txtai失败: " + e.getMessage(), e);
        }
    }

    /**
     * txtai向量库查询
     *
     * @param query
     * @param k
     * @param maxDistanceValue
     * @param type
     * @return
     */
    @Override
    public List<Document> similaritySearch(String query, int k, Double maxDistanceValue, Integer type) {
        validateQuery(query);
        validateK(k);

        try {
            TxtaiSearchRequest request = new TxtaiSearchRequest();
            request.setQuery(query);
            request.setLimit(k);
            if (maxDistanceValue != null) {
                request.setThreshold(1.0 - maxDistanceValue);
            }

            List<TxtaiSearchResponse.SearchResult> searchResults = service.search(request);

            List<Document> documents = new ArrayList<>();
            for (TxtaiSearchResponse.SearchResult result : searchResults) {
                if (maxDistanceValue != null && result.getScore() != null) {
                    double distance = 1.0 - result.getScore();
                    if (distance > maxDistanceValue) {
                        continue;
                    }
                }

                Document document = new Document();
                document.setUniqueId(result.getId());
                document.setPageContent(result.getText());
                document.setScore(result.getScore());

                Map<String, Object> metadata = result.getMetadata();
                if (metadata != null) {
                    document.setMetadata(metadata);
                } else {
                    document.setMetadata(new HashMap<>());
                }

                documents.add(document);
            }

            log.debug("相似性搜索返回 {} 个结果，查询: {}", documents.size(), query);
            return documents;

        } catch (TxtaiException e) {
            log.error("txtai相似性搜索失败", e);
            throw e;
        } catch (Exception e) {
            log.error("txtai相似性搜索失败", e);
            throw TxtaiException.processingError("txtai相似性搜索失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取索引信息
     *
     * @return 索引信息
     */
    public String getIndexInfo() {
        try {
            return service.info();
        } catch (TxtaiException e) {
            log.error("获取txtai索引信息失败", e);
            throw e;
        } catch (Exception e) {
            log.error("获取txtai索引信息失败", e);
            throw TxtaiException.processingError("获取txtai索引信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重建索引
     */
    public void rebuildIndex() {
        try {
            service.index();
            log.info("txtai索引重建完成: {}", indexName);
        } catch (TxtaiException e) {
            log.error("重建txtai索引失败", e);
            throw e;
        } catch (Exception e) {
            log.error("重建txtai索引失败", e);
            throw TxtaiException.processingError("重建txtai索引失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (service != null) {
            service.close();
        }
    }

    /**
     * 设置service（主要用于测试）
     */
    public void setService(TxtaiService service) {
        this.service = service;
    }

    // 验证方法

    /**
     * 验证服务器URL
     */
    private void validateServerUrl(String serverUrl) {
        if (StringUtils.isEmpty(serverUrl)) {
            throw TxtaiException.configurationError("txtai服务器URL不能为空，请配置 txtai_server_url 参数");
        }
        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
            throw TxtaiException.configurationError("txtai服务器URL格式不正确，必须以http://或https://开头");
        }
    }

    /**
     * 验证索引名称
     */
    private String validateIndexName(String indexName) {
        if (StringUtils.isEmpty(indexName)) {
            return "default";
        }

        String cleaned = indexName.toLowerCase().trim();
        if (cleaned.length() < 3 || cleaned.length() > 63) {
            throw TxtaiException.validationError("索引名称长度必须在3到63个字符之间");
        }

        if (!cleaned.matches("^[a-z0-9][a-z0-9._-]*[a-z0-9]$")) {
            throw TxtaiException.validationError("索引名称格式不正确，必须以字母或数字开头和结尾，只能包含小写字母、数字、点、下划线和破折号");
        }

        if (cleaned.contains("..")) {
            throw TxtaiException.validationError("索引名称不能包含连续的点");
        }

        return cleaned;
    }

    /**
     * 验证Embedding
     */
    private Embeddings validateEmbedding(Embeddings embedding) {
        if (embedding == null) {
            throw TxtaiException.configurationError("Embedding对象不能为空");
        }
        return embedding;
    }

    /**
     * 验证文档列表
     */
    private void validateDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            throw TxtaiException.validationError("文档列表不能为空");
        }
    }

    /**
     * 验证查询文本
     */
    private void validateQuery(String query) {
        if (StringUtils.isEmpty(query)) {
            throw TxtaiException.validationError("查询文本不能为空");
        }
        if (query.trim().length() == 0) {
            throw TxtaiException.validationError("查询文本不能只包含空白字符");
        }
    }

    /**
     * 验证返回结果数量k
     */
    private void validateK(int k) {
        if (k <= 0) {
            throw TxtaiException.validationError("返回结果数量k必须大于0");
        }
        if (k > 1000) {
            throw TxtaiException.validationError("返回结果数量k不能超过1000");
        }
    }
}