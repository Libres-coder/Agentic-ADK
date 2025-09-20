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
package com.alibaba.langengine.lancedb.client;

import com.alibaba.langengine.lancedb.LanceDbConfiguration;
import com.alibaba.langengine.lancedb.LanceDbException;
import com.alibaba.langengine.lancedb.client.interceptor.AuthenticationInterceptor;
import com.alibaba.langengine.lancedb.client.interceptor.LoggingInterceptor;
import com.alibaba.langengine.lancedb.model.LanceDbQueryRequest;
import com.alibaba.langengine.lancedb.model.LanceDbQueryResponse;
import com.alibaba.langengine.lancedb.model.LanceDbVector;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
public class LanceDbClient implements AutoCloseable {

    private final LanceDbConfiguration configuration;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HttpRequestExecutor requestExecutor;
    private volatile LocalDateTime lastRequestTime;

    /**
     * 构造函数
     *
     * @param configuration LanceDB配置
     */
    public LanceDbClient(LanceDbConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("LanceDB configuration cannot be null");
        }
        if (!configuration.isValid()) {
            throw new IllegalArgumentException("Invalid LanceDB configuration");
        }

        this.configuration = configuration;
        this.objectMapper = new ObjectMapper();
        this.httpClient = createHttpClient();
        this.requestExecutor = new HttpRequestExecutor(httpClient, objectMapper);

        log.info("LanceDB client initialized with server: {}", configuration.getFullServerUrl());
    }

    /**
     * 创建HTTP客户端
     *
     * @return HTTP客户端
     */
    private OkHttpClient createHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(
                configuration.getConnectionPoolSize(),
                5,
                TimeUnit.MINUTES
        );

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(configuration.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(configuration.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(configuration.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true);

        // 添加拦截器
        addInterceptors(builder);

        return builder.build();
    }

    /**
     * 添加拦截器到HTTP客户端构建器
     *
     * @param builder HTTP客户端构建器
     */
    private void addInterceptors(OkHttpClient.Builder builder) {
        // 添加认证拦截器
        builder.addInterceptor(new AuthenticationInterceptor(configuration.getApiKey()));
        
        // 添加日志拦截器
        builder.addInterceptor(new LoggingInterceptor());
    }

    /**
     * 强制执行速率限制
     *
     * @throws InterruptedException 如果线程在等待期间被中断
     */
    public synchronized void enforceRateLimit() throws InterruptedException {
        if (lastRequestTime != null) {
            long elapsedTime = System.currentTimeMillis() - lastRequestTime.toEpochSecond(ZoneOffset.UTC) * 1000;
            long minInterval = configuration.getRetryIntervalMs();

            if (elapsedTime < minInterval) {
                long waitTime = minInterval - elapsedTime;
                Thread.sleep(waitTime);
            }
        }
        lastRequestTime = LocalDateTime.now();
    }

    /**
     * 查询向量
     *
     * @param tableName 表名
     * @param request   查询请求
     * @return 查询响应
     * @throws LanceDbException 查询异常
     */
    public LanceDbQueryResponse query(String tableName, LanceDbQueryRequest request) throws LanceDbException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (request == null) {
            throw new IllegalArgumentException("Query request cannot be null");
        }
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid query request");
        }

        try {
            enforceRateLimit();
            return executeQuery(tableName, request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LanceDbException("Query interrupted", e);
        }
    }

    /**
     * 执行查询
     *
     * @param tableName 表名
     * @param request   查询请求
     * @return 查询响应
     * @throws LanceDbException 查询异常
     */
    private LanceDbQueryResponse executeQuery(String tableName, LanceDbQueryRequest request) throws LanceDbException {
        String url = configuration.getFullServerUrl() + "/tables/" + tableName + "/query";
        
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            String responseText = requestExecutor.executeForRawResponse(httpRequest, "QUERY_FAILED");
            return parseQueryResponse(responseText);
        } catch (IOException e) {
            throw new LanceDbException("Failed to serialize query request", e, "QUERY_FAILED", null);
        }
    }

    /**
     * 解析查询响应
     * 
     * 注意：由于LanceDB API在某些情况下返回不同的响应格式，
     * 我们首先尝试解析为完整的LanceDbQueryResponse对象，
     * 如果失败则尝试解析为向量列表并包装成响应对象。
     * 这种设计是为了兼容不同版本的API响应格式。
     *
     * @param responseText 响应文本
     * @return 查询响应
     * @throws LanceDbException 解析异常
     */
    private LanceDbQueryResponse parseQueryResponse(String responseText) throws LanceDbException {
        try {
            // 尝试解析为完整响应对象
            return objectMapper.readValue(responseText, LanceDbQueryResponse.class);
        } catch (Exception e) {
            log.debug("Failed to parse as LanceDbQueryResponse, trying as vector list", e);
            try {
                // 如果失败，尝试解析为向量列表
                List<LanceDbVector> vectors = objectMapper.readValue(
                        responseText, new TypeReference<List<LanceDbVector>>() {});
                return LanceDbQueryResponse.success(vectors);
            } catch (Exception e2) {
                throw new LanceDbException("Failed to parse query response: incompatible format", e, "PARSE_FAILED", null);
            }
        }
    }

    /**
     * 插入向量
     *
     * @param tableName 表名
     * @param vectors   向量列表
     * @throws LanceDbException 插入异常
     */
    public void insert(String tableName, List<LanceDbVector> vectors) throws LanceDbException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (vectors == null || vectors.isEmpty()) {
            throw new IllegalArgumentException("Vectors cannot be null or empty");
        }

        try {
            enforceRateLimit();
            executeInsert(tableName, vectors);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LanceDbException("Insert interrupted", e);
        }
    }

    /**
     * 执行插入
     *
     * @param tableName 表名
     * @param vectors   向量列表
     * @throws LanceDbException 插入异常
     */
    private void executeInsert(String tableName, List<LanceDbVector> vectors) throws LanceDbException {
        String url = configuration.getFullServerUrl() + "/tables/" + tableName + "/add";
        
        try {
            String requestBody = objectMapper.writeValueAsString(vectors);
            RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
            
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            requestExecutor.executeNoResponse(httpRequest, "INSERT_FAILED");
        } catch (IOException e) {
            throw new LanceDbException("Failed to serialize insert request", e, "INSERT_FAILED", null);
        }
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param dimension 向量维度
     * @throws LanceDbException 创建异常
     */
    public void createTable(String tableName, int dimension) throws LanceDbException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }

        try {
            enforceRateLimit();
            executeCreateTable(tableName, dimension);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LanceDbException("Create table interrupted", e);
        }
    }

    /**
     * 执行创建表
     *
     * @param tableName 表名
     * @param dimension 向量维度
     * @throws LanceDbException 创建异常
     */
    private void executeCreateTable(String tableName, int dimension) throws LanceDbException {
        String url = configuration.getFullServerUrl() + "/tables/" + tableName;
        
        String requestBody = String.format("{\"dimension\": %d}", dimension);
        RequestBody body = RequestBody.create(requestBody, MediaType.get("application/json"));
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        requestExecutor.executeNoResponse(httpRequest, "CREATE_TABLE_FAILED");
    }

    /**
     * 删除表
     *
     * @param tableName 表名
     * @throws LanceDbException 删除异常
     */
    public void dropTable(String tableName) throws LanceDbException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }

        try {
            enforceRateLimit();
            executeDropTable(tableName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LanceDbException("Drop table interrupted", e);
        }
    }

    /**
     * 执行删除表
     *
     * @param tableName 表名
     * @throws LanceDbException 删除异常
     */
    private void executeDropTable(String tableName) throws LanceDbException {
        String url = configuration.getFullServerUrl() + "/tables/" + tableName;
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .delete()
                .build();

        requestExecutor.executeNoResponse(httpRequest, "DROP_TABLE_FAILED");
    }

    /**
     * 获取配置信息
     *
     * @return 配置对象
     */
    public LanceDbConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 关闭客户端资源
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
        log.info("LanceDB client closed");
    }
}
