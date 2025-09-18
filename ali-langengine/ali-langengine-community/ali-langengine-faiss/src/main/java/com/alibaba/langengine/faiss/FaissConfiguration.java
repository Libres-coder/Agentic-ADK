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
package com.alibaba.langengine.faiss;

import lombok.Data;

/**
 * FAISS 配置类
 * 
 * @author langengine
 */
@Data
public class FaissConfiguration {
    
    /**
     * FAISS索引文件路径
     */
    public static final String FAISS_INDEX_PATH = System.getProperty("faiss.index.path", "./faiss_index");
    
    /**
     * FAISS索引类型
     */
    public static final String FAISS_INDEX_TYPE = System.getProperty("faiss.index.type", "IVFFlat");
    
    /**
     * 向量维度
     */
    public static final int FAISS_VECTOR_DIM = Integer.parseInt(System.getProperty("faiss.vector.dim", "768"));
    
    /**
     * 聚类中心数量
     */
    public static final int FAISS_NLIST = Integer.parseInt(System.getProperty("faiss.nlist", "100"));
    
    /**
     * 量化器类型
     */
    public static final String FAISS_QUANTIZER_TYPE = System.getProperty("faiss.quantizer.type", "Flat");
    
    /**
     * 是否使用GPU
     */
    public static final boolean FAISS_USE_GPU = Boolean.parseBoolean(System.getProperty("faiss.use.gpu", "false"));
    
    /**
     * GPU设备ID
     */
    public static final int FAISS_GPU_DEVICE_ID = Integer.parseInt(System.getProperty("faiss.gpu.device.id", "0"));
    
    /**
     * 搜索参数 - 返回结果数量
     */
    public static final int FAISS_SEARCH_K = Integer.parseInt(System.getProperty("faiss.search.k", "10"));
    
    /**
     * 搜索参数 - 最大距离阈值
     */
    public static final double FAISS_SEARCH_MAX_DISTANCE = Double.parseDouble(System.getProperty("faiss.search.max.distance", "1.0"));
    
    /**
     * 是否启用索引持久化
     */
    public static final boolean FAISS_ENABLE_PERSISTENCE = Boolean.parseBoolean(System.getProperty("faiss.enable.persistence", "true"));
    
    /**
     * 索引重建阈值
     */
    public static final int FAISS_REBUILD_THRESHOLD = Integer.parseInt(System.getProperty("faiss.rebuild.threshold", "10000"));
    
    /**
     * 批量插入大小
     */
    public static final int FAISS_BATCH_SIZE = Integer.parseInt(System.getProperty("faiss.batch.size", "1000"));
}
