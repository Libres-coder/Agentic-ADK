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
package com.alibaba.langengine.influxdb.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.influxdb.InfluxDBConfiguration;
import com.alibaba.langengine.influxdb.InfluxDBConstants;
import com.alibaba.langengine.influxdb.exception.InfluxDBVectorStoreException;
import com.alibaba.langengine.influxdb.model.InfluxDBQueryRequest;
import com.alibaba.langengine.influxdb.model.InfluxDBQueryResponse;
import com.alibaba.langengine.influxdb.model.InfluxDBVector;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class InfluxDBVectorClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBVectorClient.class);

    private final InfluxDBClient client;
    private final String bucket;
    private final String organization;
    private final WriteApiBlocking writeApi;
    private final QueryApi queryApi;
    private final Map<String, List<Double>> vectorCache;
    private final int maxCacheSize;

    /**
     * 构造函数
     *
     * @param url    InfluxDB服务器URL
     * @param token  访问令牌
     * @param org    组织名称
     * @param bucket 存储桶名称
     */
    public InfluxDBVectorClient(String url, String token, String org, String bucket) {
        this(url, token, org, bucket, 1000);
    }

    /**
     * 构造函数
     *
     * @param url          InfluxDB服务器URL
     * @param token        访问令牌
     * @param org          组织名称
     * @param bucket       存储桶名称
     * @param maxCacheSize 最大缓存大小
     */
    public InfluxDBVectorClient(String url, String token, String org, String bucket, int maxCacheSize) {
        try {
            if (StringUtils.isBlank(url)) {
                throw InfluxDBVectorStoreException.configurationError("InfluxDB URL cannot be null or empty");
            }
            if (StringUtils.isBlank(token)) {
                throw InfluxDBVectorStoreException.configurationError("InfluxDB token cannot be null or empty");
            }
            if (StringUtils.isBlank(org)) {
                throw InfluxDBVectorStoreException.configurationError("InfluxDB org cannot be null or empty");
            }
            if (StringUtils.isBlank(bucket)) {
                throw InfluxDBVectorStoreException.configurationError("InfluxDB bucket cannot be null or empty");
            }

            this.client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            this.bucket = bucket;
            this.organization = org;
            this.writeApi = client.getWriteApiBlocking();
            this.queryApi = client.getQueryApi();
            this.vectorCache = new ConcurrentHashMap<>();
            this.maxCacheSize = maxCacheSize;

            log.info("InfluxDB vector client initialized: url={}, org={}, bucket={}", url, org, bucket);
        } catch (Exception e) {
            throw InfluxDBVectorStoreException.connectionError("Failed to initialize InfluxDB client", e);
        }
    }

    /**
     * 从配置创建客户端
     *
     * @param config InfluxDB配置
     */
    public InfluxDBVectorClient(InfluxDBConfiguration config) {
        this(config.getUrl(), config.getToken(), config.getOrg(), config.getBucket(), config.getCacheSize());
    }

    /**
     * 插入向量数据
     *
     * @param vectors 向量列表
     */
    public void insertVectors(List<InfluxDBVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return;
        }

        try {
            List<Point> points = vectors.stream()
                    .filter(InfluxDBVector::isValid)
                    .map(this::vectorToPoint)
                    .collect(Collectors.toList());

            if (!points.isEmpty()) {
                writeApi.writePoints(points);
                log.debug("Successfully inserted {} vectors into InfluxDB", points.size());

                // 更新缓存
                for (InfluxDBVector vector : vectors) {
                    updateCache(vector.getId(), vector.getVector());
                }
            }
        } catch (Exception e) {
            throw InfluxDBVectorStoreException.writeError("Failed to insert vectors", e);
        }
    }

    /**
     * 插入单个向量数据
     *
     * @param vector 向量数据
     */
    public void insertVector(InfluxDBVector vector) {
        insertVectors(Collections.singletonList(vector));
    }

    /**
     * 查询相似向量
     *
     * @param request 查询请求
     * @return 查询响应
     */
    public InfluxDBQueryResponse querySimilarVectors(InfluxDBQueryRequest request) {
        if (request == null || !request.isValid()) {
            throw new IllegalArgumentException("Invalid query request");
        }

        long startTime = System.currentTimeMillis();
        
        try {
            String fluxQuery = buildFluxQuery(request);
            log.debug("Executing Flux query: {}", fluxQuery);

            List<FluxTable> tables = queryApi.query(fluxQuery, organization);
            List<InfluxDBVector> results = processQueryResults(tables, request);

            // 计算相似度并排序
            results = calculateSimilarities(results, request.getQueryVector(), request.getSimilarityMetric());
            
            // 应用相似度阈值过滤
            if (request.getSimilarityThreshold() != null) {
                results = results.stream()
                        .filter(v -> v.getScore() >= request.getSimilarityThreshold())
                        .collect(Collectors.toList());
            }

            // 限制结果数量
            if (results.size() > request.getLimit()) {
                results = results.subList(0, request.getLimit());
            }

            long queryTime = System.currentTimeMillis() - startTime;

            return InfluxDBQueryResponse.builder()
                    .results(results)
                    .queryTimeMs(queryTime)
                    .totalResults(results.size())
                    .returnedResults(results.size())
                    .hasMore(false)
                    .success(true)
                    .build();

        } catch (Exception e) {
            throw InfluxDBVectorStoreException.queryError("Failed to query similar vectors", e);
        }
    }

    /**
     * 删除向量数据
     *
     * @param docId       文档ID
     * @param measurement 测量名称
     */
    public void deleteVector(String docId, String measurement) {
        try {
            String fluxQuery = String.format(
                    "from(bucket: \"%s\") " +
                    "|> range(start: 0) " +
                    "|> filter(fn: (r) => r._measurement == \"%s\" and r.doc_id == \"%s\") " +
                    "|> delete()",
                    bucket, measurement, docId);

            queryApi.query(fluxQuery, organization);
            vectorCache.remove(docId);
            log.debug("Successfully deleted vector with docId: {}", docId);
        } catch (Exception e) {
            throw InfluxDBVectorStoreException.writeError("Failed to delete vector", e);
        }
    }

    /**
     * 检查连接状态
     *
     * @return 连接是否正常
     */
    public boolean ping() {
        try {
            return client.ping();
        } catch (Exception e) {
            log.error("Failed to ping InfluxDB", e);
            return false;
        }
    }

    /**
     * 获取存储桶信息
     *
     * @return 存储桶名称
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * 获取组织信息
     *
     * @return 组织名称
     */
    public String getOrg() {
        return organization;
    }

    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
            }
            vectorCache.clear();
            log.info("InfluxDB vector client closed");
        } catch (Exception e) {
            log.error("Error closing InfluxDB client", e);
        }
    }

    /**
     * 将向量对象转换为InfluxDB Point
     *
     * @param vector 向量对象
     * @return Point对象
     */
    private Point vectorToPoint(InfluxDBVector vector) {
        Point point = Point.measurement(vector.getMeasurement() != null ? 
                      vector.getMeasurement() : InfluxDBConstants.DEFAULT_MEASUREMENT)
                .time(vector.getTimestamp() != null ? vector.getTimestamp() : Instant.now(), 
                      WritePrecision.NS);

        // 添加标签
        Map<String, String> tags = vector.getDefaultTags();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            point.addTag(entry.getKey(), entry.getValue());
        }

        // 添加字段
        Map<String, Object> fields = vector.getDefaultFields();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                point.addField(entry.getKey(), (String) value);
            } else if (value instanceof Number) {
                point.addField(entry.getKey(), (Number) value);
            } else if (value instanceof Boolean) {
                point.addField(entry.getKey(), (Boolean) value);
            } else {
                point.addField(entry.getKey(), value.toString());
            }
        }

        return point;
    }

    /**
     * 构建Flux查询语句
     *
     * @param request 查询请求
     * @return Flux查询语句
     */
    private String buildFluxQuery(InfluxDBQueryRequest request) {
        StringBuilder query = new StringBuilder();
        
        query.append(String.format("from(bucket: \"%s\")", bucket));
        
        // 时间范围
        if (request.getTimeRangeStart() != null && request.getTimeRangeEnd() != null) {
            query.append(String.format(" |> range(start: %s, stop: %s)", 
                    request.getTimeRangeStart(), request.getTimeRangeEnd()));
        } else {
            query.append(" |> range(start: 0)");
        }
        
        // 测量过滤
        if (StringUtils.isNotBlank(request.getMeasurement())) {
            query.append(String.format(" |> filter(fn: (r) => r._measurement == \"%s\")", 
                    request.getMeasurement()));
        }
        
        // 标签过滤
        if (request.getTagFilters() != null && !request.getTagFilters().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getTagFilters().entrySet()) {
                query.append(String.format(" |> filter(fn: (r) => r.%s == \"%s\")", 
                        entry.getKey(), entry.getValue()));
            }
        }
        
        // 字段过滤
        if (request.getFieldFilters() != null && !request.getFieldFilters().isEmpty()) {
            for (Map.Entry<String, Object> entry : request.getFieldFilters().entrySet()) {
                query.append(String.format(" |> filter(fn: (r) => r._field == \"%s\")", 
                        entry.getKey()));
            }
        }
        
        // 限制结果数量
        query.append(String.format(" |> limit(n: %d)", request.getLimit() * 2)); // 获取更多结果以便计算相似度
        
        return query.toString();
    }

    /**
     * 处理查询结果
     *
     * @param tables  查询结果表
     * @param request 查询请求
     * @return 向量列表
     */
    private List<InfluxDBVector> processQueryResults(List<FluxTable> tables, InfluxDBQueryRequest request) {
        Map<String, InfluxDBVector.InfluxDBVectorBuilder> vectorBuilders = new HashMap<>();
        
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                String docId = (String) record.getValueByKey("doc_id");
                if (docId == null) continue;
                
                InfluxDBVector.InfluxDBVectorBuilder builder = vectorBuilders.computeIfAbsent(docId, 
                        k -> InfluxDBVector.builder().id(docId));
                
                String field = record.getField();
                Object value = record.getValue();
                
                if ("content".equals(field)) {
                    builder.content(value.toString());
                } else if ("vector".equals(field)) {
                    List<Double> vector = parseVectorString(value.toString());
                    builder.vector(vector);
                } else if (field.startsWith("meta_")) {
                    Map<String, Object> metadata = builder.build().getMetadata();
                    if (metadata == null) {
                        metadata = new HashMap<>();
                    }
                    metadata.put(field.substring(5), value);
                    builder.metadata(metadata);
                }
                
                builder.timestamp(record.getTime());
                builder.measurement(record.getMeasurement());
            }
        }
        
        return vectorBuilders.values().stream()
                .map(InfluxDBVector.InfluxDBVectorBuilder::build)
                .filter(v -> v.getVector() != null && !v.getVector().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 解析向量字符串
     *
     * @param vectorStr 向量字符串
     * @return 向量列表
     */
    private List<Double> parseVectorString(String vectorStr) {
        try {
            if (StringUtils.isBlank(vectorStr)) {
                return new ArrayList<>();
            }
            
            // 移除方括号
            String cleaned = vectorStr.trim();
            if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            }
            
            // 分割并解析
            String[] parts = cleaned.split(",");
            List<Double> vector = new ArrayList<>();
            for (String part : parts) {
                vector.add(Double.parseDouble(part.trim()));
            }
            return vector;
        } catch (Exception e) {
            log.error("Failed to parse vector string: {}", vectorStr, e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算相似度并排序
     *
     * @param vectors         向量列表
     * @param queryVector     查询向量
     * @param similarityMetric 相似度计算方法
     * @return 排序后的向量列表
     */
    private List<InfluxDBVector> calculateSimilarities(List<InfluxDBVector> vectors, 
                                                      List<Double> queryVector,
                                                      InfluxDBQueryRequest.SimilarityMetric similarityMetric) {
        return vectors.stream()
                .peek(vector -> {
                    double similarity = calculateSimilarity(queryVector, vector.getVector(), similarityMetric);
                    vector.setScore(similarity);
                })
                .sorted((v1, v2) -> Double.compare(v2.getScore(), v1.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 计算两个向量的相似度
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @param metric  相似度计算方法
     * @return 相似度分数
     */
    private double calculateSimilarity(List<Double> vector1, List<Double> vector2, 
                                     InfluxDBQueryRequest.SimilarityMetric metric) {
        if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
            return 0.0;
        }

        switch (metric) {
            case COSINE:
                return cosineSimilarity(vector1, vector2);
            case DOT_PRODUCT:
                return dotProduct(vector1, vector2);
            case EUCLIDEAN:
                return 1.0 / (1.0 + euclideanDistance(vector1, vector2));
            case MANHATTAN:
                return 1.0 / (1.0 + manhattanDistance(vector1, vector2));
            default:
                return cosineSimilarity(vector1, vector2);
        }
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Double> vector1, List<Double> vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算点积
     */
    private double dotProduct(List<Double> vector1, List<Double> vector2) {
        double sum = 0.0;
        for (int i = 0; i < vector1.size(); i++) {
            sum += vector1.get(i) * vector2.get(i);
        }
        return sum;
    }

    /**
     * 计算欧几里得距离
     */
    private double euclideanDistance(List<Double> vector1, List<Double> vector2) {
        double sum = 0.0;
        for (int i = 0; i < vector1.size(); i++) {
            sum += Math.pow(vector1.get(i) - vector2.get(i), 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * 计算曼哈顿距离
     */
    private double manhattanDistance(List<Double> vector1, List<Double> vector2) {
        double sum = 0.0;
        for (int i = 0; i < vector1.size(); i++) {
            sum += Math.abs(vector1.get(i) - vector2.get(i));
        }
        return sum;
    }

    /**
     * 更新向量缓存
     */
    private void updateCache(String docId, List<Double> vector) {
        if (vectorCache.size() >= maxCacheSize) {
            // 简单的缓存清理策略
            vectorCache.clear();
        }
        vectorCache.put(docId, vector);
    }
}
