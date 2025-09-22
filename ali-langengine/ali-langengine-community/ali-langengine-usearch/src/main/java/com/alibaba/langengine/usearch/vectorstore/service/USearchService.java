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
package com.alibaba.langengine.usearch.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.usearch.vectorstore.USearchClient;
import com.alibaba.langengine.usearch.vectorstore.USearchException;
import com.alibaba.langengine.usearch.vectorstore.USearchParam;
import cloud.unum.usearch.Index;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Slf4j
@Data
public class USearchService {

    private final USearchClient client;
    private final USearchParam param;
    private final String metadataPath;
    
    // 内存中的文档记录映射
    private final Map<Long, USearchDocumentRecord> documentRecords = new ConcurrentHashMap<>();
    private final Map<String, Long> uniqueIdToKeyMap = new ConcurrentHashMap<>();
    private final AtomicLong keyGenerator = new AtomicLong(1L);

    public USearchService(String indexPath, USearchParam param) {
        this.param = param;
        this.client = new USearchClient(indexPath, param);
        this.metadataPath = indexPath + ".metadata";
    }

    /**
     * 初始化服务
     */
    public void init() {
        try {
            client.initialize();
            loadMetadata();
            log.info("USearch service initialized successfully");
        } catch (Exception e) {
            throw USearchException.indexInitializationFailed("Failed to initialize USearch service", e);
        }
    }

    /**
     * 添加文档
     *
     * @param documents 文档列表
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try {
            for (Document document : documents) {
                addSingleDocument(document);
            }
            
            // 保存索引和元数据
            client.saveIndex();
            saveMetadata();
            
            log.info("Added {} documents to USearch index", documents.size());
        } catch (Exception e) {
            throw USearchException.addDocumentFailed("Failed to add documents", e);
        }
    }

    /**
     * 相似度搜索
     *
     * @param queryVector 查询向量
     * @param k 返回结果数量
     * @param maxDistance 最大距离阈值
     * @return 搜索结果列表
     */
    public List<USearchSearchResult> similaritySearch(float[] queryVector, int k, Double maxDistance) {
        try {
            long[] matchKeys = client.search(queryVector, k);
            List<USearchSearchResult> results = new ArrayList<>();
            
            for (long key : matchKeys) {
                USearchDocumentRecord record = documentRecords.get(key);
                if (record != null) {
                    // 从USearch客户端获取向量并计算距离
                    float[] storedVector = client.getVector(key);
                    double distance = storedVector != null ? 
                        calculateDistance(queryVector, storedVector) : 
                        Double.MAX_VALUE;
                    
                    if (maxDistance != null && distance > maxDistance) {
                        continue;
                    }
                    
                    USearchSearchResult result = new USearchSearchResult(key, (float) distance, record);
                    results.add(result);
                }
            }
            
            return results;
        } catch (Exception e) {
            throw USearchException.searchFailed("Failed to perform similarity search", e);
        }
    }

    /**
     * 根据文档ID列表删除文档
     *
     * @param uniqueIds 文档唯一ID列表
     */
    public void deleteByIds(List<String> uniqueIds) {
        if (CollectionUtils.isEmpty(uniqueIds)) {
            return;
        }

        try {
            int deletedCount = 0;
            for (String uniqueId : uniqueIds) {
                Long vectorKey = uniqueIdToKeyMap.get(uniqueId);
                if (vectorKey != null) {
                    client.removeVector(vectorKey);
                    documentRecords.remove(vectorKey);
                    uniqueIdToKeyMap.remove(uniqueId);
                    deletedCount++;
                }
            }
            
            if (deletedCount > 0) {
                client.saveIndex();
                saveMetadata();
            }
            
            log.info("Deleted {} documents from USearch index", deletedCount);
        } catch (Exception e) {
            throw USearchException.addDocumentFailed("Failed to delete documents", e);
        }
    }

    /**
     * 根据文档ID删除文档
     *
     * @param uniqueId 文档唯一ID
     */
    public void deleteById(String uniqueId) {
        deleteByIds(Arrays.asList(uniqueId));
    }

    /**
     * 获取索引统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("indexSize", client.size());
        stats.put("indexCapacity", client.capacity());
        stats.put("documentCount", documentRecords.size());
        stats.put("dimension", param.getDimension());
        stats.put("metricType", param.getMetricType());
        return stats;
    }

    /**
     * 关闭服务
     */
    public void close() {
        try {
            client.close();
            log.info("USearch service closed");
        } catch (Exception e) {
            log.error("Failed to close USearch service", e);
        }
    }

    private void addSingleDocument(Document document) {
        // 验证文档
        if (StringUtils.isEmpty(document.getPageContent())) {
            log.warn("Document content is empty, skipping");
            return;
        }

        if (CollectionUtils.isEmpty(document.getEmbedding())) {
            throw USearchException.addDocumentFailed("Document embedding is empty", null);
        }

        // 确保文档有唯一ID
        if (StringUtils.isEmpty(document.getUniqueId())) {
            document.setUniqueId(UUID.randomUUID().toString());
        }

        // 检查是否已存在，如果存在则更新
        Long existingKey = uniqueIdToKeyMap.get(document.getUniqueId());
        long vectorKey;
        
        if (existingKey != null) {
            // 更新现有文档
            vectorKey = existingKey;
        } else {
            // 添加新文档
            vectorKey = keyGenerator.getAndIncrement();
            uniqueIdToKeyMap.put(document.getUniqueId(), vectorKey);
        }

        // 转换向量格式
        float[] vector = convertToFloatArray(document.getEmbedding());
        
        // 添加到索引
        client.addVector(vectorKey, vector);
        
        // 创建文档记录
        USearchDocumentRecord record = USearchDocumentRecord.fromDocument(document, vectorKey);
        documentRecords.put(vectorKey, record);
    }

    private float[] convertToFloatArray(List<Double> doubleList) {
        float[] floatArray = new float[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            floatArray[i] = doubleList.get(i).floatValue();
        }
        return floatArray;
    }

    private void saveMetadata() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metadataPath))) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentRecords", new HashMap<>(documentRecords));
            metadata.put("uniqueIdToKeyMap", new HashMap<>(uniqueIdToKeyMap));
            metadata.put("keyGenerator", keyGenerator.get());
            
            oos.writeObject(metadata);
            log.debug("Metadata saved to: {}", metadataPath);
        } catch (Exception e) {
            log.error("Failed to save metadata", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadMetadata() {
        if (!Files.exists(Paths.get(metadataPath))) {
            log.info("No metadata file found, starting with empty index");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadataPath))) {
            Map<String, Object> metadata = (Map<String, Object>) ois.readObject();
            
            Map<Long, USearchDocumentRecord> loadedRecords = 
                (Map<Long, USearchDocumentRecord>) metadata.get("documentRecords");
            if (loadedRecords != null) {
                documentRecords.putAll(loadedRecords);
            }
            
            Map<String, Long> loadedIdMap = 
                (Map<String, Long>) metadata.get("uniqueIdToKeyMap");
            if (loadedIdMap != null) {
                uniqueIdToKeyMap.putAll(loadedIdMap);
            }
            
            Long maxKey = (Long) metadata.get("keyGenerator");
            if (maxKey != null) {
                keyGenerator.set(maxKey);
            }
            
            log.info("Metadata loaded from: {}, {} documents found", metadataPath, documentRecords.size());
        } catch (Exception e) {
            log.warn("Failed to load metadata, starting with empty index", e);
        }
    }
    
    /**
     * 计算两个向量之间的距离
     */
    private double calculateDistance(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            return Double.MAX_VALUE;
        }
        
        // 计算余弦距离
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 1.0; // 最大余弦距离
        }
        
        double cosine = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        return 1.0 - cosine; // 余弦距离 = 1 - 余弦相似度
    }

}
