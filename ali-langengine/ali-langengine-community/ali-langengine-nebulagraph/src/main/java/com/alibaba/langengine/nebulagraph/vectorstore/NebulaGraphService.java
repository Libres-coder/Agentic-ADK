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
package com.alibaba.langengine.nebulagraph.vectorstore;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.nebulagraph.client.NebulaGraphVectorClient;
import com.alibaba.langengine.nebulagraph.exception.NebulaGraphVectorStoreException;
import com.alibaba.langengine.nebulagraph.model.NebulaGraphQueryRequest;
import com.alibaba.langengine.nebulagraph.model.NebulaGraphQueryResponse;
import com.alibaba.langengine.nebulagraph.model.NebulaGraphVector;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.net.Session;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Data
public class NebulaGraphService {
    
    private final NebulaGraphVectorClient client;
    private final String spaceName;
    private final String tagName;
    private final NebulaGraphParam param;
    private final Map<String, Object> queryCache;
    private final boolean enableCache;
    
    /**
     * 构造函数
     */
    public NebulaGraphService(NebulaGraphVectorClient client, String spaceName, String tagName, NebulaGraphParam param) {
        this.client = client;
        this.spaceName = spaceName;
        this.tagName = tagName != null ? tagName : "Document";
        this.param = param != null ? param : NebulaGraphParam.createDefault();
        this.enableCache = this.param.getInitParam().isEnableQueryCache();
        this.queryCache = enableCache ? new ConcurrentHashMap<>() : null;
        
        log.info("NebulaGraph service initialized: space={}, tag={}, cache={}", 
                spaceName, this.tagName, enableCache);
    }
    
    /**
     * 初始化图空间和标签
     */
    public void init() {
        try {
            log.info("Initializing NebulaGraph space and schema...");
            
            // 创建图空间
            createSpaceIfNotExists();
            
            // 创建标签
            createTagIfNotExists();
            
            // 创建向量索引
            if (param.getInitParam().isEnableAutoIndex()) {
                createVectorIndexIfNotExists();
            }
            
            log.info("NebulaGraph initialization completed");
            
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.graphSchemaError(
                "Failed to initialize NebulaGraph schema", e);
        }
    }
    
    /**
     * 创建图空间（如果不存在）
     */
    private void createSpaceIfNotExists() {
        Session session = null;
        try {
            session = client.getSession();
            
            // 检查图空间是否存在
            String checkSpaceQuery = "SHOW SPACES";
            ResultSet result = session.execute(checkSpaceQuery);
            
            boolean spaceExists = false;
            if (result.isSucceeded()) {
                for (int i = 0; i < result.rowsSize(); i++) {
                    ValueWrapper spaceNameWrapper = result.rowValues(i).get(0);
                    if (spaceNameWrapper.isString() && spaceName.equals(spaceNameWrapper.asString())) {
                        spaceExists = true;
                        break;
                    }
                }
            }
            
            if (!spaceExists) {
                // 创建图空间
                String createSpaceQuery = String.format(
                    "CREATE SPACE IF NOT EXISTS %s (partition_num = 10, replica_factor = 1, vid_type = FIXED_STRING(64))",
                    spaceName);
                
                ResultSet createResult = session.execute(createSpaceQuery);
                if (!createResult.isSucceeded()) {
                    throw NebulaGraphVectorStoreException.spaceError(
                        "Failed to create space: " + createResult.getErrorMessage(), null);
                }
                
                // 等待图空间创建完成
                Thread.sleep(3000);
                log.info("Created NebulaGraph space: {}", spaceName);
            } else {
                log.info("NebulaGraph space already exists: {}", spaceName);
            }
            
            // 使用图空间
            String useSpaceQuery = "USE " + spaceName;
            ResultSet useResult = session.execute(useSpaceQuery);
            if (!useResult.isSucceeded()) {
                throw NebulaGraphVectorStoreException.spaceError(
                    "Failed to use space: " + useResult.getErrorMessage(), null);
            }
            
        } catch (NebulaGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.spaceError(
                "Error creating space", e);
        } finally {
            client.releaseSession(session);
        }
    }
    
    /**
     * 创建标签（如果不存在）
     */
    private void createTagIfNotExists() {
        Session session = null;
        try {
            session = client.getSession();
            
            // 使用图空间
            session.execute("USE " + spaceName);
            
            // 创建标签
            String createTagQuery = String.format(
                "CREATE TAG IF NOT EXISTS %s (" +
                "%s string NOT NULL, " +
                "%s string NOT NULL, " +
                "%s string, " +
                "%s string, " +
                "%s string, " +
                "%s string, " +
                "%s string, " +
                "%s string, " +
                "%s int64, " +
                "%s int64, " +
                "%s list<double> NOT NULL" +
                ")",
                tagName,
                param.getFieldNameUniqueId(),
                param.getFieldNamePageContent(),
                param.getFieldMeta(),
                param.getFieldTitle(),
                param.getFieldDocIndex(),
                param.getFieldDocType(),
                param.getFieldTags(),
                param.getFieldCustomFields(),
                param.getFieldCreatedAt(),
                param.getFieldUpdatedAt(),
                param.getFieldVector()
            );
            
            ResultSet result = session.execute(createTagQuery);
            if (!result.isSucceeded()) {
                throw NebulaGraphVectorStoreException.tagError(
                    "Failed to create tag: " + result.getErrorMessage(), null);
            }
            
            // 等待标签创建完成
            Thread.sleep(2000);
            log.info("Created NebulaGraph tag: {}", tagName);
            
        } catch (NebulaGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.tagError(
                "Error creating tag", e);
        } finally {
            client.releaseSession(session);
        }
    }
    
    /**
     * 创建向量索引（如果不存在）
     */
    private void createVectorIndexIfNotExists() {
        Session session = null;
        try {
            session = client.getSession();
            
            // 使用图空间
            session.execute("USE " + spaceName);
            
            // 创建向量索引
            String indexName = tagName + "_vector_index";
            String createIndexQuery = String.format(
                "CREATE TAG INDEX IF NOT EXISTS %s ON %s(%s)",
                indexName, tagName, param.getFieldVector()
            );
            
            ResultSet result = session.execute(createIndexQuery);
            if (!result.isSucceeded()) {
                log.warn("Failed to create vector index, this is expected if vector indexing is not supported: {}", 
                        result.getErrorMessage());
            } else {
                // 等待索引创建完成
                Thread.sleep(2000);
                log.info("Created NebulaGraph vector index: {}", indexName);
            }
            
        } catch (Exception e) {
            log.warn("Error creating vector index, continuing without it", e);
        } finally {
            client.releaseSession(session);
        }
    }
    
    /**
     * 插入向量数据
     */
    public void insertVectors(List<NebulaGraphVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return;
        }
        
        Session session = null;
        try {
            session = client.getSession();
            
            // 使用图空间
            session.execute("USE " + spaceName);
            
            int batchSize = param.getInitParam().getBatchSize();
            for (int i = 0; i < vectors.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, vectors.size());
                List<NebulaGraphVector> batch = vectors.subList(i, endIndex);
                insertVectorBatch(session, batch);
            }
            
            log.info("Inserted {} vectors into NebulaGraph", vectors.size());
            
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.insertError(
                "Failed to insert vectors", e);
        } finally {
            client.releaseSession(session);
        }
    }
    
    /**
     * 批量插入向量
     */
    private void insertVectorBatch(Session session, List<NebulaGraphVector> vectors) {
        try {
            StringBuilder insertQuery = new StringBuilder();
            insertQuery.append("INSERT VERTEX ").append(tagName).append(" (");
            insertQuery.append(param.getFieldNameUniqueId()).append(", ");
            insertQuery.append(param.getFieldNamePageContent()).append(", ");
            insertQuery.append(param.getFieldMeta()).append(", ");
            insertQuery.append(param.getFieldTitle()).append(", ");
            insertQuery.append(param.getFieldDocIndex()).append(", ");
            insertQuery.append(param.getFieldDocType()).append(", ");
            insertQuery.append(param.getFieldTags()).append(", ");
            insertQuery.append(param.getFieldCustomFields()).append(", ");
            insertQuery.append(param.getFieldCreatedAt()).append(", ");
            insertQuery.append(param.getFieldUpdatedAt()).append(", ");
            insertQuery.append(param.getFieldVector());
            insertQuery.append(") VALUES ");
            
            for (int i = 0; i < vectors.size(); i++) {
                if (i > 0) {
                    insertQuery.append(", ");
                }
                
                NebulaGraphVector vector = vectors.get(i);
                insertQuery.append("\"").append(vector.getUniqueId()).append("\":(");
                insertQuery.append("\"").append(vector.getUniqueId()).append("\", ");
                insertQuery.append("\"").append(escapeString(vector.getContent())).append("\", ");
                insertQuery.append("\"").append(serializeMetadata(vector.getMetadata())).append("\", ");
                insertQuery.append("\"").append(escapeString(vector.getTitle())).append("\", ");
                insertQuery.append("\"").append(escapeString(vector.getDocIndex())).append("\", ");
                insertQuery.append("\"").append(escapeString(vector.getDocType())).append("\", ");
                insertQuery.append("\"").append(serializeTags(vector.getTags())).append("\", ");
                insertQuery.append("\"").append(serializeCustomFields(vector.getCustomFields())).append("\", ");
                insertQuery.append(vector.getCreatedAt() != null ? vector.getCreatedAt() : System.currentTimeMillis()).append(", ");
                insertQuery.append(vector.getUpdatedAt() != null ? vector.getUpdatedAt() : System.currentTimeMillis()).append(", ");
                insertQuery.append("[").append(vector.getVector().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "))).append("])");
            }
            
            ResultSet result = session.execute(insertQuery.toString());
            if (!result.isSucceeded()) {
                throw NebulaGraphVectorStoreException.insertError(
                    "Failed to insert vector batch: " + result.getErrorMessage(), null);
            }
            
        } catch (NebulaGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.insertError(
                "Error inserting vector batch", e);
        }
    }
    
    /**
     * 查询相似向量
     */
    public NebulaGraphQueryResponse querySimilarVectors(NebulaGraphQueryRequest request) {
        try {
            request.validate();
            
            // 检查缓存
            String cacheKey = generateCacheKey(request);
            if (enableCache && queryCache.containsKey(cacheKey)) {
                log.debug("Cache hit for query: {}", cacheKey);
                return (NebulaGraphQueryResponse) queryCache.get(cacheKey);
            }
            
            long startTime = System.currentTimeMillis();
            
            Session session = null;
            try {
                session = client.getSession();
                
                // 使用图空间
                session.execute("USE " + spaceName);
                
                // 构建查询语句
                String query = buildSimilarityQuery(request);
                log.debug("Executing similarity query: {}", query);
                
                ResultSet result = session.execute(query);
                if (!result.isSucceeded()) {
                    throw NebulaGraphVectorStoreException.queryError(
                        "Failed to execute similarity query: " + result.getErrorMessage(), null);
                }
                
                // 解析查询结果
                NebulaGraphQueryResponse response = parseQueryResult(result, request);
                response.setExecutionTime(System.currentTimeMillis() - startTime);
                
                // 缓存结果
                if (enableCache) {
                    queryCache.put(cacheKey, response);
                    
                    // 清理过期缓存
                    if (queryCache.size() > param.getInitParam().getMaxCacheSize()) {
                        clearExpiredCache();
                    }
                }
                
                return response;
                
            } finally {
                client.releaseSession(session);
            }
            
        } catch (NebulaGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.queryError(
                "Error executing similarity query", e);
        }
    }
    
    /**
     * 构建相似度查询语句
     */
    private String buildSimilarityQuery(NebulaGraphQueryRequest request) {
        // 注意：NebulaGraph的向量相似度查询语法可能需要根据实际版本调整
        // 这里提供一个基本的实现框架
        
        StringBuilder query = new StringBuilder();
        query.append("MATCH (v:").append(tagName).append(") ");
        
        // 添加过滤条件
        if (request.getFilter() != null && !request.getFilter().isEmpty()) {
            query.append("WHERE ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : request.getFilter().entrySet()) {
                if (!first) query.append(" AND ");
                query.append("v.").append(entry.getKey()).append(" == ");
                if (entry.getValue() instanceof String) {
                    query.append("\"").append(escapeString((String) entry.getValue())).append("\"");
                } else {
                    query.append(entry.getValue());
                }
                first = false;
            }
        }
        
        query.append("RETURN ");
        query.append("v.").append(param.getFieldNameUniqueId()).append(" AS unique_id, ");
        query.append("v.").append(param.getFieldNamePageContent()).append(" AS content, ");
        
        if (request.isIncludeMetadata()) {
            query.append("v.").append(param.getFieldMeta()).append(" AS metadata, ");
        }
        
        if (request.isIncludeVector()) {
            query.append("v.").append(param.getFieldVector()).append(" AS vector, ");
        }
        
        // 计算相似度（这里需要根据NebulaGraph的实际向量函数调整）
        query.append("0.0 AS distance ");  // 占位符，实际实现需要向量相似度计算
        
        query.append("ORDER BY distance ");
        if (request.getDistanceFunction() == NebulaGraphQueryRequest.DistanceFunction.COSINE) {
            query.append("DESC ");
        } else {
            query.append("ASC ");
        }
        
        query.append("LIMIT ").append(request.getTopK());
        
        return query.toString();
    }
    
    /**
     * 解析查询结果
     */
    private NebulaGraphQueryResponse parseQueryResult(ResultSet resultSet, NebulaGraphQueryRequest request) {
        NebulaGraphQueryResponse response = new NebulaGraphQueryResponse();
        List<NebulaGraphQueryResponse.DocumentResult> documents = new ArrayList<>();
        
        try {
            for (int i = 0; i < resultSet.rowsSize(); i++) {
                ResultSet.Record record = resultSet.rowValues(i);
                List<ValueWrapper> row = record.values();

                NebulaGraphQueryResponse.DocumentResult document = new NebulaGraphQueryResponse.DocumentResult();
                
                // 解析基本字段
                if (row.size() > 0 && row.get(0).isString()) {
                    document.setUniqueId(row.get(0).asString());
                }
                
                if (row.size() > 1 && row.get(1).isString()) {
                    document.setContent(row.get(1).asString());
                }
                
                // 解析元数据
                if (request.isIncludeMetadata() && row.size() > 2 && row.get(2).isString()) {
                    document.setMetadata(deserializeMetadata(row.get(2).asString()));
                }
                
                // 解析向量
                if (request.isIncludeVector()) {
                    // 根据实际的列索引调整
                    int vectorIndex = request.isIncludeMetadata() ? 3 : 2;
                    if (row.size() > vectorIndex && row.get(vectorIndex).isList()) {
                        document.setVector(parseVectorFromList(row.get(vectorIndex)));
                    }
                }
                
                // 解析距离和分数
                int distanceIndex = request.isIncludeVector() ? 
                    (request.isIncludeMetadata() ? 4 : 3) : 
                    (request.isIncludeMetadata() ? 3 : 2);
                
                if (row.size() > distanceIndex && row.get(distanceIndex).isDouble()) {
                    document.setDistance(row.get(distanceIndex).asDouble());
                    document.calculateScore();
                }
                
                document.validate();
                documents.add(document);
            }
            
            response.setDocuments(documents);
            response.setSuccess(true);
            
        } catch (Exception e) {
            log.error("Error parsing query result", e);
            response.setSuccess(false);
            response.setErrorMessage("Failed to parse query result: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 删除向量
     */
    public void deleteVectors(List<String> uniqueIds) {
        if (uniqueIds == null || uniqueIds.isEmpty()) {
            return;
        }
        
        Session session = null;
        try {
            session = client.getSession();
            
            // 使用图空间
            session.execute("USE " + spaceName);
            
            String deleteQuery = "DELETE VERTEX " + 
                uniqueIds.stream()
                    .map(id -> "\"" + id + "\"")
                    .collect(Collectors.joining(", "));
            
            ResultSet result = session.execute(deleteQuery);
            if (!result.isSucceeded()) {
                throw NebulaGraphVectorStoreException.deleteError(
                    "Failed to delete vectors: " + result.getErrorMessage(), null);
            }
            
            log.info("Deleted {} vectors from NebulaGraph", uniqueIds.size());
            
            // 清理缓存
            if (enableCache) {
                queryCache.clear();
            }
            
        } catch (NebulaGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.deleteError(
                "Error deleting vectors", e);
        } finally {
            client.releaseSession(session);
        }
    }
    
    // 辅助方法
    
    private String escapeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        // 简单的JSON序列化，实际可以使用更完善的JSON库
        return metadata.entrySet().stream()
            .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
            .collect(Collectors.joining(",", "{", "}"));
    }
    
    private String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        return tags.stream()
            .map(tag -> "\"" + tag + "\"")
            .collect(Collectors.joining(",", "[", "]"));
    }
    
    private String serializeCustomFields(Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return "{}";
        }
        return fields.entrySet().stream()
            .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
            .collect(Collectors.joining(",", "{", "}"));
    }
    
    private Map<String, Object> deserializeMetadata(String metadataStr) {
        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.isBlank(metadataStr) || "{}".equals(metadataStr)) {
            return metadata;
        }
        // 简单的JSON反序列化，实际应该使用更完善的JSON库
        // 这里仅作示例
        return metadata;
    }
    
    private List<Double> parseVectorFromList(ValueWrapper listWrapper) {
        List<Double> vector = new ArrayList<>();
        // 解析NebulaGraph的list类型到Double列表
        // 具体实现需要根据NebulaGraph Java客户端API调整
        return vector;
    }
    
    private String generateCacheKey(NebulaGraphQueryRequest request) {
        return String.format("query_%s_%d_%f_%s", 
            request.getQueryVector().hashCode(),
            request.getTopK(),
            request.getSimilarityThreshold(),
            request.getDistanceFunction());
    }
    
    private void clearExpiredCache() {
        // 简单的缓存清理策略：保留最近一半的缓存
        int targetSize = param.getInitParam().getMaxCacheSize() / 2;
        if (queryCache.size() > targetSize) {
            queryCache.clear();
            log.debug("Cleared query cache");
        }
    }
}
