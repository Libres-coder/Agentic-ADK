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
package com.alibaba.langengine.tuargpg.vectorstore;

import com.alibaba.langengine.tuargpg.vectorstore.service.TuargpgEmbeddingsRequest;
import com.alibaba.langengine.tuargpg.vectorstore.service.TuargpgQueryRequest;
import com.alibaba.langengine.tuargpg.vectorstore.service.TuargpgQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TuargpgClient {

    private final TuargpgVectorStoreParam param;
    private Connection connection;

    public TuargpgClient(TuargpgVectorStoreParam param) {
        this.param = param;
        validateParams();
        initConnection();
    }

    private void validateParams() {
        if (StringUtils.isBlank(param.getServerUrl())) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.INVALID_PARAMETER,
                "Server URL is required"
            );
        }
        if (StringUtils.isBlank(param.getDatabase())) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.INVALID_PARAMETER,
                "Database name is required"
            );
        }
        if (StringUtils.isBlank(param.getTableName())) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.INVALID_PARAMETER,
                "Table name is required"
            );
        }
    }

    private void initConnection() {
        try {
            String jdbcUrl = buildJdbcUrl();
            connection = DriverManager.getConnection(jdbcUrl, param.getUsername(), param.getPassword());
            connection.setAutoCommit(true);
            log.info("Successfully connected to Tuargpg database: {}", param.getDatabase());
        } catch (SQLException e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.CONNECTION_FAILED,
                "Failed to connect to Tuargpg database",
                e
            );
        }
    }

    private String buildJdbcUrl() {
        return String.format("jdbc:postgresql://%s/%s", param.getServerUrl(), param.getDatabase());
    }

    public void createTableIfNotExists() {
        String sql = String.format(
            "CREATE TABLE IF NOT EXISTS %s.%s (" +
            "id VARCHAR(255) PRIMARY KEY, " +
            "content TEXT NOT NULL, " +
            "vector float8[] NOT NULL, " +
            "metadata JSONB " +
            ")",
            param.getSchema(),
            param.getTableName()
        );

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            log.info("Table {}.{} created or already exists", param.getSchema(), param.getTableName());
        } catch (SQLException e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.QUERY_EXECUTION_FAILED,
                "Failed to create table",
                e
            );
        }
    }

    public void addEmbeddings(TuargpgEmbeddingsRequest request) {
        String sql = String.format(
            "INSERT INTO %s.%s (id, content, vector, metadata) VALUES (?, ?, ?, ?::jsonb) " +
            "ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content, vector = EXCLUDED.vector, metadata = EXCLUDED.metadata",
            param.getSchema(),
            param.getTableName()
        );

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (TuargpgEmbeddingsRequest.TuargpgEmbeddingRecord record : request.getRecords()) {
                pstmt.setString(1, record.getId());
                pstmt.setString(2, record.getContent());

                Array vectorArray = connection.createArrayOf("float8", record.getVector().toArray());
                pstmt.setArray(3, vectorArray);

                String metadataJson = convertMetadataToJson(record.getMetadata());
                pstmt.setString(4, metadataJson);

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            log.info("Successfully added {} embeddings", request.getRecords().size());
        } catch (SQLException e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.QUERY_EXECUTION_FAILED,
                "Failed to add embeddings",
                e
            );
        }
    }

    public TuargpgQueryResponse queryByVector(TuargpgQueryRequest request) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, content, vector, metadata, ");

        if ("cosine".equals(param.getDistanceFunction())) {
            sql.append("1 - (vector <=> ?) AS distance ");
        } else if ("euclidean".equals(param.getDistanceFunction())) {
            sql.append("vector <-> ? AS distance ");
        } else {
            sql.append("vector <#> ? AS distance ");
        }

        sql.append(String.format("FROM %s.%s ", param.getSchema(), param.getTableName()));

        if (StringUtils.isNotBlank(request.getWhereClause())) {
            sql.append("WHERE ").append(request.getWhereClause()).append(" ");
        }

        sql.append("ORDER BY distance ASC ");
        sql.append("LIMIT ?");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            Array vectorArray = connection.createArrayOf("float8", request.getQueryVector().toArray());
            pstmt.setArray(1, vectorArray);
            pstmt.setInt(2, request.getTopK());

            try (ResultSet rs = pstmt.executeQuery()) {
                return buildQueryResponse(rs);
            }
        } catch (SQLException e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.QUERY_EXECUTION_FAILED,
                "Failed to execute vector query",
                e
            );
        }
    }

    public void deleteById(String id) {
        String sql = String.format("DELETE FROM %s.%s WHERE id = ?", param.getSchema(), param.getTableName());

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            log.info("Deleted {} record(s) with id: {}", affectedRows, id);
        } catch (SQLException e) {
            throw new TuargpgVectorStoreException(
                TuargpgVectorStoreException.ErrorCodes.QUERY_EXECUTION_FAILED,
                "Failed to delete record",
                e
            );
        }
    }

    private TuargpgQueryResponse buildQueryResponse(ResultSet rs) throws SQLException {
        List<TuargpgQueryResponse.TuargpgVectorRecord> records = new ArrayList<>();

        while (rs.next()) {
            TuargpgQueryResponse.TuargpgVectorRecord record = new TuargpgQueryResponse.TuargpgVectorRecord();
            record.setId(rs.getString("id"));
            record.setContent(rs.getString("content"));
            record.setDistance(rs.getDouble("distance"));
            record.setScore(1.0 - rs.getDouble("distance"));

            Array vectorArray = rs.getArray("vector");
            if (vectorArray != null) {
                Double[] vectorDoubles = (Double[]) vectorArray.getArray();
                List<Float> vector = new ArrayList<>();
                for (Double d : vectorDoubles) {
                    vector.add(d.floatValue());
                }
                record.setVector(vector);
            }

            String metadataJson = rs.getString("metadata");
            if (metadataJson != null) {
                record.setMetadata(parseMetadataFromJson(metadataJson));
            }

            records.add(record);
        }

        TuargpgQueryResponse response = new TuargpgQueryResponse();
        response.setRecords(records);
        response.setTotalCount(records.size());

        return response;
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue().toString().replace("\"", "\\\"")).append("\"");
            } else {
                json.append(entry.getValue().toString());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private Map<String, Object> parseMetadataFromJson(String json) {
        Map<String, Object> metadata = new HashMap<>();
        if (StringUtils.isBlank(json) || "{}".equals(json.trim())) {
            return metadata;
        }
        return metadata;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                log.info("Tuargpg connection closed");
            } catch (SQLException e) {
                log.warn("Failed to close Tuargpg connection", e);
            }
        }
    }
}