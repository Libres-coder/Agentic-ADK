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

import com.alibaba.langengine.arangodb.ArangoDBConfiguration;
import com.alibaba.langengine.arangodb.exception.ArangoDBVectorStoreException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.ViewEntity;
import com.arangodb.entity.ViewType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class ArangoSearchManager {
    
    private final ArangoDatabase database;
    private final ArangoDBConfiguration configuration;
    private final Map<String, String> viewCache;
    
    public ArangoSearchManager(ArangoDatabase database, ArangoDBConfiguration configuration) {
        this.database = database;
        this.configuration = configuration;
        this.viewCache = new HashMap<>();
    }
    
    /**
     * 为集合创建或获取 ArangoSearch 视图
     */
    public String getOrCreateVectorSearchView(String collectionName) {
        String viewName = collectionName + "_vector_search";
        
        return viewCache.computeIfAbsent(viewName, name -> {
            try {
                // 检查视图是否已存在
                if (database.view(name).exists()) {
                    log.debug("Using existing ArangoSearch view: {}", name);
                    return name;
                }
                
                // 创建 ArangoSearch 视图
                createVectorSearchView(name, collectionName);
                log.info("Created ArangoSearch view: {} for collection: {}", name, collectionName);
                return name;
                
            } catch (Exception e) {
                log.warn("Failed to create ArangoSearch view: {}", name, e);
                return null;
            }
        });
    }
    
    /**
     * 创建向量搜索视图
     * 注意：这是一个简化实现，实际使用中需要根据 ArangoDB 版本调整
     */
    private void createVectorSearchView(String viewName, String collectionName) {
        try {
            // 创建基本的 ArangoSearch 视图
            // 使用正确的 ViewType 枚举类型创建视图
            ViewEntity viewEntity = database.createView(viewName, ViewType.ARANGO_SEARCH);
            log.info("Successfully created ArangoSearch view: {}", viewName);
            
        } catch (Exception e) {
            log.warn("Failed to create ArangoSearch view: {}", viewName, e);
            // 不抛出异常，允许系统继续运行，使用传统查询方式
        }
    }
    
    /**
     * 创建优化的向量搜索 AQL 查询
     */
    public String buildOptimizedVectorSearchQuery(String collectionName, String viewName, 
                                                  Map<String, Object> queryParams) {
        StringBuilder aql = new StringBuilder();
        
        // 使用 ArangoSearch 视图进行搜索
        aql.append("FOR doc IN @@view ");
        
        // 添加过滤条件
        if (queryParams.containsKey("docType")) {
            aql.append("FILTER doc.doc_type == @docType ");
        }
        
        if (queryParams.containsKey("tags")) {
            aql.append("FILTER @tags ALL IN doc.tags ");
        }
        
        if (queryParams.containsKey("metadataFilter")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataFilter = (Map<String, Object>) queryParams.get("metadataFilter");
            for (Map.Entry<String, Object> entry : metadataFilter.entrySet()) {
                aql.append("FILTER doc.metadata.").append(entry.getKey()).append(" == @metadata_")
                   .append(entry.getKey()).append(" ");
            }
        }
        
        // 向量相似度计算
        aql.append("LET similarity = COSINE_SIMILARITY(doc.vector, @queryVector) ");
        aql.append("FILTER similarity >= @similarityThreshold ");
        
        // 计算距离
        aql.append("LET distance = 1 - similarity ");
        
        if (queryParams.containsKey("maxDistance")) {
            aql.append("FILTER distance <= @maxDistance ");
        }
        
        // 排序和限制
        aql.append("SORT similarity DESC ");
        aql.append("LIMIT @topK ");
        
        // 返回结果
        aql.append("RETURN MERGE(doc, { ");
        aql.append("score: similarity, ");
        aql.append("distance: distance");
        
        if (Boolean.FALSE.equals(queryParams.get("includeVector"))) {
            aql.append(", vector: null");
        }
        
        aql.append(" })");
        
        return aql.toString();
    }
    
    /**
     * 创建全文搜索 AQL 查询
     */
    public String buildFullTextSearchQuery(String collectionName, String viewName, 
                                          Map<String, Object> queryParams) {
        StringBuilder aql = new StringBuilder();
        
        aql.append("FOR doc IN SEARCH(@@view, { ");
        
        // 全文搜索条件
        if (queryParams.containsKey("searchText")) {
            aql.append("query_string: { ");
            aql.append("query: @searchText, ");
            aql.append("default_operator: 'AND' ");
            aql.append("}");
        }
        
        // 过滤条件
        if (queryParams.containsKey("docType")) {
            aql.append(", doc_type: @docType ");
        }
        
        if (queryParams.containsKey("tags")) {
            aql.append(", tags: @tags ");
        }
        
        aql.append("}) ");
        
        // 额外的过滤条件
        if (queryParams.containsKey("metadataFilter")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataFilter = (Map<String, Object>) queryParams.get("metadataFilter");
            for (Map.Entry<String, Object> entry : metadataFilter.entrySet()) {
                aql.append("FILTER doc.metadata.").append(entry.getKey()).append(" == @metadata_")
                   .append(entry.getKey()).append(" ");
            }
        }
        
        // 排序和限制
        aql.append("SORT BM25(doc) DESC ");
        aql.append("LIMIT @topK ");
        
        // 返回结果
        aql.append("RETURN doc");
        
        return aql.toString();
    }
    
    /**
     * 创建混合搜索 AQL 查询（向量 + 全文）
     */
    public String buildHybridSearchQuery(String collectionName, String viewName, 
                                        Map<String, Object> queryParams) {
        StringBuilder aql = new StringBuilder();
        
        aql.append("FOR doc IN @@view ");
        
        // 过滤条件
        if (queryParams.containsKey("docType")) {
            aql.append("FILTER doc.doc_type == @docType ");
        }
        
        if (queryParams.containsKey("tags")) {
            aql.append("FILTER @tags ALL IN doc.tags ");
        }
        
        // 计算向量相似度
        aql.append("LET vectorScore = COSINE_SIMILARITY(doc.vector, @queryVector) ");
        
        // 计算全文搜索分数
        aql.append("LET textScore = BM25(doc) ");
        
        // 混合分数计算（可配置权重）
        double vectorWeight = (Double) queryParams.getOrDefault("vectorWeight", 0.7);
        double textWeight = (Double) queryParams.getOrDefault("textWeight", 0.3);
        
        aql.append("LET hybridScore = @vectorWeight * vectorScore + @textWeight * textScore ");
        aql.append("FILTER hybridScore >= @similarityThreshold ");
        
        // 排序和限制
        aql.append("SORT hybridScore DESC ");
        aql.append("LIMIT @topK ");
        
        // 返回结果
        aql.append("RETURN MERGE(doc, { ");
        aql.append("score: hybridScore, ");
        aql.append("vectorScore: vectorScore, ");
        aql.append("textScore: textScore");
        
        if (Boolean.FALSE.equals(queryParams.get("includeVector"))) {
            aql.append(", vector: null");
        }
        
        aql.append(" })");
        
        return aql.toString();
    }
    
    /**
     * 检查视图是否存在
     */
    public boolean viewExists(String viewName) {
        try {
            return database.view(viewName).exists();
        } catch (Exception e) {
            log.warn("Failed to check if view exists: {}", viewName, e);
            return false;
        }
    }
    
    /**
     * 删除视图
     */
    public void deleteView(String viewName) {
        try {
            if (database.view(viewName).exists()) {
                database.view(viewName).drop();
                viewCache.remove(viewName);
                log.info("Deleted ArangoSearch view: {}", viewName);
            }
        } catch (Exception e) {
            log.warn("Failed to delete view: {}", viewName, e);
        }
    }
    
    /**
     * 获取视图信息
     */
    public Map<String, Object> getViewInfo(String viewName) {
        try {
            if (database.view(viewName).exists()) {
                ViewEntity viewEntity = database.view(viewName).getInfo();
                return Map.of(
                        "name", viewEntity.getName(),
                        "type", viewEntity.getType(),
                        "id", viewEntity.getId()
                );
            }
            return Map.of("error", "View not found");
        } catch (Exception e) {
            log.error("Failed to get view info: {}", viewName, e);
            return Map.of("error", e.getMessage());
        }
    }
    
    /**
     * 清理所有视图
     */
    public void cleanup() {
        for (String viewName : new ArrayList<>(viewCache.keySet())) {
            deleteView(viewName);
        }
        viewCache.clear();
        log.info("Cleaned up all ArangoSearch views");
    }
}
