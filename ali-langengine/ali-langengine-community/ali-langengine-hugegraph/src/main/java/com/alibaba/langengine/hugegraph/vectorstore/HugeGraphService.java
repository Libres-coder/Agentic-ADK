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
        StringBuilder gremlin = new StringBuilder("g");
        for (Document doc : documents) {
            gremlin.append(buildAddVertexGremlin(doc));
        }
        try {
            hugeGraphClient.executeGremlin(gremlin.toString());
        } catch (Exception e) {
            throw HugeGraphVectorStoreException.vectorInsertFailed("Batch document insertion failed", e);
        }
    }

    private void addSingleDocument(Document document) {
        String gremlin = buildAddVertexGremlin(document);
        try {
            hugeGraphClient.executeGremlin("g" + gremlin);
        } catch (Exception e) {
            throw HugeGraphVectorStoreException.vectorInsertFailed("Single document insertion failed for docId: " + document.getUniqueId(), e);
        }
    }

    private String buildAddVertexGremlin(Document document) {
        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
        String vectorJson = JSON.toJSONString(document.getEmbedding());
        String metadataJson = JSON.toJSONString(document.getMetadata());

        return String.format(".addV('%s').property('%s', '%s').property('%s', '%s').property('%s', '%s').property('%s', '%s')",
                vectorConfig.getVertexLabel(),
                vectorConfig.getIdPropertyName(), document.getUniqueId(),
                vectorConfig.getContentPropertyName(), escapeString(document.getPageContent()),
                vectorConfig.getVectorPropertyName(), escapeString(vectorJson),
                vectorConfig.getMetadataPropertyName(), escapeString(metadataJson));
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

        // This is a simplified in-memory search. For production, a dedicated vector search index in HugeGraph is needed.
        return performOptimizedMemoryVectorSearch(queryVector, k, maxDistanceValue);
    }

    private List<Document> performOptimizedMemoryVectorSearch(List<Double> queryVector, int k, Double maxDistanceValue) {
        HugeGraphParam.VectorConfig vectorConfig = hugeGraphParam.getVectorConfig();
        String gremlin = String.format("g.V().hasLabel('%s')", vectorConfig.getVertexLabel());
        ResultSet resultSet = hugeGraphClient.executeGremlin(gremlin);

        List<Document> allDocs = new ArrayList<>();
        resultSet.iterator().forEachRemaining(result -> {
            Vertex vertex = (Vertex) result.getObject();
            Document doc = new Document();
            doc.setUniqueId(getPropertyValue(vertex, vectorConfig.getIdPropertyName()));
            doc.setPageContent(getPropertyValue(vertex, vectorConfig.getContentPropertyName()));
            String vectorStr = getPropertyValue(vertex, vectorConfig.getVectorPropertyName());
            doc.setEmbedding(JSON.parseArray(vectorStr, Double.class));
            String metadataStr = getPropertyValue(vertex, vectorConfig.getMetadataPropertyName());
            doc.setMetadata(JSON.parseObject(metadataStr, Map.class));
            allDocs.add(doc);
        });

        return allDocs.stream()
                .map(doc -> {
                    double distance = HugeGraphSimilarityFunction.COSINE.calculateDistance(queryVector, doc.getEmbedding());
                    doc.getMetadata().put("distance", distance);
                    return doc;
                })
                .filter(doc -> maxDistanceValue == null || (Double) doc.getMetadata().get("distance") <= maxDistanceValue)
                .sorted((d1, d2) -> Double.compare((Double) d1.getMetadata().get("distance"), (Double) d2.getMetadata().get("distance")))
                .limit(k)
                .collect(Collectors.toList());
    }

    /**
     * 从顶点获取属性值
     */
    private String getPropertyValue(Vertex vertex, String propertyName) {
        try {
            Object propertyValue = vertex.property(propertyName);
            if (propertyValue instanceof VertexProperty) {
                return ((VertexProperty) propertyValue).value().toString();
            } else {
                return propertyValue != null ? propertyValue.toString() : "";
            }
        } catch (Exception e) {
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
