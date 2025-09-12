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
package com.alibaba.langengine.neo4j.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Data
public class Neo4jService implements AutoCloseable {

    private Driver driver;
    private String database;
    private Neo4jParam neo4jParam;

    public Neo4jService(String uri, String username, String password, String database, Neo4jParam neo4jParam) {
        this.database = StringUtils.defaultIfEmpty(database, "neo4j");
        this.neo4jParam = neo4jParam != null ? neo4jParam : new Neo4jParam();
        
        try {
            Config.ConfigBuilder configBuilder = Config.builder()
                    .withConnectionTimeout(this.neo4jParam.getInitParam().getConnectionTimeoutSeconds(), TimeUnit.SECONDS)
                    .withMaxConnectionPoolSize(this.neo4jParam.getInitParam().getMaxConnectionPoolSize());

            this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password), configBuilder.build());
            
            // Test connection
            verifyConnectivity();
            
            log.info("Neo4j Service initialized successfully. URI: {}, Database: {}", uri, this.database);
        } catch (Exception e) {
            log.error("Failed to initialize Neo4j Service", e);
            throw new RuntimeException("Failed to connect to Neo4j", e);
        }
    }

    /**
     * 验证连接
     */
    public void verifyConnectivity() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            session.run("RETURN 1").consume();
            log.info("Neo4j connectivity verified successfully");
        } catch (Exception e) {
            log.error("Neo4j connectivity verification failed", e);
            throw new RuntimeException("Neo4j connection verification failed", e);
        }
    }

    /**
     * 初始化向量索引
     */
    public void init(Embeddings embedding) {
        try {
            if (neo4jParam.getInitParam().isAutoCreateIndex()) {
                createVectorIndex(embedding);
            }
            log.info("Neo4j vector store initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Neo4j vector store", e);
            throw new RuntimeException("Failed to initialize Neo4j vector store", e);
        }
    }

    /**
     * 创建向量索引
     */
    private void createVectorIndex(Embeddings embedding) {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String nodeLabel = neo4jParam.getNodeLabel();
            String vectorIndexName = neo4jParam.getVectorIndexName();
            String embeddingField = neo4jParam.getFieldNameEmbedding();

            Neo4jParam.InitParam initParam = neo4jParam.getInitParam();

            // 检查索引是否已存在
            String checkIndexQuery = "SHOW INDEXES YIELD name WHERE name = $indexName RETURN count(*) as count";
            Result result = session.run(checkIndexQuery, Values.parameters("indexName", vectorIndexName));

            if (result.single().get("count").asInt() > 0) {
                log.info("Vector index {} already exists", vectorIndexName);
                return;
            }

            // 推断向量维度（如果未正确配置）
            int vectorDimensions = initParam.getVectorDimensions();
            if (vectorDimensions <= 0) {
                log.warn("Invalid vector dimensions: {}, attempting to infer from embedding model", vectorDimensions);
                try {
                    List<String> testEmbedding = embedding.embedQuery("test", 1);
                    if (!testEmbedding.isEmpty() && testEmbedding.get(0).startsWith("[")) {
                        List<Float> testVector = com.alibaba.fastjson.JSON.parseArray(testEmbedding.get(0), Float.class);
                        vectorDimensions = testVector.size();
                        initParam.setVectorDimensions(vectorDimensions);
                        log.info("Inferred vector dimensions from embedding model: {}", vectorDimensions);
                    }
                } catch (Exception e) {
                    log.warn("Failed to infer vector dimensions from embedding model, using default: 1536", e);
                    vectorDimensions = 1536;
                    initParam.setVectorDimensions(vectorDimensions);
                }
            }

            // 创建向量索引
            String createIndexQuery = String.format(
                "CREATE VECTOR INDEX %s FOR (n:%s) ON (n.%s) " +
                "OPTIONS {indexConfig: {" +
                "`vector.dimensions`: %d, " +
                "`vector.similarity_function`: '%s'" +
                "}}",
                vectorIndexName, nodeLabel, embeddingField,
                vectorDimensions, initParam.getSimilarityFunction().getValue()
            );

            session.run(createIndexQuery);
            log.info("Created vector index: {} with dimensions: {}, similarity: {}",
                vectorIndexName, vectorDimensions, initParam.getSimilarityFunction().getValue());

        } catch (Exception e) {
            log.error("Failed to create vector index", e);
            throw new RuntimeException("Failed to create vector index", e);
        }
    }

    /**
     * 添加文档
     */
    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String nodeLabel = neo4jParam.getNodeLabel();
            String idField = neo4jParam.getFieldNameUniqueId();
            String contentField = neo4jParam.getFieldNamePageContent();
            String embeddingField = neo4jParam.getFieldNameEmbedding();
            String metadataField = neo4jParam.getFieldNameMetadata();

            int batchSize = neo4jParam.getInitParam().getBatchSize();
            
            for (int i = 0; i < documents.size(); i += batchSize) {
                List<Document> batch = documents.subList(i, Math.min(i + batchSize, documents.size()));
                addDocumentsBatch(session, batch, nodeLabel, idField, contentField, embeddingField, metadataField);
            }
            
            log.info("Successfully added {} documents to Neo4j", documents.size());
        } catch (Exception e) {
            log.error("Failed to add documents to Neo4j", e);
            throw new RuntimeException("Failed to add documents to Neo4j", e);
        }
    }

    /**
     * 批量添加文档
     */
    private void addDocumentsBatch(Session session, List<Document> documents, String nodeLabel, 
                                   String idField, String contentField, String embeddingField, String metadataField) {
        
        String query = String.format(
            "UNWIND $documents AS doc " +
            "MERGE (n:%s {%s: doc.id}) " +
            "SET n.%s = doc.content, " +
            "    n.%s = doc.embedding, " +
            "    n.%s = doc.metadata",
            nodeLabel, idField, contentField, embeddingField, metadataField
        );

        List<Map<String, Object>> docMaps = documents.stream().map(doc -> {
            Map<String, Object> docMap = new HashMap<>();
            docMap.put("id", StringUtils.defaultIfEmpty(doc.getUniqueId(), UUID.randomUUID().toString()));
            docMap.put("content", doc.getPageContent());
            docMap.put("embedding", doc.getEmbedding());
            docMap.put("metadata", doc.getMetadata() != null ? JSON.toJSONString(doc.getMetadata()) : "{}");
            return docMap;
        }).collect(Collectors.toList());

        session.run(query, Values.parameters("documents", docMaps));
    }

    /**
     * 相似性搜索
     */
    public List<Document> similaritySearch(List<Float> queryEmbedding, int k) {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String nodeLabel = neo4jParam.getNodeLabel();
            String vectorIndexName = neo4jParam.getVectorIndexName();
            String idField = neo4jParam.getFieldNameUniqueId();
            String contentField = neo4jParam.getFieldNamePageContent();
            String embeddingField = neo4jParam.getFieldNameEmbedding();
            String metadataField = neo4jParam.getFieldNameMetadata();

            // 将Float转换为Double
            List<Double> queryVector = queryEmbedding.stream()
                    .map(Float::doubleValue)
                    .collect(Collectors.toList());

            String query = String.format(
                "CALL db.index.vector.queryNodes('%s', %d, $queryVector) " +
                "YIELD node, score " +
                "RETURN node.%s as id, node.%s as content, node.%s as embedding, node.%s as metadata, score " +
                "ORDER BY score DESC",
                vectorIndexName, k, idField, contentField, embeddingField, metadataField
            );

            Result result = session.run(query, Values.parameters("queryVector", queryVector));
            
            List<Document> documents = new ArrayList<>();
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Document document = new Document();
                document.setUniqueId(record.get("id").asString());
                document.setPageContent(record.get("content").asString());
                document.setScore(record.get("score").asDouble());
                
                // 解析metadata
                String metadataJson = record.get("metadata").asString();
                if (StringUtils.isNotEmpty(metadataJson) && !metadataJson.equals("{}")) {
                    try {
                        Map<String, Object> metadata = JSON.parseObject(metadataJson, Map.class);
                        document.setMetadata(metadata);
                    } catch (Exception e) {
                        log.warn("Failed to parse metadata for document {}: {}", document.getUniqueId(), e.getMessage());
                    }
                }
                
                documents.add(document);
            }
            
            log.info("Found {} similar documents", documents.size());
            return documents;
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    /**
     * 检查向量索引是否存在
     */
    public boolean vectorIndexExists() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String checkQuery = "SHOW INDEXES YIELD name WHERE name = $indexName RETURN count(*) as count";
            Result result = session.run(checkQuery, Values.parameters("indexName", neo4jParam.getVectorIndexName()));
            return result.single().get("count").asInt() > 0;
        } catch (Exception e) {
            log.error("Failed to check vector index existence", e);
            return false;
        }
    }

    /**
     * 删除向量索引
     */
    public void dropVectorIndex() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String dropQuery = "DROP INDEX " + neo4jParam.getVectorIndexName() + " IF EXISTS";
            session.run(dropQuery);
            log.info("Dropped vector index: {}", neo4jParam.getVectorIndexName());
        } catch (Exception e) {
            log.error("Failed to drop vector index", e);
            throw new RuntimeException("Failed to drop vector index", e);
        }
    }

    /**
     * 清空所有文档
     */
    public void clearDocuments() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String deleteQuery = "MATCH (n:" + neo4jParam.getNodeLabel() + ") DELETE n";
            session.run(deleteQuery);
            log.info("Cleared all documents from Neo4j");
        } catch (Exception e) {
            log.error("Failed to clear documents", e);
            throw new RuntimeException("Failed to clear documents", e);
        }
    }

    /**
     * 获取文档数量
     */
    public long getDocumentCount() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String countQuery = "MATCH (n:" + neo4jParam.getNodeLabel() + ") RETURN count(n) as count";
            Result result = session.run(countQuery);
            return result.single().get("count").asLong();
        } catch (Exception e) {
            log.error("Failed to get document count", e);
            return 0;
        }
    }

    /**
     * 检查连接健康状态
     */
    public boolean isHealthy() {
        try {
            verifyConnectivity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.close();
            log.info("Neo4j driver closed");
        }
    }
}
