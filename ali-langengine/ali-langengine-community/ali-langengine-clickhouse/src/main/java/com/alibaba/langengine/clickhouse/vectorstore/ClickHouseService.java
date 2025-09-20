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
package com.alibaba.langengine.clickhouse.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class ClickHouseService {

    private final DataSource dataSource;
    private final String database;
    private final ClickHouseParam clickHouseParam;

    public ClickHouseService(String url, String username, String password, String database, ClickHouseParam clickHouseParam) {
        this.database = database;
        this.clickHouseParam = clickHouseParam != null ? clickHouseParam : new ClickHouseParam();
        
        try {
            Properties properties = new Properties();
            properties.setProperty("user", username);
            properties.setProperty("password", password);
            properties.setProperty("database", database);
            properties.setProperty("socket_timeout", String.valueOf(this.clickHouseParam.getConnectionTimeout()));
            properties.setProperty("connection_timeout", String.valueOf(this.clickHouseParam.getQueryTimeout()));
            
            this.dataSource = new ClickHouseDataSource(url, properties);
            
            // 验证连接
            verifyConnection();
            
            log.info("ClickHouse Service initialized successfully with database: {}", database);
        } catch (Exception e) {
            log.error("Failed to initialize ClickHouse Service", e);
            throw new RuntimeException("Failed to initialize ClickHouse Service", e);
        }
    }

    /**
     * 验证数据库连接
     */
    private void verifyConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet rs = statement.executeQuery("SELECT 1");
                if (!rs.next()) {
                    throw new SQLException("Connection verification failed");
                }
            }
        }
    }

    /**
     * 初始化向量存储
     */
    public void init(Embeddings embedding) {
        try {
            createDatabase();
            createTable(embedding);
            if (clickHouseParam.getInitParam().isCreateVectorIndex()) {
                createVectorIndex();
            }
            log.info("ClickHouse vector store initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize ClickHouse vector store", e);
            throw new RuntimeException("Failed to initialize ClickHouse vector store", e);
        }
    }

    /**
     * 创建数据库
     */
    private void createDatabase() throws SQLException {
        String createDbSql = String.format("CREATE DATABASE IF NOT EXISTS %s", database);
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createDbSql);
            log.info("Database {} created or already exists", database);
        }
    }

    /**
     * 创建表
     */
    private void createTable(Embeddings embedding) throws SQLException {
        ClickHouseParam.InitParam initParam = clickHouseParam.getInitParam();
        
        // 推断向量维度
        int vectorDimensions = inferVectorDimensions(embedding, initParam);
        
        String tableName = clickHouseParam.getTableName();
        String uniqueIdField = clickHouseParam.getFieldNameUniqueId();
        String pageContentField = clickHouseParam.getFieldNamePageContent();
        String metadataField = clickHouseParam.getFieldNameMetadata();
        String embeddingField = clickHouseParam.getFieldNameEmbedding();
        String scoreField = clickHouseParam.getFieldNameScore();
        
        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append(String.format("CREATE TABLE IF NOT EXISTS %s.%s (", database, tableName));
        createTableSql.append(String.format("%s String,", uniqueIdField));
        createTableSql.append(String.format("%s String,", pageContentField));
        createTableSql.append(String.format("%s String,", metadataField));
        createTableSql.append(String.format("%s Array(Float32),", embeddingField));
        createTableSql.append(String.format("%s Float64 DEFAULT 0.0", scoreField));
        createTableSql.append(String.format(") ENGINE = %s()", initParam.getEngineType()));
        
        if (StringUtils.isNotEmpty(initParam.getOrderBy())) {
            createTableSql.append(String.format(" ORDER BY %s", initParam.getOrderBy()));
        }
        
        if (StringUtils.isNotEmpty(initParam.getPartitionBy())) {
            createTableSql.append(String.format(" PARTITION BY %s", initParam.getPartitionBy()));
        }
        
        createTableSql.append(String.format(" SETTINGS index_granularity = %d", initParam.getIndexGranularity()));
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSql.toString());
            log.info("Table {}.{} created or already exists", database, tableName);
        }
    }

    /**
     * 推断向量维度
     */
    private int inferVectorDimensions(Embeddings embedding, ClickHouseParam.InitParam initParam) {
        int vectorDimensions = initParam.getVectorDimensions();
        
        if (vectorDimensions <= 0 && embedding != null) {
            log.warn("Invalid vector dimensions: {}, attempting to infer from embedding model", vectorDimensions);
            try {
                List<String> testEmbedding = embedding.embedQuery("test", 1);
                if (!testEmbedding.isEmpty() && testEmbedding.get(0).startsWith("[")) {
                    List<Float> testVector = JSON.parseArray(testEmbedding.get(0), Float.class);
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
        
        return vectorDimensions;
    }

    /**
     * 创建向量索引
     */
    private void createVectorIndex() {
        // ClickHouse目前不直接支持向量索引，但可以创建其他类型的索引来优化查询
        try {
            String tableName = clickHouseParam.getTableName();
            String uniqueIdField = clickHouseParam.getFieldNameUniqueId();
            
            // 创建唯一ID索引
            String createIndexSql = String.format(
                "ALTER TABLE %s.%s ADD INDEX IF NOT EXISTS idx_%s %s TYPE minmax GRANULARITY 1",
                database, tableName, uniqueIdField, uniqueIdField
            );
            
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(createIndexSql);
                log.info("Index created for table {}.{}", database, tableName);
            }
        } catch (Exception e) {
            log.warn("Failed to create index, continuing without index", e);
        }
    }

    /**
     * 添加文档
     */
    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        
        try {
            addDocumentsBatch(documents);
            log.info("Added {} documents to ClickHouse", documents.size());
        } catch (Exception e) {
            log.error("Failed to add documents to ClickHouse", e);
            throw new RuntimeException("Failed to add documents to ClickHouse", e);
        }
    }

    /**
     * 批量添加文档
     */
    private void addDocumentsBatch(List<Document> documents) throws SQLException {
        String tableName = clickHouseParam.getTableName();
        String uniqueIdField = clickHouseParam.getFieldNameUniqueId();
        String pageContentField = clickHouseParam.getFieldNamePageContent();
        String metadataField = clickHouseParam.getFieldNameMetadata();
        String embeddingField = clickHouseParam.getFieldNameEmbedding();
        
        String insertSql = String.format(
            "INSERT INTO %s.%s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
            database, tableName, uniqueIdField, pageContentField, metadataField, embeddingField
        );
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSql)) {
            
            int batchCount = 0;
            for (Document document : documents) {
                // 生成唯一ID
                String uniqueId = StringUtils.isNotEmpty(document.getUniqueId()) 
                    ? document.getUniqueId() 
                    : UUID.randomUUID().toString();
                
                // 处理元数据
                String metadataJson = document.getMetadata() != null 
                    ? JSON.toJSONString(document.getMetadata()) 
                    : "{}";
                
                // 处理向量
                List<Float> embedding = document.getEmbedding() != null 
                    ? document.getEmbedding().stream().map(Double::floatValue).collect(Collectors.toList())
                    : new ArrayList<>();
                
                statement.setString(1, uniqueId);
                statement.setString(2, document.getPageContent());
                statement.setString(3, metadataJson);
                statement.setArray(4, connection.createArrayOf("Float32", embedding.toArray()));
                
                statement.addBatch();
                batchCount++;
                
                if (batchCount >= clickHouseParam.getBatchSize()) {
                    statement.executeBatch();
                    batchCount = 0;
                }
            }
            
            if (batchCount > 0) {
                statement.executeBatch();
            }
        }
    }

    /**
     * 相似性搜索
     */
    public List<Document> similaritySearch(List<Float> queryVector, int k, Double maxDistanceValue, Integer type) {
        if (queryVector == null || queryVector.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return performSimilaritySearch(queryVector, k, maxDistanceValue, type);
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    /**
     * 执行相似性搜索
     */
    private List<Document> performSimilaritySearch(List<Float> queryVector, int k, Double maxDistanceValue, Integer type) throws SQLException {
        String tableName = clickHouseParam.getTableName();
        String uniqueIdField = clickHouseParam.getFieldNameUniqueId();
        String pageContentField = clickHouseParam.getFieldNamePageContent();
        String metadataField = clickHouseParam.getFieldNameMetadata();
        String embeddingField = clickHouseParam.getFieldNameEmbedding();
        String scoreField = clickHouseParam.getFieldNameScore();

        ClickHouseSimilarityFunction similarityFunction = clickHouseParam.getInitParam().getSimilarityFunction();

        // 构建查询向量数组字符串
        String queryVectorStr = "[" + queryVector.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",")) + "]";

        // 构建相似性计算SQL
        String similarityCalc = String.format("%s(%s, %s)",
            similarityFunction.getFunctionName(), embeddingField, queryVectorStr);

        StringBuilder querySql = new StringBuilder();
        querySql.append(String.format("SELECT %s, %s, %s, %s, %s as %s",
            uniqueIdField, pageContentField, metadataField, embeddingField, similarityCalc, scoreField));
        querySql.append(String.format(" FROM %s.%s", database, tableName));
        querySql.append(String.format(" WHERE length(%s) > 0", embeddingField));

        // 添加距离过滤
        if (maxDistanceValue != null) {
            if (similarityFunction.isDistanceFunction()) {
                querySql.append(String.format(" AND %s <= %f", similarityCalc, maxDistanceValue));
            } else {
                querySql.append(String.format(" AND %s >= %f", similarityCalc, maxDistanceValue));
            }
        }

        // 排序和限制
        if (similarityFunction.isDistanceFunction()) {
            querySql.append(String.format(" ORDER BY %s ASC", scoreField));
        } else {
            querySql.append(String.format(" ORDER BY %s DESC", scoreField));
        }
        querySql.append(String.format(" LIMIT %d", k));

        List<Document> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(querySql.toString())) {

            while (rs.next()) {
                Document document = new Document();
                document.setUniqueId(rs.getString(uniqueIdField));
                document.setPageContent(rs.getString(pageContentField));
                document.setScore(rs.getDouble(scoreField));

                // 解析元数据
                String metadataJson = rs.getString(metadataField);
                if (StringUtils.isNotEmpty(metadataJson) && !metadataJson.equals("{}")) {
                    try {
                        Map<String, Object> metadata = JSON.parseObject(metadataJson, Map.class);
                        document.setMetadata(metadata);
                    } catch (Exception e) {
                        log.warn("Failed to parse metadata JSON: {}", metadataJson, e);
                    }
                }

                // 解析向量
                Array embeddingArray = rs.getArray(embeddingField);
                if (embeddingArray != null) {
                    Float[] embeddingFloats = (Float[]) embeddingArray.getArray();
                    List<Double> embedding = Arrays.stream(embeddingFloats)
                        .map(Float::doubleValue)
                        .collect(Collectors.toList());
                    document.setEmbedding(embedding);
                }

                results.add(document);
            }
        }

        return results;
    }

    /**
     * 获取文档数量
     */
    public long getDocumentCount() {
        String tableName = clickHouseParam.getTableName();
        String countSql = String.format("SELECT COUNT(*) FROM %s.%s", database, tableName);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(countSql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            log.error("Failed to get document count", e);
        }

        return 0;
    }

    /**
     * 清空文档
     */
    public void clearDocuments() {
        String tableName = clickHouseParam.getTableName();
        String truncateSql = String.format("TRUNCATE TABLE %s.%s", database, tableName);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(truncateSql);
            log.info("Cleared all documents from table {}.{}", database, tableName);
        } catch (Exception e) {
            log.error("Failed to clear documents", e);
            throw new RuntimeException("Failed to clear documents", e);
        }
    }

    /**
     * 删除表
     */
    public void dropTable() {
        String tableName = clickHouseParam.getTableName();
        String dropSql = String.format("DROP TABLE IF EXISTS %s.%s", database, tableName);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(dropSql);
            log.info("Dropped table {}.{}", database, tableName);
        } catch (Exception e) {
            log.error("Failed to drop table", e);
            throw new RuntimeException("Failed to drop table", e);
        }
    }

    /**
     * 检查健康状态
     */
    public boolean isHealthy() {
        try {
            verifyConnection();
            return true;
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        // ClickHouse DataSource 会自动管理连接池
        log.info("ClickHouse Service closed");
    }
}
