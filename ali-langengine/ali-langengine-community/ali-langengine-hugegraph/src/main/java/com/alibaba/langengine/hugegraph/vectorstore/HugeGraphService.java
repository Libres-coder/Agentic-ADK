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
package com.alibaba.langengine.hugegraph.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.hugegraph.client.HugeGraphClient;
import com.alibaba.langengine.hugegraph.exception.HugeGraphVectorStoreException;
import org.apache.hugegraph.structure.gremlin.ResultSet;
import org.apache.hugegraph.structure.graph.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Data
public class HugeGraphService implements AutoCloseable {

    private final HugeGraphClient hugeGraphClient;
    private final HugeGraphParam hugeGraphParam;

    public HugeGraphService(HugeGraphParam hugeGraphParam) {
        this.hugeGraphParam = hugeGraphParam;
        this.hugeGraphClient = new HugeGraphClient(hugeGraphParam.getServerConfig(), hugeGraphParam.getConnectionConfig());
    }

    /**
     * 初始化Schema和索引
     */
    public void initializeSchema(Embeddings embedding) {
        if (!hugeGraphParam.getInitParam().getCreateSchemaOnInit()) {
            log.info("Schema creation is disabled, skipping schema initialization");
            return;
        }

        try {
            HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();

            // 创建属性键
            hugeGraphClient.createPropertyKeyIfNotExists(vectorConfig.getIdPropertyName(), String.class);
            hugeGraphClient.createPropertyKeyIfNotExists(vectorConfig.getContentPropertyName(), String.class);
            hugeGraphClient.createPropertyKeyIfNotExists(vectorConfig.getVectorPropertyName(), String.class); // Store vector as string
            hugeGraphClient.createPropertyKeyIfNotExists(vectorConfig.getMetadataPropertyName(), String.class); // Store metadata as string

            // 创建顶点标签
            hugeGraphClient.createVertexLabelIfNotExists(vectorConfig.getVertexLabel(),
                    vectorConfig.getIdPropertyName(),
                    vectorConfig.getContentPropertyName(),
                    vectorConfig.getVectorPropertyName(),
                    vectorConfig.getMetadataPropertyName());

            // 创建索引
            if (hugeGraphParam.getInitParam().getCreateIndexOnInit()) {
                hugeGraphClient.createIndexIfNotExists("idx_id", "vertex", vectorConfig.getVertexLabel(), vectorConfig.getIdPropertyName());
                // HugeGraph may not support vector index directly via schema API, it might be configured on backend
            }

            log.info("Schema and indexes initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize schema", e);
            throw HugeGraphVectorStoreException.graphOperationFailed("Failed to initialize schema", e);
        }
    }

    /**
     * 添加文档
     */
    public void addDocuments(List<Document> documents, Embeddings embedding) {
        documents = embedding.embedDocument(documents);
        HugeGraphParam.BatchConfig batchConfig = hugeGraphParam.getBatchConfig();

        if (batchConfig.isEnableBatchInsert()) {
            List<List<Document>> partitions = new ArrayList<>();
            for (int i = 0; i < documents.size(); i += batchConfig.getBatchInsertSize()) {
                partitions.add(documents.subList(i, Math.min(i + batchConfig.getBatchInsertSize(), documents.size())));
            }
            for (List<Document> partition : partitions) {
                addDocumentsInBatch(partition);
            }
        } else {
            for (Document document : documents) {
                addSingleDocument(document);
            }
        }
    }

    private void addDocumentsInBatch(List<Document> documents) {
        // 构建批量插入的Gremlin查询，使用参数绑定避免注入
        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
        StringBuilder gremlin = new StringBuilder("g");
        Map<String, Object> bindings = new HashMap<>();
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String idParam = "id" + i;
            String contentParam = "content" + i;
            String vectorParam = "vector" + i; 
            String metadataParam = "metadata" + i;
            
            gremlin.append(String.format(".addV('%s').property('%s', %s).property('%s', %s).property('%s', %s).property('%s', %s)",
                    vectorConfig.getVertexLabel(),
                    vectorConfig.getIdPropertyName(), idParam,
                    vectorConfig.getContentPropertyName(), contentParam,
                    vectorConfig.getVectorPropertyName(), vectorParam,
                    vectorConfig.getMetadataPropertyName(), metadataParam));
            
            bindings.put(idParam, doc.getUniqueId());
            bindings.put(contentParam, doc.getPageContent());
            bindings.put(vectorParam, JSON.toJSONString(doc.getEmbedding()));
            bindings.put(metadataParam, JSON.toJSONString(doc.getMetadata()));
        }
        
        try {
            hugeGraphClient.executeGremlinWithBindings(gremlin.toString(), bindings);
        } catch (Exception e) {
            throw HugeGraphVectorStoreException.vectorInsertFailed("Batch document insertion failed", e);
        }
    }

    private void addSingleDocument(Document document) {
        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
        String gremlin = String.format("g.addV('%s').property('%s', idParam).property('%s', contentParam).property('%s', vectorParam).property('%s', metadataParam)",
                vectorConfig.getVertexLabel(),
                vectorConfig.getIdPropertyName(),
                vectorConfig.getContentPropertyName(),
                vectorConfig.getVectorPropertyName(),
                vectorConfig.getMetadataPropertyName());
        
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("idParam", document.getUniqueId());
        bindings.put("contentParam", document.getPageContent());
        bindings.put("vectorParam", JSON.toJSONString(document.getEmbedding()));
        bindings.put("metadataParam", JSON.toJSONString(document.getMetadata()));
        
        try {
            hugeGraphClient.executeGremlinWithBindings(gremlin, bindings);
        } catch (Exception e) {
            throw HugeGraphVectorStoreException.vectorInsertFailed("Single document insertion failed for docId: " + document.getUniqueId(), e);
        }
    }

    private String buildAddVertexGremlin(Document document) {
        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
        
        // 使用参数化查询避免Gremlin注入
        return String.format(".addV('%s').property('%s', idParam).property('%s', contentParam).property('%s', vectorParam).property('%s', metadataParam)",
                vectorConfig.getVertexLabel(),
                vectorConfig.getIdPropertyName(),
                vectorConfig.getContentPropertyName(),
                vectorConfig.getVectorPropertyName(),
                vectorConfig.getMetadataPropertyName());
    }

    /**
     * 相似度搜索
     */
    public List<Document> similaritySearch(String query, Embeddings embedding, int k, Double maxDistanceValue, Integer type) {
        List<String> embeddingStrings = embedding.embedQuery(query, 1);
        if (embeddingStrings.isEmpty()) {
            return new ArrayList<>();
        }
        List<Double> queryVector = JSON.parseArray(embeddingStrings.get(0), Double.class);

        // 尝试使用向量索引搜索，如果不可用则回退到内存搜索
        if (hugeGraphParam.getPerformanceConfig().isEnableVectorIndex()) {
            try {
                return performVectorIndexSearch(queryVector, k, maxDistanceValue);
            } catch (Exception e) {
                log.warn("Vector index search failed, falling back to memory search: {}", e.getMessage());
            }
        }
        
        // 改进的内存搜索，包含分页和早期终止优化
        return performOptimizedMemoryVectorSearch(queryVector, k, maxDistanceValue);
    }

    /**
     * 使用向量索引进行搜索（需要HugeGraph后端支持）
     * 这是生产环境推荐的方式，性能更好
     */
    private List<Document> performVectorIndexSearch(List<Double> queryVector, int k, Double maxDistanceValue) {
        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
        
        // 构建向量相似度查询的Gremlin脚本
        // 注意：这需要HugeGraph后端（通常是Elasticsearch）支持向量相似度搜索
        String vectorQueryScript = buildVectorSimilarityQuery(vectorConfig, queryVector, k, maxDistanceValue);
        
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("queryVectorParam", JSON.toJSONString(queryVector));
        bindings.put("topKParam", k);
        if (maxDistanceValue != null) {
            bindings.put("maxDistanceParam", maxDistanceValue);
        }
        
        ResultSet resultSet = hugeGraphClient.executeGremlinWithBindings(vectorQueryScript, bindings);
        
        List<Document> results = new ArrayList<>();
        resultSet.iterator().forEachRemaining(result -> {
            Vertex vertex = (Vertex) result.getObject();
            Document doc = convertVertexToDocument(vertex, vectorConfig);
            results.add(doc);
        });
        
        return results;
    }

    /**
     * 构建向量相似度查询的Gremlin脚本
     * 这个实现假设HugeGraph后端支持自定义向量相似度计算
     */
    private String buildVectorSimilarityQuery(HugeGraphParam.VectorConfig vectorConfig, List<Double> queryVector, int k, Double maxDistanceValue) {
        StringBuilder query = new StringBuilder();
        query.append("g.V().hasLabel('").append(vectorConfig.getVertexLabel()).append("')");
        
        // 这里需要根据具体的HugeGraph后端向量索引实现来调整
        // 以下是一个示例，实际实现可能需要自定义Gremlin步骤或使用全文索引
        query.append(".map{vertex -> ");
        query.append("  def storedVector = vertex.get().value('").append(vectorConfig.getVectorPropertyName()).append("'); ");
        query.append("  def parsedVector = groovy.json.JsonSlurper().parseText(storedVector); ");
        query.append("  def queryVec = groovy.json.JsonSlurper().parseText(queryVectorParam); ");
        query.append("  def similarity = cosineSimilarity(parsedVector, queryVec); ");
        query.append("  vertex.get().property('_similarity', similarity); ");
        query.append("  return vertex.get(); ");
        query.append("}");
        
        if (maxDistanceValue != null) {
            query.append(".filter{vertex -> vertex.get().value('_similarity') >= ").append(1.0 - maxDistanceValue).append("}");
        }
        
        query.append(".order().by('_similarity', desc).limit(topKParam)");
        
        return query.toString();
    }

    /**
     * 改进的内存向量搜索，包含性能优化
     */
    private List<Document> performOptimizedMemoryVectorSearch(List<Double> queryVector, int k, Double maxDistanceValue) {
        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
        HugeGraphParam.PerformanceConfig perfConfig = hugeGraphParam.getPerformanceConfig();
        
        // 使用分页查询减少内存消耗，避免一次性加载所有数据
        int batchSize = perfConfig.getBatchQuerySize();
        List<Document> topKResults = new ArrayList<>();
        double currentWorstDistance = Double.MAX_VALUE;
        int offset = 0;
        boolean hasMore = true;
        
        while (hasMore && topKResults.size() < k) {
            // 构建分页查询
            String gremlin = String.format("g.V().hasLabel('%s').range(%d, %d)", 
                    vectorConfig.getVertexLabel(), offset, offset + batchSize);
            
            ResultSet resultSet = hugeGraphClient.executeGremlin(gremlin);
            List<Document> batchResults = new ArrayList<>();
            
            resultSet.iterator().forEachRemaining(result -> {
                try {
                    Vertex vertex = (Vertex) result.getObject();
                    Document doc = convertVertexToDocument(vertex, vectorConfig);
                    if (doc != null && doc.getEmbedding() != null) {
                        // 计算相似度距离
                        double distance = HugeGraphSimilarityFunction.COSINE.calculateDistance(queryVector, doc.getEmbedding());
                        doc.getMetadata().put("distance", distance);
                        
                        // 早期过滤：只保留满足距离阈值的文档
                        if (maxDistanceValue == null || distance <= maxDistanceValue) {
                            batchResults.add(doc);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to process vertex in vector search: {}", e.getMessage());
                }
            });
            
            // 合并当前批次结果
            topKResults.addAll(batchResults);
            
            // 排序并保持topK
            topKResults.sort((d1, d2) -> Double.compare(
                (Double) d1.getMetadata().get("distance"), 
                (Double) d2.getMetadata().get("distance")
            ));
            
            // 只保留前k个结果
            if (topKResults.size() > k) {
                topKResults = new ArrayList<>(topKResults.subList(0, k));
            }
            
            // 更新当前最差距离用于剪枝
            if (!topKResults.isEmpty()) {
                currentWorstDistance = (Double) topKResults.get(topKResults.size() - 1).getMetadata().get("distance");
            }
            
            offset += batchSize;
            hasMore = batchResults.size() == batchSize; // 如果返回的结果少于批次大小，说明没有更多数据
            
            // 性能优化：如果已经找到足够的结果且当前最差距离很小，可以提前终止
            if (topKResults.size() >= k && maxDistanceValue != null && currentWorstDistance < maxDistanceValue * 0.5) {
                log.debug("Early termination: found {} results with good similarity scores", topKResults.size());
                break;
            }
        }
        
        return topKResults;
    }

    /**
     * 将顶点转换为文档对象
     */
    private Document convertVertexToDocument(Vertex vertex, HugeGraphParam.VectorConfig vectorConfig) {
        try {
            Document doc = new Document();
            doc.setUniqueId(getPropertyValue(vertex, vectorConfig.getIdPropertyName()));
            doc.setPageContent(getPropertyValue(vertex, vectorConfig.getContentPropertyName()));
            
            String vectorStr = getPropertyValue(vertex, vectorConfig.getVectorPropertyName());
            if (StringUtils.isNotEmpty(vectorStr)) {
                doc.setEmbedding(JSON.parseArray(vectorStr, Double.class));
            }
            
            String metadataStr = getPropertyValue(vertex, vectorConfig.getMetadataPropertyName());
            if (StringUtils.isNotEmpty(metadataStr)) {
                doc.setMetadata(JSON.parseObject(metadataStr, Map.class));
            } else {
                doc.setMetadata(new HashMap<>());
            }
            
            return doc;
        } catch (Exception e) {
            log.error("Failed to convert vertex to document: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从顶点获取属性值（改进的错误处理）
     */
    private String getPropertyValue(Vertex vertex, String propertyName) {
        try {
            // 首先检查属性是否存在
            if (!vertex.keys().contains(propertyName)) {
                log.debug("Property '{}' not found in vertex '{}'", propertyName, vertex.id());
                return "";
            }
            
            Object propertyValue = vertex.property(propertyName);
            if (propertyValue instanceof VertexProperty) {
                VertexProperty vertexProperty = (VertexProperty) propertyValue;
                if (vertexProperty.isPresent()) {
                    return vertexProperty.value().toString();
                } else {
                    log.debug("Property '{}' exists but has no value in vertex '{}'", propertyName, vertex.id());
                    return "";
                }
            } else {
                return propertyValue != null ? propertyValue.toString() : "";
            }
        } catch (Exception e) {
            log.warn("Failed to get property '{}' from vertex '{}': {}", propertyName, vertex.id(), e.getMessage());
            return "";
        }
    }

    /**
     * 转义字符串中的特殊字符，用于Gremlin查询
     */
    private String escapeString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    @Override
    public void close() {
        if (hugeGraphClient != null) {
            hugeGraphClient.close();
        }
    }
}
