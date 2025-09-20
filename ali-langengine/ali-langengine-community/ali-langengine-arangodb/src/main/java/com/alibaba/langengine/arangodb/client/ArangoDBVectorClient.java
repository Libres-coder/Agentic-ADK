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
package com.alibaba.langengine.arangodb.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.cache.LRUCache;
import com.alibaba.langengine.arangodb.exception.ArangoDBVectorStoreException;
import com.alibaba.langengine.arangodb.model.ArangoDBQueryRequest;
import com.alibaba.langengine.arangodb.model.ArangoDBQueryResponse;
import com.alibaba.langengine.arangodb.model.ArangoDBVector;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoCollection;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentCreateEntity;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.PersistentIndexOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;



@Slf4j
public class ArangoDBVectorClient implements AutoCloseable {
    
    private final ArangoDB arangoDB;
    private final ArangoDatabase database;
    private final ArangoDBConfiguration configuration;
    private final LRUCache<String, List<Double>> vectorCache;
    private final LRUCache<String, ArangoCollection> collectionCache;
    private final ArangoSearchManager searchManager;
    
    /**
     * 构造函数
     * 
     * <p>创建一个新的 ArangoDB 向量存储客户端实例。该构造函数会：</p>
     * <ul>
     *   <li>验证配置参数的有效性</li>
     *   <li>建立与 ArangoDB 数据库的连接</li>
     *   <li>初始化数据库（如果不存在则创建）</li>
     *   <li>设置 LRU 缓存</li>
     *   <li>初始化 ArangoSearch 管理器</li>
     * </ul>
     * 
     * @param configuration ArangoDB 连接配置，不能为 null
     * @throws ArangoDBVectorStoreException 如果配置无效或连接失败
     * @throws IllegalArgumentException 如果配置为 null
     * 
     * @see ArangoDBConfiguration#validate()
     * @see #initializeDatabase()
     * @see ArangoSearchManager
     */
    public ArangoDBVectorClient(ArangoDBConfiguration configuration) {
        this.configuration = configuration;
        this.configuration.validate();
        
        try {
            // 创建 ArangoDB 连接
            this.arangoDB = new ArangoDB.Builder()
                    .host(configuration.getHost(), configuration.getPort())
                    .user(configuration.getUsername())
                    .password(configuration.getPassword())
                    .build();
            
            // 获取数据库
            this.database = initializeDatabase();
            
            // 初始化 LRU 缓存
            this.vectorCache = new LRUCache<>(configuration.getMaxCacheSize());
            this.collectionCache = new LRUCache<>(100); // 集合缓存容量较小
            
            // 初始化 ArangoSearch 管理器
            this.searchManager = new ArangoSearchManager(database, configuration);
            
            log.info("ArangoDB vector client initialized successfully: {}:{}/{}", 
                    configuration.getHost(), configuration.getPort(), configuration.getDatabase());
                    
        } catch (Exception e) {
            throw ArangoDBVectorStoreException.connectionError(
                    "Failed to initialize ArangoDB vector client", e);
        }
    }
    
    /**
     * 初始化数据库
     */
    private ArangoDatabase initializeDatabase() {
        try {
            // 检查数据库是否存在
            if (!arangoDB.getDatabases().contains(configuration.getDatabase())) {
                // 创建数据库
                arangoDB.createDatabase(configuration.getDatabase());
                log.info("Created database: {}", configuration.getDatabase());
            }
            
            return arangoDB.db(configuration.getDatabase());
            
        } catch (Exception e) {
            throw ArangoDBVectorStoreException.databaseError(
                    "Failed to initialize database: " + configuration.getDatabase(), e);
        }
    }
    
    /**
     * 获取或创建集合
     */
    public ArangoCollection getOrCreateCollection(String collectionName) {
        return collectionCache.computeIfAbsent(collectionName, name -> {
            try {
                ArangoCollection collection;
                
                if (database.collection(name).exists()) {
                    collection = database.collection(name);
                    log.debug("Using existing collection: {}", name);
                } else {
                    // 创建集合选项
                    CollectionCreateOptions options = new CollectionCreateOptions()
                            .type(com.arangodb.entity.CollectionType.DOCUMENT);
                    
                    CollectionEntity collectionEntity = database.createCollection(name, options);
                    collection = database.collection(name);
                    
                    // 创建向量索引（如果支持）
                    createVectorIndex(collection);
                    
                    log.info("Created new collection: {}", name);
                }
                
                return collection;
                
            } catch (Exception e) {
                throw ArangoDBVectorStoreException.collectionError(
                        "Failed to get or create collection: " + name, e);
            }
        });
    }
    
    /**
     * 创建向量索引
     */
    private void createVectorIndex(ArangoCollection collection) {
        try {
            // 创建常规索引用于元数据查询
            Map<String, Object> indexOptions = new HashMap<>();
            indexOptions.put("type", "persistent");
            indexOptions.put("fields", Arrays.asList("unique_id", "doc_type", "created_at"));
            indexOptions.put("unique", false);
            indexOptions.put("sparse", true);
            
            // 创建常规索引，使用简化的方法
            try {
                collection.ensurePersistentIndex(Arrays.asList("unique_id", "doc_type", "created_at"), 
                    new PersistentIndexOptions().sparse(true));
            } catch (Exception e) {
                log.warn("Failed to create persistent index: {}", e.getMessage());
            }
            
            // 为向量字段创建索引（用于范围查询）
            try {
                collection.ensurePersistentIndex(Arrays.asList("dimension"), 
                    new PersistentIndexOptions().sparse(true));
            } catch (Exception e) {
                log.warn("Failed to create vector index: {}", e.getMessage());
            }
            
            // 创建 ArangoSearch 视图用于优化的向量搜索
            try {
                String viewName = searchManager.getOrCreateVectorSearchView(collection.name());
                if (viewName != null) {
                    log.info("Created ArangoSearch view for collection: {} -> {}", collection.name(), viewName);
                }
            } catch (Exception e) {
                log.warn("Failed to create ArangoSearch view for collection: {}", collection.name(), e);
            }
            
            log.info("Created indexes for collection: {}", collection.name());
            
        } catch (Exception e) {
            log.warn("Failed to create vector index for collection: {}", collection.name(), e);
        }
    }
    
    /**
     * 插入向量文档
     */
    public void insertVector(String collectionName, ArangoDBVector vector) {
        insertVectors(collectionName, Collections.singletonList(vector));
    }
    
    /**
     * 批量插入向量文档
     */
    public void insertVectors(String collectionName, List<ArangoDBVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return;
        }
        
        try {
            ArangoCollection collection = getOrCreateCollection(collectionName);
            
            // 验证并准备文档
            List<BaseDocument> documents = vectors.stream()
                    .peek(ArangoDBVector::validate)
                    .map(this::convertToBaseDocument)
                    .collect(Collectors.toList());
            
            // 批量插入
            DocumentCreateOptions options = new DocumentCreateOptions()
                    .returnNew(false)
                    .waitForSync(false);
                    
            MultiDocumentEntity<DocumentCreateEntity<Void>> result = 
                    collection.insertDocuments(documents, options);
                    
            // 更新缓存
            vectors.forEach(vector -> updateCache(vector.getUniqueId(), vector.getVector()));
            
            log.info("Successfully inserted {} vectors into collection: {}", 
                    vectors.size(), collectionName);
                    
        } catch (Exception e) {
            throw ArangoDBVectorStoreException.insertError(
                    "Failed to insert vectors into collection: " + collectionName, e);
        }
    }
    
    /**
     * 向量相似度搜索
     */
    public ArangoDBQueryResponse querySimilarVectors(String collectionName, ArangoDBQueryRequest request) {
        request.validate();
        
        long startTime = System.currentTimeMillis();
        
        try {
            ArangoCollection collection = getOrCreateCollection(collectionName);
            
            // 尝试使用 ArangoSearch 优化查询
            String aqlQuery;
            Map<String, Object> bindVars = request.buildOptimizedBindVariables();
            
            String viewName = searchManager.getOrCreateVectorSearchView(collectionName);
            if (viewName != null && searchManager.viewExists(viewName)) {
                // 使用 ArangoSearch 优化查询
                Map<String, Object> searchParams = new HashMap<>(bindVars);
                searchParams.put("docType", request.getDocTypeFilter());
                searchParams.put("tags", request.getTagFilter());
                searchParams.put("metadataFilter", request.getMetadataFilter());
                searchParams.put("includeVector", request.isIncludeVector());
                
                aqlQuery = searchManager.buildOptimizedVectorSearchQuery(collectionName, viewName, searchParams);
                bindVars.put("@view", viewName);
                
                log.debug("Using ArangoSearch optimized query: {}", aqlQuery);
            } else {
                // 回退到传统查询
                aqlQuery = buildSimilarityQuery(request);
                log.debug("Using traditional AQL query: {}", aqlQuery);
            }
            
            log.debug("Bind variables: {}", bindVars);
            
            // 执行查询
            List<BaseDocument> results = database.query(aqlQuery, BaseDocument.class, bindVars).asListRemaining();
            
            // 转换结果
            List<ArangoDBVector> vectors = results.stream()
                    .map(this::convertFromBaseDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("Found {} similar vectors in {}ms using {}", 
                    vectors.size(), executionTime, 
                    viewName != null ? "ArangoSearch" : "traditional query");
            
            return ArangoDBQueryResponse.success(vectors, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to query similar vectors", e);
            
            return ArangoDBQueryResponse.failure("QUERY_ERROR", e.getMessage(), executionTime);
        }
    }
    
    /**
     * 全文搜索
     */
    public ArangoDBQueryResponse fullTextSearch(String collectionName, String searchText, 
                                               Map<String, Object> filters, int topK) {
        long startTime = System.currentTimeMillis();
        
        try {
            String viewName = searchManager.getOrCreateVectorSearchView(collectionName);
            if (viewName == null || !searchManager.viewExists(viewName)) {
                throw new IllegalStateException("ArangoSearch view not available for collection: " + collectionName);
            }
            
            // 构建搜索参数
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("searchText", searchText);
            searchParams.put("topK", topK);
            searchParams.putAll(filters);
            
            // 构建查询
            String aqlQuery = searchManager.buildFullTextSearchQuery(collectionName, viewName, searchParams);
            Map<String, Object> bindVars = new HashMap<>();
            bindVars.put("@view", viewName);
            bindVars.putAll(searchParams);
            
            log.debug("Executing full-text search query: {}", aqlQuery);
            
            // 执行查询
            List<BaseDocument> results = database.query(aqlQuery, BaseDocument.class, bindVars).asListRemaining();
            
            // 转换结果
            List<ArangoDBVector> vectors = results.stream()
                    .map(this::convertFromBaseDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("Found {} documents in full-text search in {}ms", vectors.size(), executionTime);
            
            return ArangoDBQueryResponse.success(vectors, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to perform full-text search", e);
            
            return ArangoDBQueryResponse.failure("FULLTEXT_SEARCH_ERROR", e.getMessage(), executionTime);
        }
    }
    
    /**
     * 混合搜索（向量 + 全文）
     */
    public ArangoDBQueryResponse hybridSearch(String collectionName, List<Double> queryVector, 
                                             String searchText, Map<String, Object> filters, 
                                             int topK, double vectorWeight, double textWeight) {
        long startTime = System.currentTimeMillis();
        
        try {
            String viewName = searchManager.getOrCreateVectorSearchView(collectionName);
            if (viewName == null || !searchManager.viewExists(viewName)) {
                throw new IllegalStateException("ArangoSearch view not available for collection: " + collectionName);
            }
            
            // 构建搜索参数
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("queryVector", queryVector);
            searchParams.put("searchText", searchText);
            searchParams.put("topK", topK);
            searchParams.put("vectorWeight", vectorWeight);
            searchParams.put("textWeight", textWeight);
            searchParams.put("similarityThreshold", 0.0); // 混合搜索通常不设置阈值
            searchParams.putAll(filters);
            
            // 构建查询
            String aqlQuery = searchManager.buildHybridSearchQuery(collectionName, viewName, searchParams);
            Map<String, Object> bindVars = new HashMap<>();
            bindVars.put("@view", viewName);
            bindVars.putAll(searchParams);
            
            log.debug("Executing hybrid search query: {}", aqlQuery);
            
            // 执行查询
            List<BaseDocument> results = database.query(aqlQuery, BaseDocument.class, bindVars).asListRemaining();
            
            // 转换结果
            List<ArangoDBVector> vectors = results.stream()
                    .map(this::convertFromBaseDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            log.info("Found {} documents in hybrid search in {}ms", vectors.size(), executionTime);
            
            return ArangoDBQueryResponse.success(vectors, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to perform hybrid search", e);
            
            return ArangoDBQueryResponse.failure("HYBRID_SEARCH_ERROR", e.getMessage(), executionTime);
        }
    }
    
    /**
     * 构建相似度查询 AQL
     */
    private String buildSimilarityQuery(ArangoDBQueryRequest request) {
        StringBuilder aql = new StringBuilder();
        
        aql.append("FOR doc IN @@collection ");
        
        // 添加过滤条件
        String whereClause = request.buildWhereClause();
        if (!whereClause.isEmpty()) {
            aql.append("FILTER 1 == 1").append(whereClause).append(" ");
        }
        
        // 计算相似度
        aql.append("LET similarity = ");
        switch (request.getDistanceFunction()) {
            case COSINE:
                aql.append("COSINE_SIMILARITY(doc.vector, @queryVector) ");
                break;
            case DOT_PRODUCT:
                aql.append("DOT_PRODUCT(doc.vector, @queryVector) ");
                break;
            case EUCLIDEAN:
                aql.append("1 / (1 + SQRT(SUM((doc.vector[*] - @queryVector[*]) ** 2))) ");
                break;
            default:
                aql.append("COSINE_SIMILARITY(doc.vector, @queryVector) ");
                break;
        }
        
        // 相似度过滤
        aql.append("FILTER similarity >= @similarityThreshold ");
        
        // 计算距离
        aql.append("LET distance = 1 - similarity ");
        
        // 最大距离过滤
        if (request.getMaxDistance() != null) {
            aql.append("FILTER distance <= @maxDistance ");
        }
        
        // 排序和限制
        aql.append("SORT similarity DESC ");
        aql.append("LIMIT @topK ");
        
        // 返回字段
        aql.append("RETURN MERGE(doc, { ");
        aql.append("score: similarity, ");
        aql.append("distance: distance");
        
        if (!request.isIncludeVector()) {
            aql.append(", vector: null");
        }
        
        aql.append(" })");
        
        return aql.toString();
    }
    
    /**
     * 构建绑定变量
     */
    private Map<String, Object> buildBindVariables(ArangoDBQueryRequest request) {
        Map<String, Object> bindVars = new HashMap<>();
        
        bindVars.put("queryVector", request.getQueryVector());
        bindVars.put("topK", request.getTopK());
        bindVars.put("similarityThreshold", request.getSimilarityThreshold());
        
        if (request.getMaxDistance() != null) {
            bindVars.put("maxDistance", request.getMaxDistance());
        }
        
        if (request.hasDocTypeFilter()) {
            bindVars.put("docType", request.getDocTypeFilter());
        }
        
        if (request.hasTagFilter()) {
            bindVars.put("tags", request.getTagFilter());
        }
        
        if (request.hasMetadataFilter()) {
            for (Map.Entry<String, Object> entry : request.getMetadataFilter().entrySet()) {
                bindVars.put("metadata_" + entry.getKey(), entry.getValue());
            }
        }
        
        if (request.hasCustomFieldsFilter()) {
            for (Map.Entry<String, Object> entry : request.getCustomFieldsFilter().entrySet()) {
                bindVars.put("custom_" + entry.getKey(), entry.getValue());
            }
        }
        
        return bindVars;
    }
    
    /**
     * 转换为 BaseDocument
     */
    private BaseDocument convertToBaseDocument(ArangoDBVector vector) {
        BaseDocument doc = new BaseDocument();
        
        if (vector.getKey() != null) {
            doc.setKey(vector.getKey());
        }
        
        doc.addAttribute("vector", vector.getVector());
        doc.addAttribute("content", vector.getContent());
        doc.addAttribute("unique_id", vector.getUniqueId());
        doc.addAttribute("dimension", vector.getDimension());
        doc.addAttribute("created_at", vector.getCreatedAt());
        doc.addAttribute("updated_at", vector.getUpdatedAt());
        
        if (vector.getTitle() != null) {
            doc.addAttribute("title", vector.getTitle());
        }
        
        if (vector.getDocIndex() != null) {
            doc.addAttribute("doc_index", vector.getDocIndex());
        }
        
        if (vector.getDocType() != null) {
            doc.addAttribute("doc_type", vector.getDocType());
        }
        
        if (vector.getMetadata() != null) {
            doc.addAttribute("metadata", vector.getMetadata());
        }
        
        if (vector.getTags() != null) {
            doc.addAttribute("tags", vector.getTags());
        }
        
        if (vector.getCustomFields() != null) {
            doc.addAttribute("custom_fields", vector.getCustomFields());
        }
        
        return doc;
    }
    
    /**
     * 从 BaseDocument 转换
     */
    @SuppressWarnings("unchecked")
    private ArangoDBVector convertFromBaseDocument(BaseDocument document) {
        try {
            // 转换为 Map
            Map<String, Object> map = document.getProperties();
            
            ArangoDBVector vector = new ArangoDBVector();
            
            // 基础字段
            vector.setKey((String) map.get("_key"));
            vector.setId((String) map.get("_id"));
            vector.setRev((String) map.get("_rev"));
            vector.setContent((String) map.get("content"));
            vector.setUniqueId((String) map.get("unique_id"));
            vector.setTitle((String) map.get("title"));
            vector.setDocIndex((String) map.get("doc_index"));
            vector.setDocType((String) map.get("doc_type"));
            
            // 向量数据
            if (map.get("vector") instanceof List) {
                List<?> vectorList = (List<?>) map.get("vector");
                List<Double> doubleVector = vectorList.stream()
                        .map(v -> v instanceof Number ? ((Number) v).doubleValue() : Double.parseDouble(v.toString()))
                        .collect(Collectors.toList());
                vector.setVector(doubleVector);
            }
            
            // 维度
            if (map.get("dimension") instanceof Number) {
                vector.setDimension(((Number) map.get("dimension")).intValue());
            }
            
            // 分数和距离
            if (map.get("score") instanceof Number) {
                vector.setScore(((Number) map.get("score")).doubleValue());
            }
            
            if (map.get("distance") instanceof Number) {
                vector.setDistance(((Number) map.get("distance")).doubleValue());
            }
            
            // 元数据
            if (map.get("metadata") instanceof Map) {
                vector.setMetadata((Map<String, Object>) map.get("metadata"));
            }
            
            // 标签
            if (map.get("tags") instanceof List) {
                List<?> tagsList = (List<?>) map.get("tags");
                List<String> stringTags = tagsList.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                vector.setTags(stringTags);
            }
            
            // 自定义字段
            if (map.get("custom_fields") instanceof Map) {
                vector.setCustomFields((Map<String, Object>) map.get("custom_fields"));
            }
            
            // 时间字段
            // 处理时间戳字段...
            
            return vector;
            
        } catch (Exception e) {
            log.error("Failed to convert VPackSlice to ArangoDBVector", e);
            return null;
        }
    }
    
    /**
     * 更新向量缓存
     */
    private void updateCache(String docId, List<Double> vector) {
        if (docId != null && vector != null) {
            vectorCache.put(docId, new ArrayList<>(vector));
        }
    }
    
    /**
     * 测试连接
     */
    public boolean ping() {
        try {
            database.getInfo();
            return true;
        } catch (Exception e) {
            log.error("Ping failed", e);
            return false;
        }
    }
    
    /**
     * 获取集合统计信息
     */
    public Map<String, Object> getCollectionStats(String collectionName) {
        try {
            ArangoCollection collection = getOrCreateCollection(collectionName);
            Map<String, Object> stats = new HashMap<>();
            stats.put("name", collection.name());
            stats.put("count", collection.count());
            stats.put("vectorCacheStats", vectorCache.getStats());
            stats.put("collectionCacheStats", collectionCache.getStats());
            return stats;
        } catch (Exception e) {
            log.error("Failed to get collection stats", e);
            return Map.of("error", e.getMessage());
        }
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        vectorCache.clear();
        collectionCache.clear();
        log.info("Cleared vector cache");
    }
    
    @Override
    public void close() {
        try {
            clearCache();
            
            // 清理 ArangoSearch 管理器
            if (searchManager != null) {
                searchManager.cleanup();
            }
            
            if (arangoDB != null) {
                arangoDB.shutdown();
            }
            
            log.info("ArangoDB vector client closed");
            
        } catch (Exception e) {
            log.error("Error closing ArangoDB client", e);
        }
    }
}
