/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.annoy.service;

import com.alibaba.langengine.annoy.model.AnnoyIndex;
import com.alibaba.langengine.annoy.model.AnnoyParam;
import com.alibaba.langengine.annoy.model.AnnoySearchResult;
import com.alibaba.langengine.annoy.exception.AnnoyException;
import com.alibaba.langengine.core.indexes.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class AnnoyService {

    /**
     * 索引管理器
     */
    private final Map<String, AnnoyIndex> indexMap = new ConcurrentHashMap<>();

    /**
     * 向量ID计数器
     */
    private final AtomicInteger vectorIdCounter = new AtomicInteger(0);

    /**
     * 文档ID到向量ID的映射
     */
    private final Map<String, Integer> documentIdToVectorId = new ConcurrentHashMap<>();

    /**
     * 向量ID到文档的映射
     */
    private final Map<Integer, Document> vectorIdToDocument = new ConcurrentHashMap<>();

    /**
     * 向量数据存储（内存中的向量数据）
     */
    private final Map<Integer, List<Float>> vectorData = new ConcurrentHashMap<>();

    /**
     * 默认索引ID
     */
    private static final String DEFAULT_INDEX_ID = "default";

    /**
     * 初始化服务
     */
    public void initialize() {
        log.info("Initializing AnnoyService...");
        // 创建索引存储目录
        createIndexDirectory();
        log.info("AnnoyService initialized successfully");
    }

    /**
     * 创建索引
     */
    public AnnoyIndex createIndex(String indexId, AnnoyParam param) {
        if (StringUtils.isEmpty(indexId)) {
            indexId = DEFAULT_INDEX_ID;
        }

        // 验证参数
        param.validate();

        // 生成索引文件路径
        String indexPath = generateIndexPath(indexId);

        // 创建索引对象
        AnnoyIndex index = AnnoyIndex.create(indexId, indexId, indexPath, param);

        // 存储索引
        indexMap.put(indexId, index);

        log.info("Created Annoy index: {}", index.getSummary());
        return index;
    }

    /**
     * 获取或创建默认索引
     */
    public AnnoyIndex getOrCreateDefaultIndex(AnnoyParam param) {
        return indexMap.computeIfAbsent(DEFAULT_INDEX_ID, k -> createIndex(DEFAULT_INDEX_ID, param));
    }

    /**
     * 添加文档向量
     */
    public void addDocuments(List<Document> documents, String indexId) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        AnnoyIndex index = indexMap.get(indexId);
        if (index == null) {
            throw new AnnoyException.VectorAddException("Index not found: " + indexId);
        }

        try {
            for (Document document : documents) {
                addDocument(document, index);
            }
            
            // 标记索引需要重新构建
            index.setBuilt(false);
            index.setLoaded(false);
            
            log.info("Added {} documents to index {}", documents.size(), indexId);
        } catch (Exception e) {
            throw new AnnoyException.VectorAddException("Failed to add documents to index " + indexId, e);
        }
    }

    /**
     * 添加单个文档
     */
    private void addDocument(Document document, AnnoyIndex index) {
        if (document.getEmbedding() == null || document.getEmbedding().isEmpty()) {
            throw new AnnoyException.VectorAddException("Document embedding is null or empty");
        }

        // 检查向量维度
        List<Double> embedding = document.getEmbedding();
        if (embedding.size() != index.getParam().getVectorDimension()) {
            throw new AnnoyException.VectorAddException(
                String.format("Vector dimension mismatch: expected %d, got %d", 
                    index.getParam().getVectorDimension(), embedding.size()));
        }

        // 转换向量格式
        List<Float> vector = new ArrayList<>();
        for (Double d : embedding) {
            vector.add(d.floatValue());
        }

        // 生成向量ID
        int vectorId = vectorIdCounter.getAndIncrement();

        // 存储映射关系
        if (StringUtils.isNotEmpty(document.getUniqueId())) {
            documentIdToVectorId.put(document.getUniqueId(), vectorId);
        }
        vectorIdToDocument.put(vectorId, document);
        vectorData.put(vectorId, vector);

        // 更新索引统计
        index.incrementVectorCount();
    }

    /**
     * 构建索引
     */
    public void buildIndex(String indexId) {
        AnnoyIndex index = indexMap.get(indexId);
        if (index == null) {
            throw new AnnoyException.IndexBuildException("Index not found: " + indexId);
        }

        if (index.getVectorCount().get() == 0) {
            throw new AnnoyException.IndexBuildException("No vectors to build index for: " + indexId);
        }

        try {
            index.setStatus(AnnoyIndex.IndexStatus.BUILDING);
            
            // 模拟索引构建过程（实际实现中会调用Annoy C++库）
            log.info("Building index {} with {} vectors...", indexId, index.getVectorCount().get());
            
            // 这里应该调用Annoy C++库进行实际的索引构建
            // 由于没有实际的C++库，我们模拟构建过程
            simulateIndexBuild(index);
            
            index.setBuildCompleted();
            log.info("Index {} built successfully", indexId);
            
        } catch (Exception e) {
            index.setErrorStatus();
            throw new AnnoyException.IndexBuildException("Failed to build index " + indexId, e);
        }
    }

    /**
     * 加载索引
     */
    public void loadIndex(String indexId) {
        AnnoyIndex index = indexMap.get(indexId);
        if (index == null) {
            throw new AnnoyException.IndexLoadException("Index not found: " + indexId);
        }

        if (!index.isBuilt()) {
            throw new AnnoyException.IndexLoadException("Index not built: " + indexId);
        }

        try {
            index.setStatus(AnnoyIndex.IndexStatus.LOADING);
            
            // 模拟索引加载过程
            log.info("Loading index {}...", indexId);
            simulateIndexLoad(index);
            
            index.setLoadCompleted();
            log.info("Index {} loaded successfully", indexId);
            
        } catch (Exception e) {
            index.setErrorStatus();
            throw new AnnoyException.IndexLoadException("Failed to load index " + indexId, e);
        }
    }

    /**
     * 相似性搜索
     */
    public List<AnnoySearchResult> similaritySearch(List<Float> queryVector, int k, String indexId) {
        AnnoyIndex index = indexMap.get(indexId);
        if (index == null) {
            throw new AnnoyException.SearchException("Index not found: " + indexId);
        }

        if (!index.isAvailable()) {
            throw new AnnoyException.SearchException("Index not available: " + indexId);
        }

        if (queryVector.size() != index.getParam().getVectorDimension()) {
            throw new AnnoyException.SearchException(
                String.format("Query vector dimension mismatch: expected %d, got %d", 
                    index.getParam().getVectorDimension(), queryVector.size()));
        }

        try {
            // 模拟相似性搜索（实际实现中会调用Annoy C++库）
            List<AnnoySearchResult> results = simulateSearch(queryVector, k, index);
            
            log.debug("Found {} similar vectors for query in index {}", results.size(), indexId);
            return results;
            
        } catch (Exception e) {
            throw new AnnoyException.SearchException("Failed to search in index " + indexId, e);
        }
    }

    /**
     * 获取文档通过向量ID
     */
    public Document getDocumentByVectorId(Integer vectorId) {
        return vectorIdToDocument.get(vectorId);
    }

    /**
     * 获取向量ID通过文档ID
     */
    public Integer getVectorIdByDocumentId(String documentId) {
        return documentIdToVectorId.get(documentId);
    }

    /**
     * 获取索引信息
     */
    public AnnoyIndex getIndex(String indexId) {
        return indexMap.get(indexId);
    }

    /**
     * 获取所有索引
     */
    public Collection<AnnoyIndex> getAllIndexes() {
        return indexMap.values();
    }

    /**
     * 删除索引
     */
    public void deleteIndex(String indexId) {
        AnnoyIndex index = indexMap.remove(indexId);
        if (index != null) {
            // 清理相关数据
            cleanupIndexData(index);
            
            // 删除索引文件
            File indexFile = index.getIndexFile();
            if (indexFile.exists()) {
                indexFile.delete();
            }
            
            log.info("Deleted index: {}", indexId);
        }
    }

    /**
     * 生成索引文件路径
     */
    private String generateIndexPath(String indexId) {
        return Paths.get(com.alibaba.langengine.annoy.AnnoyConfiguration.ANNOY_INDEX_PATH, 
                        com.alibaba.langengine.annoy.AnnoyConfiguration.ANNOY_INDEX_PREFIX + "_" + indexId + ".ann")
                    .toString();
    }

    /**
     * 创建索引存储目录
     */
    private void createIndexDirectory() {
        try {
            Files.createDirectories(Paths.get(com.alibaba.langengine.annoy.AnnoyConfiguration.ANNOY_INDEX_PATH));
        } catch (Exception e) {
            throw new AnnoyException.FileOperationException("Failed to create index directory", e);
        }
    }

    /**
     * 模拟索引构建
     */
    private void simulateIndexBuild(AnnoyIndex index) throws InterruptedException {
        // 模拟构建时间
        Thread.sleep(100);
        
        // 创建索引文件
        try {
            File indexFile = index.getIndexFile();
            indexFile.getParentFile().mkdirs();
            indexFile.createNewFile();
        } catch (Exception e) {
            throw new AnnoyException.IndexBuildException("Failed to create index file", e);
        }
    }

    /**
     * 模拟索引加载
     */
    private void simulateIndexLoad(AnnoyIndex index) throws InterruptedException {
        // 模拟加载时间
        Thread.sleep(50);
    }

    /**
     * 模拟相似性搜索
     */
    private List<AnnoySearchResult> simulateSearch(List<Float> queryVector, int k, AnnoyIndex index) {
        List<AnnoySearchResult> results = new ArrayList<>();
        
        // 简单的线性搜索实现（实际中会使用Annoy的近似搜索）
        List<Map.Entry<Integer, Float>> distances = new ArrayList<>();
        
        for (Map.Entry<Integer, List<Float>> entry : vectorData.entrySet()) {
            float distance = calculateDistance(queryVector, entry.getValue(), index.getParam().getDistanceMetric());
            distances.add(new AbstractMap.SimpleEntry<>(entry.getKey(), distance));
        }
        
        // 排序并取前k个
        distances.sort(Map.Entry.comparingByValue());
        
        for (int i = 0; i < Math.min(k, distances.size()); i++) {
            Map.Entry<Integer, Float> entry = distances.get(i);
            AnnoySearchResult result = AnnoySearchResult.create(entry.getKey(), entry.getValue());
            result.calculateSimilarity(index.getParam().getDistanceMetric());
            results.add(result);
        }
        
        return results;
    }

    /**
     * 计算距离
     */
    private float calculateDistance(List<Float> v1, List<Float> v2, String metric) {
        switch (metric.toLowerCase()) {
            case "euclidean":
                return calculateEuclideanDistance(v1, v2);
            case "angular":
            case "cosine":
                return calculateAngularDistance(v1, v2);
            case "manhattan":
                return calculateManhattanDistance(v1, v2);
            case "dot":
                return calculateDotProduct(v1, v2);
            default:
                return calculateEuclideanDistance(v1, v2);
        }
    }

    private float calculateEuclideanDistance(List<Float> v1, List<Float> v2) {
        float sum = 0;
        for (int i = 0; i < v1.size(); i++) {
            float diff = v1.get(i) - v2.get(i);
            sum += diff * diff;
        }
        return (float) Math.sqrt(sum);
    }

    private float calculateAngularDistance(List<Float> v1, List<Float> v2) {
        float dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }
        float cosine = dot / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
        return (float) Math.acos(Math.max(-1, Math.min(1, cosine)));
    }

    private float calculateManhattanDistance(List<Float> v1, List<Float> v2) {
        float sum = 0;
        for (int i = 0; i < v1.size(); i++) {
            sum += Math.abs(v1.get(i) - v2.get(i));
        }
        return sum;
    }

    private float calculateDotProduct(List<Float> v1, List<Float> v2) {
        float sum = 0;
        for (int i = 0; i < v1.size(); i++) {
            sum += v1.get(i) * v2.get(i);
        }
        return -sum; // Negative because we want smaller distances for higher similarity
    }

    /**
     * 清理索引数据
     */
    private void cleanupIndexData(AnnoyIndex index) {
        // 清理与该索引相关的向量数据
        Set<Integer> vectorIdsToRemove = new HashSet<>();
        for (Map.Entry<Integer, Document> entry : vectorIdToDocument.entrySet()) {
            // 这里简化处理，实际中需要更精确的索引关联
            vectorIdsToRemove.add(entry.getKey());
        }
        
        for (Integer vectorId : vectorIdsToRemove) {
            Document doc = vectorIdToDocument.remove(vectorId);
            if (doc != null && StringUtils.isNotEmpty(doc.getUniqueId())) {
                documentIdToVectorId.remove(doc.getUniqueId());
            }
            vectorData.remove(vectorId);
        }
    }
}
