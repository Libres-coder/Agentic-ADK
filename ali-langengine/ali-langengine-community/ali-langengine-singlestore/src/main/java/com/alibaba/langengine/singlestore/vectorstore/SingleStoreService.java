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
package com.alibaba.langengine.singlestore.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
public class SingleStoreService {

    private String database;

    private String tableName;

    private SingleStoreParam singleStoreParam;

    private Connection connection;

    public SingleStoreService(String serverUrl, String database, String username, String password, String tableName, SingleStoreParam singleStoreParam) {
        this.database = database;
        this.tableName = tableName;
        this.singleStoreParam = singleStoreParam;

        try {
            String jdbcUrl = "jdbc:mysql://" + serverUrl + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
            log.info("SingleStoreService connected to database: " + database + ", table: " + tableName);
        } catch (SQLException e) {
            log.error("Failed to connect to SingleStore", e);
            throw new RuntimeException("Failed to connect to SingleStore", e);
        }
    }

    /**
     * 添加文档到SingleStore
     * @param documents 文档列表
     */
    public void addDocuments(List<Document> documents) {
        SingleStoreParam param = loadParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();

        String sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE %s = VALUES(%s), %s = VALUES(%s)",
                tableName, fieldNameUniqueId, fieldNamePageContent, fieldNameEmbedding,
                fieldNamePageContent, fieldNamePageContent, fieldNameEmbedding, fieldNameEmbedding);

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Document document : documents) {
                pstmt.setLong(1, NumberUtils.toLong(document.getUniqueId()));
                pstmt.setString(2, document.getPageContent());

                // 将向量转换为JSON字符串存储
                List<Float> embeddings = Lists.newArrayList();
                for (Double embedding : document.getEmbedding()) {
                    embeddings.add((float) (double) embedding);
                }
                pstmt.setString(3, JSON.toJSONString(embeddings));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            log.info("Successfully added {} documents to SingleStore", documents.size());
        } catch (SQLException e) {
            log.error("Failed to add documents to SingleStore", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    /**
     * 向量检索
     * @param embeddings 向量
     * @param k 检索数量
     * @return 文档列表
     */
    public List<Document> similaritySearch(List<Float> embeddings, int k) {
        SingleStoreParam param = loadParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();

        // SingleStore 向量相似性搜索 SQL
        String embeddingJson = JSON.toJSONString(embeddings);
        String sql = String.format(
                "SELECT %s, %s, DOT_PRODUCT(JSON_ARRAY_PACK('[%s]'), JSON_ARRAY_PACK(%s)) as similarity_score " +
                "FROM %s ORDER BY similarity_score DESC LIMIT ?",
                fieldNameUniqueId, fieldNamePageContent, embeddingJson.substring(1, embeddingJson.length()-1), fieldNameEmbedding, tableName);

        List<Document> documents = Lists.newArrayList();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, k);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Document document = new Document();
                    document.setUniqueId(String.valueOf(rs.getLong(fieldNameUniqueId)));
                    document.setPageContent(rs.getString(fieldNamePageContent));
                    document.setScore(rs.getDouble("similarity_score"));
                    documents.add(document);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to perform similarity search", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }

        return documents;
    }

    /**
     * 初始化表结构
     */
    public void init(Embeddings embedding) {
        try {
            // 如果没有表, 则创建表
            if (!hasTable()) {
                // 创建表
                createTable(embedding);
            }
        } catch (Exception e) {
            log.error("init SingleStore failed", e);
        }
    }

    /**
     * 加载指定参数 (指定参数不存在使用默认参数)
     * @return SingleStoreParam
     */
    private SingleStoreParam loadParam() {
        if (singleStoreParam == null) {
            singleStoreParam = new SingleStoreParam();
        }
        return singleStoreParam;
    }

    /**
     * 创建表
     * @param embedding 参考embedding模型
     */
    public void createTable(Embeddings embedding) {
        SingleStoreParam param = loadParam();
        SingleStoreParam.InitParam initParam = param.getInitParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();
        int embeddingsDimension = initParam.getFieldEmbeddingsDimension();

        if (initParam.getFieldEmbeddingsDimension() <= 0) {
            //使用embedding进行embedding确认向量的维度数
            List<Document> embeddingDocuments = embedding.embedTexts(Lists.newArrayList("test"));
            Document document = embeddingDocuments.get(0);
            embeddingsDimension = document.getEmbedding().size();
        }

        String createTableSQL;
        if (initParam.isFieldUniqueIdAsPrimaryKey()) {
            createTableSQL = String.format(
                    "CREATE TABLE %s (" +
                    "%s BIGINT PRIMARY KEY, " +
                    "%s TEXT, " +
                    "%s JSON, " +
                    "VECTOR INDEX (%s) " +
                    ") ENGINE=rowstore",
                    tableName, fieldNameUniqueId, fieldNamePageContent, fieldNameEmbedding, fieldNameEmbedding);
        } else {
            createTableSQL = String.format(
                    "CREATE TABLE %s (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "%s BIGINT, " +
                    "%s TEXT, " +
                    "%s JSON, " +
                    "VECTOR INDEX (%s) " +
                    ") ENGINE=rowstore",
                    tableName, fieldNameUniqueId, fieldNamePageContent, fieldNameEmbedding, fieldNameEmbedding);
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            log.info("Successfully created table: " + tableName);
        } catch (SQLException e) {
            log.error("Failed to create table", e);
            throw new RuntimeException("Failed to create table", e);
        }
    }

    public boolean hasTable() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables(database, null, tableName, new String[]{"TABLE"})) {
                boolean exists = rs.next();
                log.info("Table {} exists: {}", tableName, exists);
                return exists;
            }
        } catch (SQLException e) {
            log.error("Failed to check if table exists", e);
            return false;
        }
    }

    protected void dropTable() {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            log.info("Successfully dropped table: " + tableName);
        } catch (SQLException e) {
            log.error("Failed to drop table", e);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("SingleStore connection closed");
            } catch (SQLException e) {
                log.error("Failed to close connection", e);
            }
        }
    }
}