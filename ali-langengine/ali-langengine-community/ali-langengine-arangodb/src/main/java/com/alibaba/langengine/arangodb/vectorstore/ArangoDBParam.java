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
package com.alibaba.langengine.arangodb.vectorstore;

import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.model.ArangoDBQueryRequest;
import lombok.Data;


@Data
public class ArangoDBParam {
    
    /**
     * 初始化参数
     */
    private InitParam initParam = new InitParam();
    
    /**
     * 页面内容字段名
     */
    private String fieldNamePageContent = "content";
    
    /**
     * 唯一ID字段名
     */
    private String fieldNameUniqueId = "unique_id";
    
    /**
     * 元数据字段名
     */
    private String fieldMeta = "metadata";
    
    /**
     * 向量字段名
     */
    private String fieldVector = "vector";
    
    /**
     * 标题字段名
     */
    private String fieldTitle = "title";
    
    /**
     * 文档索引字段名
     */
    private String fieldDocIndex = "doc_index";
    
    /**
     * 文档类型字段名
     */
    private String fieldDocType = "doc_type";
    
    /**
     * 标签字段名
     */
    private String fieldTags = "tags";
    
    /**
     * 自定义字段名
     */
    private String fieldCustomFields = "custom_fields";
    
    /**
     * 创建时间字段名
     */
    private String fieldCreatedAt = "created_at";
    
    /**
     * 更新时间字段名
     */
    private String fieldUpdatedAt = "updated_at";
    
    /**
     * 初始化参数内部类
     */
    @Data
    public static class InitParam {
        
        /**
         * 向量维度
         */
        private int dimension = ArangoDBConfiguration.DEFAULT_VECTOR_DIMENSION;
        
        /**
         * 距离函数类型
         */
        private String distanceFunction = ArangoDBConfiguration.DEFAULT_DISTANCE_FUNCTION;
        
        /**
         * 相似度阈值
         */
        private double similarityThreshold = ArangoDBConfiguration.DEFAULT_SIMILARITY_THRESHOLD;
        
        /**
         * 批处理大小
         */
        private int batchSize = ArangoDBConfiguration.DEFAULT_BATCH_SIZE;
        
        /**
         * 最大缓存大小
         */
        private int maxCacheSize = ArangoDBConfiguration.DEFAULT_MAX_CACHE_SIZE;
        
        /**
         * 连接超时时间
         */
        private int timeoutMs = ArangoDBConfiguration.DEFAULT_TIMEOUT_MS;
        
        /**
         * 是否包含向量数据在查询结果中
         */
        private boolean includeVector = true;
        
        /**
         * 是否包含元数据在查询结果中
         */
        private boolean includeMetadata = true;
        
        /**
         * 默认返回结果数量
         */
        private int defaultTopK = 10;
        
        /**
         * 最大返回结果数量
         */
        private int maxTopK = 1000;
        
        /**
         * 是否启用查询缓存
         */
        private boolean enableQueryCache = true;
        
        /**
         * 查询缓存过期时间（秒）
         */
        private int queryCacheExpireSeconds = 300;
        
        /**
         * 是否启用向量压缩
         */
        private boolean enableVectorCompression = false;
        
        /**
         * 向量压缩精度
         */
        private int vectorCompressionPrecision = 6;
        
        /**
         * 是否启用自动索引创建
         */
        private boolean enableAutoIndex = true;
        
        /**
         * 索引创建策略
         */
        private String indexStrategy = "background";
        
        /**
         * 是否启用分片
         */
        private boolean enableSharding = false;
        
        /**
         * 分片数量
         */
        private int shardCount = 1;
        
        /**
         * 复制因子
         */
        private int replicationFactor = 1;
        
        /**
         * 验证参数
         */
        public void validate() {
            if (dimension <= 0) {
                throw new IllegalArgumentException("Dimension must be positive");
            }
            
            if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
                throw new IllegalArgumentException("Similarity threshold must be between 0.0 and 1.0");
            }
            
            if (batchSize <= 0) {
                throw new IllegalArgumentException("Batch size must be positive");
            }
            
            if (maxCacheSize <= 0) {
                throw new IllegalArgumentException("Max cache size must be positive");
            }
            
            if (timeoutMs <= 0) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            
            if (defaultTopK <= 0) {
                throw new IllegalArgumentException("Default topK must be positive");
            }
            
            if (maxTopK <= 0 || maxTopK < defaultTopK) {
                throw new IllegalArgumentException("Max topK must be positive and >= defaultTopK");
            }
            
            if (queryCacheExpireSeconds <= 0) {
                throw new IllegalArgumentException("Query cache expire seconds must be positive");
            }
            
            if (vectorCompressionPrecision <= 0) {
                throw new IllegalArgumentException("Vector compression precision must be positive");
            }
            
            if (shardCount <= 0) {
                throw new IllegalArgumentException("Shard count must be positive");
            }
            
            if (replicationFactor <= 0) {
                throw new IllegalArgumentException("Replication factor must be positive");
            }
        }
        
        /**
         * 获取距离函数枚举
         */
        public ArangoDBQueryRequest.DistanceFunction getDistanceFunctionEnum() {
            return ArangoDBQueryRequest.DistanceFunction.fromString(distanceFunction);
        }
        
        /**
         * 设置距离函数枚举
         */
        public void setDistanceFunctionEnum(ArangoDBQueryRequest.DistanceFunction distanceFunction) {
            this.distanceFunction = distanceFunction.getValue();
        }
    }
    
    /**
     * 验证参数
     */
    public void validate() {
        if (initParam != null) {
            initParam.validate();
        }
        
        validateFieldNames();
    }
    
    /**
     * 验证字段名
     */
    private void validateFieldNames() {
        if (fieldNamePageContent == null || fieldNamePageContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name for page content cannot be null or empty");
        }
        
        if (fieldNameUniqueId == null || fieldNameUniqueId.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name for unique ID cannot be null or empty");
        }
        
        if (fieldMeta == null || fieldMeta.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name for metadata cannot be null or empty");
        }
        
        if (fieldVector == null || fieldVector.trim().isEmpty()) {
            throw new IllegalArgumentException("Field name for vector cannot be null or empty");
        }
    }
    
    /**
     * 创建默认参数
     */
    public static ArangoDBParam createDefault() {
        ArangoDBParam param = new ArangoDBParam();
        param.setInitParam(new InitParam());
        return param;
    }
    
    /**
     * 创建带维度的参数
     */
    public static ArangoDBParam createWithDimension(int dimension) {
        ArangoDBParam param = createDefault();
        param.getInitParam().setDimension(dimension);
        return param;
    }
    
    /**
     * 创建带维度和相似度阈值的参数
     */
    public static ArangoDBParam createWithDimensionAndThreshold(int dimension, double similarityThreshold) {
        ArangoDBParam param = createWithDimension(dimension);
        param.getInitParam().setSimilarityThreshold(similarityThreshold);
        return param;
    }
    
    /**
     * 创建用于测试的参数
     */
    public static ArangoDBParam createForTesting() {
        ArangoDBParam param = createDefault();
        InitParam initParam = param.getInitParam();
        
        initParam.setDimension(384);  // 较小的维度用于测试
        initParam.setBatchSize(10);
        initParam.setMaxCacheSize(100);
        initParam.setDefaultTopK(5);
        initParam.setMaxTopK(50);
        initParam.setSimilarityThreshold(0.5);
        initParam.setEnableQueryCache(false);  // 测试时禁用缓存
        
        return param;
    }
    
    /**
     * 复制参数
     */
    public ArangoDBParam copy() {
        ArangoDBParam copy = new ArangoDBParam();
        
        // 复制字段名
        copy.setFieldNamePageContent(this.fieldNamePageContent);
        copy.setFieldNameUniqueId(this.fieldNameUniqueId);
        copy.setFieldMeta(this.fieldMeta);
        copy.setFieldVector(this.fieldVector);
        copy.setFieldTitle(this.fieldTitle);
        copy.setFieldDocIndex(this.fieldDocIndex);
        copy.setFieldDocType(this.fieldDocType);
        copy.setFieldTags(this.fieldTags);
        copy.setFieldCustomFields(this.fieldCustomFields);
        copy.setFieldCreatedAt(this.fieldCreatedAt);
        copy.setFieldUpdatedAt(this.fieldUpdatedAt);
        
        // 复制初始化参数
        if (this.initParam != null) {
            InitParam initParamCopy = new InitParam();
            initParamCopy.setDimension(this.initParam.getDimension());
            initParamCopy.setDistanceFunction(this.initParam.getDistanceFunction());
            initParamCopy.setSimilarityThreshold(this.initParam.getSimilarityThreshold());
            initParamCopy.setBatchSize(this.initParam.getBatchSize());
            initParamCopy.setMaxCacheSize(this.initParam.getMaxCacheSize());
            initParamCopy.setTimeoutMs(this.initParam.getTimeoutMs());
            initParamCopy.setIncludeVector(this.initParam.isIncludeVector());
            initParamCopy.setIncludeMetadata(this.initParam.isIncludeMetadata());
            initParamCopy.setDefaultTopK(this.initParam.getDefaultTopK());
            initParamCopy.setMaxTopK(this.initParam.getMaxTopK());
            initParamCopy.setEnableQueryCache(this.initParam.isEnableQueryCache());
            initParamCopy.setQueryCacheExpireSeconds(this.initParam.getQueryCacheExpireSeconds());
            initParamCopy.setEnableVectorCompression(this.initParam.isEnableVectorCompression());
            initParamCopy.setVectorCompressionPrecision(this.initParam.getVectorCompressionPrecision());
            initParamCopy.setEnableAutoIndex(this.initParam.isEnableAutoIndex());
            initParamCopy.setIndexStrategy(this.initParam.getIndexStrategy());
            initParamCopy.setEnableSharding(this.initParam.isEnableSharding());
            initParamCopy.setShardCount(this.initParam.getShardCount());
            initParamCopy.setReplicationFactor(this.initParam.getReplicationFactor());
            
            copy.setInitParam(initParamCopy);
        }
        
        return copy;
    }
}
