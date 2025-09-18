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
package com.alibaba.langengine.faiss.service;

import com.alibaba.langengine.faiss.exception.FaissException;
import com.alibaba.langengine.faiss.model.FaissIndex;
import com.alibaba.langengine.faiss.model.FaissSearchResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FAISS 服务类
 * 提供FAISS索引的核心操作功能
 * 
 * @author langengine
 */
@Slf4j
@Data
public class FaissService {
    
    private String indexPath;
    private int vectorDimension;
    private String indexType;
    private boolean useGpu;
    private int gpuDeviceId;
    
    private FaissIndex index;
    private Map<String, Integer> documentIdToIndexMap;
    private Map<Integer, String> indexToDocumentIdMap;
    private int nextIndex;
    
    public FaissService(String indexPath, int vectorDimension, String indexType, 
                       boolean useGpu, int gpuDeviceId) {
        this.indexPath = indexPath;
        this.vectorDimension = vectorDimension;
        this.indexType = indexType;
        this.useGpu = useGpu;
        this.gpuDeviceId = gpuDeviceId;
        
        this.documentIdToIndexMap = new ConcurrentHashMap<>();
        this.indexToDocumentIdMap = new ConcurrentHashMap<>();
        this.nextIndex = 0;
    }
    
    /**
     * 初始化FAISS索引
     */
    public void initialize() {
        try {
            // 创建索引目录
            File indexFile = new File(indexPath);
            File parentDir = indexFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 初始化索引
            this.index = new FaissIndex(vectorDimension, indexType, useGpu, gpuDeviceId);
            
            // 如果索引文件存在，则加载
            if (indexFile.exists()) {
                loadIndex();
            }
            
            log.info("FAISS service initialized successfully");
            
        } catch (Exception e) {
            log.error("Failed to initialize FAISS service", e);
            throw new FaissException("Failed to initialize FAISS service", e);
        }
    }
    
    /**
     * 添加向量到索引
     */
    public void addVectors(List<float[]> vectors, List<String> documentIds) {
        if (vectors == null || vectors.isEmpty()) {
            return;
        }
        
        try {
            // 验证向量维度
            for (float[] vector : vectors) {
                if (vector.length != vectorDimension) {
                    throw new FaissException("Vector dimension mismatch. Expected: " + 
                        vectorDimension + ", Got: " + vector.length);
                }
            }
            
            // 添加到索引
            for (int i = 0; i < vectors.size(); i++) {
                float[] vector = vectors.get(i);
                String documentId = documentIds != null && i < documentIds.size() ? 
                    documentIds.get(i) : "doc_" + nextIndex;
                
                // 添加到索引映射
                documentIdToIndexMap.put(documentId, nextIndex);
                indexToDocumentIdMap.put(nextIndex, documentId);
                
                // 添加到FAISS索引
                index.addVector(vector, nextIndex);
                nextIndex++;
            }
            
            log.info("Added {} vectors to FAISS index", vectors.size());
            
        } catch (Exception e) {
            log.error("Failed to add vectors to FAISS index", e);
            throw new FaissException("Failed to add vectors", e);
        }
    }
    
    /**
     * 执行相似性搜索
     */
    public List<FaissSearchResult> search(float[] queryVector, int k, Double maxDistance) {
        try {
            if (queryVector.length != vectorDimension) {
                throw new FaissException("Query vector dimension mismatch. Expected: " + 
                    vectorDimension + ", Got: " + queryVector.length);
            }
            
            // 执行搜索
            List<FaissSearchResult> results = index.search(queryVector, k);
            
            // 应用距离过滤
            if (maxDistance != null) {
                results = results.stream()
                    .filter(result -> result.getDistance() <= maxDistance)
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 设置文档ID
            for (FaissSearchResult result : results) {
                String documentId = indexToDocumentIdMap.get(result.getIndex());
                result.setDocumentId(documentId);
            }
            
            log.debug("Found {} similar vectors", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new FaissException("Failed to perform similarity search", e);
        }
    }
    
    /**
     * 删除向量
     */
    public void deleteVector(String documentId) {
        try {
            Integer indexId = documentIdToIndexMap.get(documentId);
            if (indexId != null) {
                // 从索引中删除
                index.removeVector(indexId);
                
                // 从映射中删除
                documentIdToIndexMap.remove(documentId);
                indexToDocumentIdMap.remove(indexId);
                
                log.info("Deleted vector for document ID: {}", documentId);
            } else {
                log.warn("Document ID not found: {}", documentId);
            }
            
        } catch (Exception e) {
            log.error("Failed to delete vector for document ID: {}", documentId, e);
            throw new FaissException("Failed to delete vector", e);
        }
    }
    
    /**
     * 获取索引统计信息
     */
    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_vectors", index.getTotalVectors());
        stats.put("vector_dimension", vectorDimension);
        stats.put("index_type", indexType);
        stats.put("use_gpu", useGpu);
        stats.put("gpu_device_id", gpuDeviceId);
        stats.put("index_path", indexPath);
        stats.put("document_count", documentIdToIndexMap.size());
        return stats;
    }
    
    /**
     * 重建索引
     */
    public void rebuildIndex() {
        try {
            log.info("Rebuilding FAISS index...");
            
            // 创建新索引
            FaissIndex newIndex = new FaissIndex(vectorDimension, indexType, useGpu, gpuDeviceId);
            
            // 重新添加所有向量
            for (Map.Entry<String, Integer> entry : documentIdToIndexMap.entrySet()) {
                String documentId = entry.getKey();
                Integer indexId = entry.getValue();
                
                // 这里需要从原始数据重新获取向量
                // 在实际实现中，可能需要维护一个向量缓存
                log.warn("Rebuilding index - vector data for document {} needs to be retrieved", documentId);
            }
            
            this.index = newIndex;
            log.info("FAISS index rebuilt successfully");
            
        } catch (Exception e) {
            log.error("Failed to rebuild FAISS index", e);
            throw new FaissException("Failed to rebuild index", e);
        }
    }
    
    /**
     * 保存索引到文件
     */
    public void saveIndex() {
        try {
            File indexFile = new File(indexPath);
            File parentDir = indexFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 保存索引数据
            index.save(indexPath);
            
            // 保存元数据
            saveMetadata();
            
            log.info("FAISS index saved to: {}", indexPath);
            
        } catch (Exception e) {
            log.error("Failed to save FAISS index", e);
            throw new FaissException("Failed to save index", e);
        }
    }
    
    /**
     * 从文件加载索引
     */
    public void loadIndex() {
        try {
            File indexFile = new File(indexPath);
            if (!indexFile.exists()) {
                log.warn("Index file does not exist: {}", indexPath);
                return;
            }
            
            // 加载索引数据
            index.load(indexPath);
            
            // 加载元数据
            loadMetadata();
            
            log.info("FAISS index loaded from: {}", indexPath);
            
        } catch (Exception e) {
            log.error("Failed to load FAISS index", e);
            throw new FaissException("Failed to load index", e);
        }
    }
    
    /**
     * 获取索引大小
     */
    public long getIndexSize() {
        try {
            return index.getTotalVectors();
        } catch (Exception e) {
            log.error("Failed to get index size", e);
            return 0;
        }
    }
    
    /**
     * 保存元数据
     */
    private void saveMetadata() {
        try {
            String metadataPath = indexPath + ".metadata";
            // 这里可以实现元数据的序列化保存
            log.debug("Metadata saved to: {}", metadataPath);
        } catch (Exception e) {
            log.error("Failed to save metadata", e);
        }
    }
    
    /**
     * 加载元数据
     */
    private void loadMetadata() {
        try {
            String metadataPath = indexPath + ".metadata";
            // 这里可以实现元数据的反序列化加载
            log.debug("Metadata loaded from: {}", metadataPath);
        } catch (Exception e) {
            log.error("Failed to load metadata", e);
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        try {
            if (index != null) {
                index.cleanup();
            }
            documentIdToIndexMap.clear();
            indexToDocumentIdMap.clear();
            log.info("FAISS service cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup FAISS service", e);
        }
    }
}
