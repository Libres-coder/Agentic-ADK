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
package com.alibaba.langengine.timescaledb.client;

import com.alibaba.langengine.timescaledb.TimescaleDBConfiguration;
import com.alibaba.langengine.timescaledb.exception.TimescaleDBException;
import com.alibaba.langengine.timescaledb.model.TimescaleDBQueryRequest;
import com.alibaba.langengine.timescaledb.model.TimescaleDBQueryResponse;
import com.alibaba.langengine.timescaledb.model.TimescaleDBVector;
import com.alibaba.fastjson.JSON;
import com.pgvector.PGvector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
public class TimescaleDBClient implements AutoCloseable {
    
    private final DataSource dataSource;
    private final String tableName;
    private final int vectorDimension;
    private final int batchSize;
    private final int connectionTimeout;
    private final int queryTimeout;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    /**
     * 构造函数 - 使用配置参数
     */
    public TimescaleDBClient(DataSource dataSource, String tableName, int vectorDimension) {
        this(dataSource, tableName, vectorDimension, 
             TimescaleDBConfiguration.DEFAULT_BATCH_SIZE,
             TimescaleDBConfiguration.DEFAULT_CONNECTION_TIMEOUT,
             TimescaleDBConfiguration.DEFAULT_QUERY_TIMEOUT);
    }
    
    /**
     * 构造函数 - 完整参数
     */
    public TimescaleDBClient(DataSource dataSource, String tableName, int vectorDimension, 
                           int batchSize, int connectionTimeout, int queryTimeout) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (vectorDimension <= 0) {
            throw new IllegalArgumentException("Vector dimension must be positive");
        }
        
        // 验证表名安全性（防止SQL注入）
        validateTableName(tableName);
        
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.vectorDimension = vectorDimension;
        this.batchSize = batchSize > 0 ? batchSize : TimescaleDBConfiguration.DEFAULT_BATCH_SIZE;
        this.connectionTimeout = connectionTimeout > 0 ? connectionTimeout : TimescaleDBConfiguration.DEFAULT_CONNECTION_TIMEOUT;
        this.queryTimeout = queryTimeout > 0 ? queryTimeout : TimescaleDBConfiguration.DEFAULT_QUERY_TIMEOUT;
        
        log.info("TimescaleDB client initialized: table={}, dimension={}, batchSize={}", 
                tableName, vectorDimension, batchSize);
    }
    
    /**
     * 初始化数据库表和索引
     */
    public void initialize() {
        if (initialized.get()) {
            return;
        }
        
        try (Connection conn = getConnection()) {
            // 创建TimescaleDB扩展
            createTimescaleExtension(conn);
            
            // 创建超表
            createHypertable(conn);
            
            // 创建向量索引
            createVectorIndex(conn);
            
            initialized.set(true);
            log.info("TimescaleDB table {} initialized successfully", tableName);
            
        } catch (SQLException e) {
            throw TimescaleDBException.sqlExecutionError("Failed to initialize TimescaleDB table", e);
        }
    }
    
    /**
     * 创建TimescaleDB扩展
     */
    private void createTimescaleExtension(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS timescaledb");
            stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.debug("TimescaleDB and vector extensions created or already exist");
        }
    }
    
    /**
     * 创建超表
     */
    private void createHypertable(Connection conn) throws SQLException {
        String createTableSQL = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
            "id TEXT PRIMARY KEY, " +
            "content TEXT, " +
            "vector vector(%d), " +
            "vector_dimension INTEGER, " +
            "metadata JSONB, " +
            "timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
            "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
            "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
            "doc_index INTEGER, " +
            "partition_key TEXT, " +
            "version INTEGER DEFAULT 1, " +
            "status TEXT DEFAULT 'active', " +
            "tags TEXT[] " +
            ")",
            tableName, vectorDimension
        );
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            
            // 转换为超表
            String convertToHypertableSQL = String.format(
                "SELECT create_hypertable('%s', 'timestamp', if_not_exists => TRUE, chunk_time_interval => INTERVAL '%d days')",
                tableName, TimescaleDBConfiguration.DEFAULT_CHUNK_TIME_INTERVAL
            );
            stmt.execute(convertToHypertableSQL);
            
            log.debug("Hypertable {} created successfully", tableName);
        }
    }
    
    /**
     * 创建向量索引
     */
    private void createVectorIndex(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 创建向量相似度索引
            String vectorIndexSQL = String.format(
                "CREATE INDEX IF NOT EXISTS %s_vector_idx ON %s USING ivfflat (vector vector_cosine_ops) WITH (lists = 100)",
                tableName, tableName
            );
            stmt.execute(vectorIndexSQL);
            
            // 创建时间索引
            String timeIndexSQL = String.format(
                "CREATE INDEX IF NOT EXISTS %s_time_idx ON %s (timestamp DESC)",
                tableName, tableName
            );
            stmt.execute(timeIndexSQL);
            
            // 创建分区键索引
            String partitionIndexSQL = String.format(
                "CREATE INDEX IF NOT EXISTS %s_partition_idx ON %s (partition_key)",
                tableName, tableName
            );
            stmt.execute(partitionIndexSQL);
            
            log.debug("Vector and time indexes created for table {}", tableName);
        }
    }
    
    /**
     * 插入单个向量
     */
    public void insertVector(TimescaleDBVector vector) {
        insertVectors(Collections.singletonList(vector));
    }
    
    /**
     * 批量插入向量
     */
    public void insertVectors(List<TimescaleDBVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return;
        }
        
        ensureInitialized();
        
        String insertSQL = String.format(
            "INSERT INTO %s (id, content, vector, vector_dimension, metadata, timestamp, created_at, updated_at, doc_index, partition_key, version, status, tags) " +
            "VALUES (?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "content = EXCLUDED.content, " +
            "vector = EXCLUDED.vector, " +
            "metadata = EXCLUDED.metadata, " +
            "updated_at = NOW(), " +
            "version = %s.version + 1",
            tableName, tableName
        );
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            
            conn.setAutoCommit(false);
            
            for (TimescaleDBVector vector : vectors) {
                if (!vector.isValidVector()) {
                    log.warn("Skipping invalid vector: {}", vector.getId());
                    continue;
                }
                
                vector.setDefaultTimestamp();
                
                pstmt.setString(1, vector.getId());
                pstmt.setString(2, vector.getContent());
                pstmt.setObject(3, new PGvector(vector.getVector()));
                pstmt.setInt(4, vector.getVectorDimension());
                pstmt.setString(5, vector.getMetadata() != null ? JSON.toJSONString(vector.getMetadata()) : "{}");
                pstmt.setTimestamp(6, Timestamp.valueOf(vector.getTimestamp()));
                pstmt.setTimestamp(7, Timestamp.valueOf(vector.getCreatedAt()));
                pstmt.setTimestamp(8, Timestamp.valueOf(vector.getUpdatedAt()));
                pstmt.setInt(9, vector.getDocIndex() != null ? vector.getDocIndex() : 0);
                pstmt.setString(10, vector.getPartitionKey() != null ? vector.getPartitionKey() : vector.buildPartitionKey());
                pstmt.setInt(11, vector.getVersion() != null ? vector.getVersion() : 1);
                pstmt.setString(12, vector.getStatus() != null ? vector.getStatus() : "active");
                pstmt.setArray(13, conn.createArrayOf("TEXT", vector.getTags() != null ? vector.getTags().toArray() : new String[0]));
                
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            conn.commit();
            
            log.info("Successfully inserted {} vectors into TimescaleDB table {}", results.length, tableName);
            
        } catch (SQLException e) {
            throw TimescaleDBException.writeError("Failed to insert vectors", e);
        }
    }
    
    /**
     * 相似性搜索
     */
    public TimescaleDBQueryResponse similaritySearch(TimescaleDBQueryRequest request) {
        if (!request.isValid()) {
            return TimescaleDBQueryResponse.failure("Invalid query request");
        }
        
        ensureInitialized();
        
        long startTime = System.currentTimeMillis();
        
        try (Connection conn = getConnection()) {
            StringBuilder sqlBuilder = new StringBuilder();
            List<Object> parameters = new ArrayList<>();
            
            // 验证并获取安全的相似度操作符
            String safeOperator = validateAndGetSimilarityOperator(request.getSimilarityOperator());
            
            // 构建基础查询
            sqlBuilder.append("SELECT id, content, vector, vector_dimension, metadata, timestamp, created_at, updated_at, ");
            sqlBuilder.append("doc_index, partition_key, version, status, tags, ");
            sqlBuilder.append("vector ").append(safeOperator).append(" ? AS distance ");
            
            if (request.getSimilarityMetric() == TimescaleDBQueryRequest.SimilarityMetric.COSINE) {
                sqlBuilder.append(", 1 - (vector <=> ?) AS score ");
            } else {
                sqlBuilder.append(", vector ").append(safeOperator).append(" ? AS score ");
            }
            
            sqlBuilder.append("FROM ").append(tableName).append(" WHERE 1=1 ");
            
            // 添加查询向量参数
            parameters.add(new PGvector(request.getQueryVector()));
            if (request.getSimilarityMetric() == TimescaleDBQueryRequest.SimilarityMetric.COSINE) {
                parameters.add(new PGvector(request.getQueryVector()));
            }
            
            // 添加过滤条件
            addFilterConditions(sqlBuilder, parameters, request);
            
            // 添加排序
            sqlBuilder.append(request.getOrderBySql());
            
            // 添加限制
            sqlBuilder.append(" LIMIT ?");
            parameters.add(request.getLimit());
            
            if (request.getOffset() > 0) {
                sqlBuilder.append(" OFFSET ?");
                parameters.add(request.getOffset());
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
                // 设置查询超时
                pstmt.setQueryTimeout(queryTimeout / 1000);
                
                // 设置参数
                for (int i = 0; i < parameters.size(); i++) {
                    pstmt.setObject(i + 1, parameters.get(i));
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    List<TimescaleDBVector> results = new ArrayList<>();
                    
                    while (rs.next()) {
                        TimescaleDBVector vector = mapResultSetToVector(rs);
                        results.add(vector);
                    }
                    
                    long executionTime = System.currentTimeMillis() - startTime;
                    
                    return TimescaleDBQueryResponse.builder()
                            .vectors(results)
                            .executionTimeMs(executionTime)
                            .totalCount(results.size())
                            .returnedCount(results.size())
                            .success(true)
                            .similarityMetric(request.getSimilarityMetric())
                            .queryStartTime(LocalDateTime.now().minusNanos(executionTime * 1_000_000))
                            .queryEndTime(LocalDateTime.now())
                            .build();
                }
            }
            
        } catch (SQLException e) {
            log.error("Similarity search failed", e);
            return TimescaleDBQueryResponse.failure("Similarity search failed: " + e.getMessage());
        }
    }
    
    /**
     * 添加过滤条件
     */
    private void addFilterConditions(StringBuilder sqlBuilder, List<Object> parameters, TimescaleDBQueryRequest request) {
        // 时间范围过滤
        if (request.hasTimeFilter()) {
            if (request.getTimeRangeStart() != null) {
                sqlBuilder.append(" AND timestamp >= ?");
                parameters.add(Timestamp.valueOf(request.getTimeRangeStart()));
            }
            if (request.getTimeRangeEnd() != null) {
                sqlBuilder.append(" AND timestamp <= ?");
                parameters.add(Timestamp.valueOf(request.getTimeRangeEnd()));
            }
        }
        
        // 元数据过滤
        if (request.hasMetadataFilter()) {
            for (Map.Entry<String, Object> entry : request.getMetadataFilter().entrySet()) {
                sqlBuilder.append(" AND metadata->>? = ?");
                parameters.add(entry.getKey());
                parameters.add(entry.getValue().toString());
            }
        }
        
        // 标签过滤
        if (request.getTagFilter() != null && !request.getTagFilter().isEmpty()) {
            sqlBuilder.append(" AND tags && ?");
            parameters.add(request.getTagFilter().toArray(new String[0]));
        }
        
        // 状态过滤
        if (StringUtils.isNotBlank(request.getStatusFilter())) {
            sqlBuilder.append(" AND status = ?");
            parameters.add(request.getStatusFilter());
        }
        
        // 分区过滤
        if (request.getPartitionFilter() != null && !request.getPartitionFilter().isEmpty()) {
            sqlBuilder.append(" AND partition_key = ANY(?)");
            parameters.add(request.getPartitionFilter().toArray(new String[0]));
        }
        
        // 相似度阈值过滤
        if (request.getSimilarityThreshold() != null) {
            sqlBuilder.append(" AND score >= ?");
            parameters.add(request.getSimilarityThreshold());
        }
        
        // 距离阈值过滤
        if (request.getDistanceThreshold() != null) {
            sqlBuilder.append(" AND distance <= ?");
            parameters.add(request.getDistanceThreshold());
        }
    }
    
    /**
     * 根据ID删除向量
     */
    public boolean deleteVector(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        
        ensureInitialized();
        
        String deleteSQL = String.format("DELETE FROM %s WHERE id = ?", tableName);
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            
            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            log.info("Deleted {} vectors with id {}", affectedRows, id);
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw TimescaleDBException.sqlExecutionError("Failed to delete vector", e);
        }
    }
    
    /**
     * 批量删除向量
     */
    public int deleteVectors(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        ensureInitialized();
        
        String deleteSQL = String.format("DELETE FROM %s WHERE id = ANY(?)", tableName);
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            
            Array idArray = conn.createArrayOf("TEXT", ids.toArray(new String[0]));
            pstmt.setArray(1, idArray);
            
            int affectedRows = pstmt.executeUpdate();
            log.info("Deleted {} vectors", affectedRows);
            return affectedRows;
            
        } catch (SQLException e) {
            throw TimescaleDBException.sqlExecutionError("Failed to delete vectors", e);
        }
    }
    
    /**
     * 获取向量总数
     */
    public long countVectors() {
        ensureInitialized();
        
        String countSQL = String.format("SELECT COUNT(*) FROM %s", tableName);
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw TimescaleDBException.sqlExecutionError("Failed to count vectors", e);
        }
    }
    
    /**
     * 清空表
     */
    public void clearTable() {
        ensureInitialized();
        
        String truncateSQL = String.format("TRUNCATE TABLE %s", tableName);
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(truncateSQL);
            log.info("Cleared table {}", tableName);
            
        } catch (SQLException e) {
            throw TimescaleDBException.sqlExecutionError("Failed to clear table", e);
        }
    }
    
    /**
     * 连接测试
     */
    public boolean ping() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.warn("Connection test failed", e);
            return false;
        }
    }
    
    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(true);
        return conn;
    }
    
    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (!initialized.get()) {
            initialize();
        }
    }
    
    /**
     * 将ResultSet映射为TimescaleDBVector
     */
    private TimescaleDBVector mapResultSetToVector(ResultSet rs) throws SQLException {
        TimescaleDBVector vector = new TimescaleDBVector();
        
        vector.setId(rs.getString("id"));
        vector.setContent(rs.getString("content"));
        
        // 处理向量数据
        PGvector pgVector = (PGvector) rs.getObject("vector");
        if (pgVector != null) {
            float[] floatArray = pgVector.toArray();
            List<Double> doubleList = new ArrayList<>();
            for (float f : floatArray) {
                doubleList.add((double) f);
            }
            vector.setVector(doubleList);
        }
        
        vector.setVectorDimension(rs.getInt("vector_dimension"));
        
        // 处理元数据
        String metadataJson = rs.getString("metadata");
        if (StringUtils.isNotBlank(metadataJson)) {
            vector.setMetadata(JSON.parseObject(metadataJson, Map.class));
        }
        
        // 处理时间戳
        Timestamp timestamp = rs.getTimestamp("timestamp");
        if (timestamp != null) {
            vector.setTimestamp(timestamp.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            vector.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            vector.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        vector.setDocIndex(rs.getInt("doc_index"));
        vector.setPartitionKey(rs.getString("partition_key"));
        vector.setVersion(rs.getInt("version"));
        vector.setStatus(rs.getString("status"));
        
        // 处理标签数组
        Array tagsArray = rs.getArray("tags");
        if (tagsArray != null) {
            vector.setTags(Arrays.asList((String[]) tagsArray.getArray()));
        }
        
        // 设置查询结果
        vector.setDistance(rs.getDouble("distance"));
        vector.setScore(rs.getDouble("score"));
        
        return vector;
    }
    
    /**
     * 验证表名安全性，防止SQL注入
     */
    private void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new TimescaleDBException("Table name cannot be null or empty");
        }
        
        // 表名只能包含字母、数字、下划线，且必须以字母或下划线开头
        String cleanTableName = tableName.trim();
        if (!cleanTableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new TimescaleDBException("Invalid table name format: " + tableName + 
                ". Table name can only contain letters, numbers, and underscores, and must start with a letter or underscore.");
        }
        
        // 检查表名长度
        if (cleanTableName.length() > 63) {
            throw new TimescaleDBException("Table name too long (max 63 characters): " + tableName);
        }
        
        // 检查是否为PostgreSQL保留字
        String upperTableName = cleanTableName.toUpperCase();
        String[] reservedWords = {
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TRUNCATE",
            "FROM", "WHERE", "JOIN", "ORDER", "GROUP", "HAVING", "UNION", "INDEX",
            "TABLE", "DATABASE", "SCHEMA", "USER", "ROLE", "GRANT", "REVOKE"
        };
        
        for (String reserved : reservedWords) {
            if (upperTableName.equals(reserved)) {
                throw new TimescaleDBException("Table name cannot be a reserved word: " + tableName);
            }
        }
    }
    
    /**
     * 验证并获取安全的相似度操作符
     */
    private String validateAndGetSimilarityOperator(String operator) {
        if (operator == null || operator.trim().isEmpty()) {
            return "<->";  // 默认使用余弦距离
        }
        
        // 只允许预定义的PGVector操作符
        String cleanOperator = operator.trim();
        switch (cleanOperator) {
            case "<->":    // L2距离（欧几里得距离）
            case "<#>":    // 内积距离
            case "<=>":    // 余弦距离
                return cleanOperator;
            default:
                throw new TimescaleDBException("Invalid similarity operator: " + operator + 
                    ". Only <->, <#>, and <=> are supported.");
        }
    }
    
    @Override
    public void close() {
        // DataSource通常由外部管理，这里不需要特殊清理
        log.info("TimescaleDB client closed");
    }
}
