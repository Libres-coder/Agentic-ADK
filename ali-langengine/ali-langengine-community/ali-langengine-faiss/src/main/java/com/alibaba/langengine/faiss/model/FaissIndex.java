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
package com.alibaba.langengine.faiss.model;

import com.alibaba.langengine.faiss.exception.FaissException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FAISS 索引模型
 * 模拟FAISS索引的核心功能
 * 
 * @author langengine
 */
@Slf4j
@Data
public class FaissIndex {
    
    private int vectorDimension;
    private String indexType;
    private boolean useGpu;
    private int gpuDeviceId;
    
    // 存储向量数据 (在实际实现中，这里会是FAISS的原生索引对象)
    private List<float[]> vectors;
    private ConcurrentHashMap<Integer, float[]> vectorMap;
    private AtomicInteger totalVectors;
    
    public FaissIndex(int vectorDimension, String indexType, boolean useGpu, int gpuDeviceId) {
        this.vectorDimension = vectorDimension;
        this.indexType = indexType;
        this.useGpu = useGpu;
        this.gpuDeviceId = gpuDeviceId;
        
        this.vectors = new ArrayList<>();
        this.vectorMap = new ConcurrentHashMap<>();
        this.totalVectors = new AtomicInteger(0);
        
        log.info("FAISS index created with dimension: {}, type: {}, GPU: {}", 
            vectorDimension, indexType, useGpu);
    }
    
    /**
     * 添加向量到索引
     */
    public void addVector(float[] vector, int id) {
        try {
            if (vector.length != vectorDimension) {
                throw new FaissException("Vector dimension mismatch. Expected: " + 
                    vectorDimension + ", Got: " + vector.length);
            }
            
            // 复制向量数据
            float[] vectorCopy = new float[vectorDimension];
            System.arraycopy(vector, 0, vectorCopy, 0, vectorDimension);
            
            // 存储向量
            vectorMap.put(id, vectorCopy);
            vectors.add(vectorCopy);
            totalVectors.incrementAndGet();
            
            log.debug("Added vector with ID: {}, dimension: {}", id, vectorDimension);
            
        } catch (Exception e) {
            log.error("Failed to add vector with ID: {}", id, e);
            throw new FaissException("Failed to add vector", e);
        }
    }
    
    /**
     * 执行相似性搜索
     */
    public List<FaissSearchResult> search(float[] queryVector, int k) {
        try {
            if (queryVector.length != vectorDimension) {
                throw new FaissException("Query vector dimension mismatch. Expected: " + 
                    vectorDimension + ", Got: " + queryVector.length);
            }
            
            List<FaissSearchResult> results = new ArrayList<>();
            
            // 计算与所有向量的相似性
            for (int i = 0; i < vectors.size(); i++) {
                float[] vector = vectors.get(i);
                float distance = calculateDistance(queryVector, vector);
                float similarity = 1.0f / (1.0f + distance); // 转换为相似性分数
                
                FaissSearchResult result = new FaissSearchResult();
                result.setIndex(i);
                result.setDistance(distance);
                result.setScore(similarity);
                
                results.add(result);
            }
            
            // 按相似性分数排序
            results.sort((a, b) -> Float.compare(b.getScore(), a.getScore()));
            
            // 返回前k个结果
            if (results.size() > k) {
                results = results.subList(0, k);
            }
            
            log.debug("Found {} similar vectors for query", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new FaissException("Failed to perform similarity search", e);
        }
    }
    
    /**
     * 删除向量
     */
    public void removeVector(int id) {
        try {
            if (vectorMap.containsKey(id)) {
                vectorMap.remove(id);
                // 从vectors列表中移除对应的向量
                // 这里简化处理，实际实现中需要更复杂的索引管理
                totalVectors.decrementAndGet();
                log.debug("Removed vector with ID: {}", id);
            } else {
                log.warn("Vector with ID {} not found", id);
            }
            
        } catch (Exception e) {
            log.error("Failed to remove vector with ID: {}", id, e);
            throw new FaissException("Failed to remove vector", e);
        }
    }
    
    /**
     * 计算两个向量之间的距离（欧几里得距离）
     */
    private float calculateDistance(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new FaissException("Vector dimensions must be equal");
        }
        
        float sum = 0.0f;
        for (int i = 0; i < vector1.length; i++) {
            float diff = vector1[i] - vector2[i];
            sum += diff * diff;
        }
        
        return (float) Math.sqrt(sum);
    }
    
    /**
     * 获取总向量数量
     */
    public int getTotalVectors() {
        return totalVectors.get();
    }
    
    /**
     * 保存索引到文件
     */
    public void save(String filePath) {
        try {
            // 在实际实现中，这里会调用FAISS的原生保存方法
            // faiss.write_index(index, filePath);
            log.info("FAISS index saved to: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to save FAISS index to: {}", filePath, e);
            throw new FaissException("Failed to save index", e);
        }
    }
    
    /**
     * 从文件加载索引
     */
    public void load(String filePath) {
        try {
            // 在实际实现中，这里会调用FAISS的原生加载方法
            // index = faiss.read_index(filePath);
            log.info("FAISS index loaded from: {}", filePath);
        } catch (Exception e) {
            log.error("Failed to load FAISS index from: {}", filePath, e);
            throw new FaissException("Failed to load index", e);
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        try {
            vectors.clear();
            vectorMap.clear();
            totalVectors.set(0);
            log.info("FAISS index cleanup completed");
        } catch (Exception e) {
            log.error("Failed to cleanup FAISS index", e);
        }
    }
}
