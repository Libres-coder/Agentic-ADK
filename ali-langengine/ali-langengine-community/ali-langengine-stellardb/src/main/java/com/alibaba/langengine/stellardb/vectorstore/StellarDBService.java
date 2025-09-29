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
package com.alibaba.langengine.stellardb.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Data
public class StellarDBService {

    private final String collection;
    private final StellarDBParam param;
    private final StellarDBClient client;

    public StellarDBService(String serverUrl, String username, String password, String collection, StellarDBParam param) {
        this.collection = collection;
        this.param = param != null ? param : new StellarDBParam();
        this.client = new StellarDBClient(serverUrl, username, password, this.param);
        
        log.info("StellarDBService initialized for collection: {}", collection);
    }

    /**
     * 初始化集合
     */
    public void init(Embeddings embedding) {
        try {
            client.connect();
            
            if (!client.hasCollection(collection)) {
                int dimension = param.getVectorDimension();
                if (dimension <= 0 && embedding != null) {
                    // 通过embedding确定维度
                    List<Document> testDocs = embedding.embedTexts(Lists.newArrayList("test"));
                    if (!testDocs.isEmpty() && testDocs.get(0).getEmbedding() != null) {
                        dimension = testDocs.get(0).getEmbedding().size();
                    }
                }
                if (dimension <= 0) {
                    throw new StellarDBException("INVALID_DIMENSION", "Cannot determine valid vector dimension");
                }
                client.createCollection(collection, dimension);
            }
        } catch (Exception e) {
            log.error("Failed to initialize StellarDB collection: {}", collection, e);
            throw new StellarDBException("INIT_ERROR", "Failed to initialize collection", e);
        }
    }

    /**
     * 添加文档
     */
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            List<Map<String, Object>> stellarDocs = Lists.newArrayList();
            
            for (Document document : documents) {
                Map<String, Object> stellarDoc = new HashMap<>();
                stellarDoc.put(param.getFieldNameUniqueId(), document.getUniqueId());
                stellarDoc.put(param.getFieldNamePageContent(), document.getPageContent());
                
                if (document.getEmbedding() != null) {
                    List<Float> embeddings = document.getEmbedding().stream()
                        .map(Double::floatValue)
                        .collect(java.util.stream.Collectors.toList());
                    stellarDoc.put(param.getFieldNameEmbedding(), embeddings);
                }
                
                stellarDocs.add(stellarDoc);
            }
            
            client.insert(collection, stellarDocs);
        } catch (Exception e) {
            log.error("Failed to add documents to StellarDB", e);
            throw new StellarDBException("ADD_DOCUMENTS_ERROR", "Failed to add documents", e);
        }
    }

    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(List<Float> embeddings, int k) {
        if (embeddings == null || embeddings.isEmpty()) {
            throw new IllegalArgumentException("Embeddings cannot be null or empty");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        try {
            List<Map<String, Object>> results = client.search(collection, embeddings, k);
            List<Document> documents = Lists.newArrayList();
            
            for (Map<String, Object> result : results) {
                Document document = new Document();
                document.setUniqueId((String) result.get(param.getFieldNameUniqueId()));
                document.setPageContent((String) result.get(param.getFieldNamePageContent()));
                
                Object scoreObj = result.get(param.getFieldNameScore());
                if (scoreObj instanceof Number) {
                    document.setScore(((Number) scoreObj).doubleValue());
                }
                
                documents.add(document);
            }
            
            return documents;
        } catch (Exception e) {
            log.error("Failed to perform similarity search in StellarDB", e);
            throw new StellarDBException("SEARCH_ERROR", "Failed to perform similarity search", e);
        }
    }

    /**
     * 删除文档
     */
    public void deleteDocuments(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        try {
            client.delete(collection, ids);
        } catch (Exception e) {
            log.error("Failed to delete documents from StellarDB", e);
            throw new StellarDBException("DELETE_ERROR", "Failed to delete documents", e);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (client != null) {
            client.close();
        }
    }

}