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
package com.alibaba.langengine.hippo.vectorstore;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class HippoClient {

    private final String serverUrl;
    private final String username;
    private final String password;
    private Connection connection;

    public HippoClient(String serverUrl, String username, String password) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.connection = createConnection();
    }

    private Connection createConnection() {
        try {
            String jdbcUrl = "jdbc:postgresql://" + serverUrl + "/hippo";
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new HippoException("CONN_001", "Failed to create connection to Hippo", e);
        }
    }

    public void createTable(String tableName, HippoParam param) {
        HippoParam.InitParam initParam = param.getInitParam();
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        
        if (initParam.isFieldUniqueIdAsPrimaryKey()) {
            sql.append(fieldNameUniqueId).append(" BIGINT PRIMARY KEY, ");
        } else {
            sql.append("id SERIAL PRIMARY KEY, ");
            sql.append(fieldNameUniqueId).append(" BIGINT, ");
        }
        
        sql.append(fieldNamePageContent).append(" VARCHAR(").append(initParam.getFieldPageContentMaxLength()).append("), ");
        sql.append(fieldNameEmbedding).append(" VECTOR(").append(initParam.getFieldEmbeddingsDimension()).append(")");
        sql.append(")");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
            log.info("Created table: {}", tableName);
        } catch (SQLException e) {
            throw new HippoException("TABLE_001", "Failed to create table: " + tableName, e);
        }
    }

    public void createIndex(String tableName, HippoParam param) {
        String fieldNameEmbedding = param.getFieldNameEmbedding();
        HippoParam.InitParam initParam = param.getInitParam();
        
        String indexName = tableName + "_" + fieldNameEmbedding + "_idx";
        String sql = String.format("CREATE INDEX IF NOT EXISTS %s ON %s USING %s (%s)",
                indexName, tableName, initParam.getIndexType().toLowerCase(), fieldNameEmbedding);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Created index: {}", indexName);
        } catch (SQLException e) {
            throw new HippoException("INDEX_001", "Failed to create index on table: " + tableName, e);
        }
    }

    public boolean tableExists(String tableName) {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new HippoException("TABLE_002", "Failed to check table existence: " + tableName, e);
        }
    }

    public void insertDocuments(String tableName, List<Map<String, Object>> documents, HippoParam param) {
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();

        String sql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?::vector) ON CONFLICT (%s) DO UPDATE SET %s = EXCLUDED.%s, %s = EXCLUDED.%s",
                tableName, fieldNameUniqueId, fieldNamePageContent, fieldNameEmbedding,
                fieldNameUniqueId, fieldNamePageContent, fieldNamePageContent, fieldNameEmbedding, fieldNameEmbedding);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Map<String, Object> doc : documents) {
                stmt.setLong(1, (Long) doc.get(fieldNameUniqueId));
                stmt.setString(2, (String) doc.get(fieldNamePageContent));
                stmt.setString(3, (String) doc.get(fieldNameEmbedding));
                stmt.addBatch();
            }
            stmt.executeBatch();
            log.info("Inserted {} documents into table: {}", documents.size(), tableName);
        } catch (SQLException e) {
            throw new HippoException("INSERT_001", "Failed to insert documents into table: " + tableName, e);
        }
    }

    public List<Map<String, Object>> searchSimilar(String tableName, String embedding, int k, HippoParam param) {
        String fieldNameUniqueId = param.getFieldNameUniqueId();
        String fieldNamePageContent = param.getFieldNamePageContent();
        String fieldNameEmbedding = param.getFieldNameEmbedding();
        HippoParam.InitParam initParam = param.getInitParam();

        String operator = "L2".equals(initParam.getMetricType()) ? "<->" : "<#>";
        String sql = String.format("SELECT %s, %s, %s %s ?::vector AS distance FROM %s ORDER BY distance LIMIT ?",
                fieldNameUniqueId, fieldNamePageContent, fieldNameEmbedding, operator, tableName);

        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, embedding);
            stmt.setInt(2, k);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put(fieldNameUniqueId, rs.getLong(fieldNameUniqueId));
                    result.put(fieldNamePageContent, rs.getString(fieldNamePageContent));
                    result.put("distance", rs.getDouble("distance"));
                    results.add(result);
                }
            }
        } catch (SQLException e) {
            throw new HippoException("SEARCH_001", "Failed to search similar vectors in table: " + tableName, e);
        }
        
        return results;
    }

    public void dropTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Dropped table: {}", tableName);
        } catch (SQLException e) {
            throw new HippoException("TABLE_003", "Failed to drop table: " + tableName, e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("Hippo connection closed");
            } catch (SQLException e) {
                log.error("Failed to close Hippo connection", e);
            }
        }
    }
}