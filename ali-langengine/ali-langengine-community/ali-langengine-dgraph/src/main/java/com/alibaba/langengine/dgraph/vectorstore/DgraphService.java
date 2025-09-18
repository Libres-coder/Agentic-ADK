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
package com.alibaba.langengine.dgraph.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphGrpc;
import io.dgraph.Transaction;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.langengine.dgraph.DgraphConfiguration.*;


@Slf4j
@Data
public class DgraphService implements AutoCloseable {

    private final DgraphClient dgraphClient;
    private final ManagedChannel channel;
    private final DgraphParam param;

    /**
     * 构造函数
     *
     * @param serverUrl Dgraph 服务器地址
     * @param param     Dgraph 参数配置
     */
    public DgraphService(String serverUrl, DgraphParam param) {
        this.param = param;
        
        // 创建 gRPC 通道
        this.channel = ManagedChannelBuilder.forTarget(serverUrl)
                .usePlaintext()
                .maxInboundMessageSize(4 * 1024 * 1024) // 4MB
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();
        
        // 创建 Dgraph 客户端
        DgraphGrpc.DgraphStub stub = DgraphGrpc.newStub(channel);
        this.dgraphClient = new DgraphClient(stub);
        
        // 初始化模式
        initializeSchema();
    }

    /**
     * 初始化 Dgraph 模式
     */
    private void initializeSchema() {
        try {
            String schema = buildSchema();
            dgraphClient.alter(io.dgraph.DgraphProto.Operation.newBuilder().setSchema(schema).build());
            log.info("Dgraph schema initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Dgraph schema", e);
            throw new RuntimeException("Failed to initialize Dgraph schema", e);
        }
    }

    /**
     * 构建 Dgraph 模式定义
     */
    private String buildSchema() {
        // 根据相似度算法选择合适的metric
        String metric = getSimilarityMetric(param.getSimilarityAlgorithm());
        
        // 构建HNSW索引参数
        String hnswParams = String.format("m: %d, efConstruction: %d, ef: %d, metric: \"%s\"",
                param.getHnswM(),
                param.getHnswEfConstruction(),
                param.getHnswEf(),
                metric);
        
        return String.format(
            "%s: float32vector @index(hnsw(%s)) .\n" +
            "%s: string @index(exact, fulltext) .\n" +
            "%s: string @index(exact) .\n" +
            "type VectorDocument {\n" +
            "    %s\n" +
            "    %s\n" +
            "    %s\n" +
            "}",
            param.getVectorFieldName(), hnswParams,
            param.getContentFieldName(),
            param.getMetadataFieldName(),
            param.getVectorFieldName(),
            param.getContentFieldName(),
            param.getMetadataFieldName()
        );
    }

    /**
     * 根据相似度算法获取对应的metric参数
     */
    private String getSimilarityMetric(String algorithm) {
        switch (algorithm.toLowerCase()) {
            case "cosine":
                return "cosine";
            case "euclidean":
                return "euclidean";
            case "dotproduct":
            case "dot":
                return "dotproduct";
            default:
                log.warn("Unknown similarity algorithm: {}, using cosine as default", algorithm);
                return "cosine";
        }
    }

    /**
     * 添加文档到向量库
     *
     * @param documents 文档列表
     * @param embeddings 对应的向量嵌入
     * @return 添加的文档数量
     */
    public int addDocuments(List<Document> documents, List<List<Float>> embeddings) {
        if (CollectionUtils.isEmpty(documents) || CollectionUtils.isEmpty(embeddings)) {
            return 0;
        }

        if (documents.size() != embeddings.size()) {
            throw new IllegalArgumentException("Documents and embeddings size mismatch");
        }

        try (Transaction txn = dgraphClient.newTransaction()) {
            List<Map<String, Object>> mutations = new ArrayList<>();
            
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                List<Float> embedding = embeddings.get(i);
                
                Map<String, Object> mutation = new HashMap<>();
                mutation.put("dgraph.type", "VectorDocument");
                mutation.put(param.getContentFieldName(), doc.getPageContent());
                mutation.put(param.getVectorFieldName(), embedding);
                
                if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                    mutation.put(param.getMetadataFieldName(), JSON.toJSONString(doc.getMetadata()));
                }
                
                mutations.add(mutation);
            }

            // 批量插入
            List<List<Map<String, Object>>> batches = partition(mutations, param.getBatchSize());
            int totalInserted = 0;
            
            for (List<Map<String, Object>> batch : batches) {
                String mutationJson = JSON.toJSONString(batch);
                io.dgraph.DgraphProto.Mutation mutation = io.dgraph.DgraphProto.Mutation.newBuilder()
                        .setSetJson(com.google.protobuf.ByteString.copyFromUtf8(mutationJson))
                        .build();
                
                txn.mutate(mutation);
                totalInserted += batch.size();
            }
            
            txn.commit();
            log.info("Successfully added {} documents to Dgraph", totalInserted);
            return totalInserted;
            
        } catch (Exception e) {
            log.error("Failed to add documents to Dgraph", e);
            throw new RuntimeException("Failed to add documents to Dgraph", e);
        }
    }

    /**
     * 向量相似性搜索
     *
     * @param queryEmbedding 查询向量
     * @param k              返回结果数量
     * @param filter         过滤条件
     * @return 相似的文档列表
     */
    public List<Document> similaritySearch(List<Float> queryEmbedding, int k, Map<String, Object> filter) {
        if (CollectionUtils.isEmpty(queryEmbedding)) {
            return Collections.emptyList();
        }

        try (Transaction txn = dgraphClient.newReadOnlyTransaction()) {
            String query = buildSimilarityQuery(queryEmbedding, k, filter);
            
            io.dgraph.DgraphProto.Response response = txn.query(query);
            return parseSearchResults(response);
            
        } catch (Exception e) {
            log.error("Failed to perform similarity search in Dgraph", e);
            throw new RuntimeException("Failed to perform similarity search in Dgraph", e);
        }
    }

    /**
     * 构建相似性搜索查询 - 使用Dgraph的similar_to向量搜索功能
     */
    private String buildSimilarityQuery(List<Float> queryEmbedding, int k, Map<String, Object> filter) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{\n");
        
        // 使用similar_to函数进行向量相似性搜索
        queryBuilder.append("  search(func: similar_to(").append(param.getVectorFieldName()).append(", ");
        queryBuilder.append(k).append(", \"");
        
        // 构建向量字符串
        String vectorStr = queryEmbedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        queryBuilder.append("[").append(vectorStr).append("]");
        queryBuilder.append("\")) ");
        
        // 添加过滤条件
        if (filter != null && !filter.isEmpty()) {
            queryBuilder.append("@filter(");
            List<String> filterConditions = new ArrayList<>();
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    filterConditions.add(String.format("eq(%s, \"%s\")", key, value));
                } else {
                    filterConditions.add(String.format("eq(%s, %s)", key, value));
                }
            }
            queryBuilder.append(String.join(" AND ", filterConditions));
            queryBuilder.append(") ");
        }
        
        queryBuilder.append("{\n");
        queryBuilder.append("    uid\n");
        queryBuilder.append(String.format("    %s\n", param.getContentFieldName()));
        queryBuilder.append(String.format("    %s\n", param.getVectorFieldName()));
        queryBuilder.append(String.format("    %s\n", param.getMetadataFieldName()));
        queryBuilder.append("    _distance_\n"); // Dgraph向量搜索会返回距离信息
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");
        
        return queryBuilder.toString();
    }

    /**
     * 解析搜索结果
     */
    private List<Document> parseSearchResults(io.dgraph.DgraphProto.Response response) {
        try {
            String jsonResult = response.getJson().toStringUtf8();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = JSON.parseObject(jsonResult, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> searchResults = (List<Map<String, Object>>) result.get("search");
            
            if (CollectionUtils.isEmpty(searchResults)) {
                return Collections.emptyList();
            }
            
            return searchResults.stream()
                    .map(this::convertToDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Failed to parse search results", e);
            return Collections.emptyList();
        }
    }

    /**
     * 将搜索结果转换为 Document 对象
     */
    private Document convertToDocument(Map<String, Object> result) {
        try {
            String content = (String) result.get(param.getContentFieldName());
            if (StringUtils.isEmpty(content)) {
                return null;
            }
            
            Map<String, Object> metadata = new HashMap<>();
            String metadataStr = (String) result.get(param.getMetadataFieldName());
            if (StringUtils.isNotEmpty(metadataStr)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsedMetadata = JSON.parseObject(metadataStr, Map.class);
                metadata = parsedMetadata;
            }
            
            // 添加 UID 到元数据
            Object uid = result.get("uid");
            if (uid != null) {
                metadata.put("uid", uid.toString());
            }
            
            return new Document(content, metadata);
            
        } catch (Exception e) {
            log.error("Failed to convert result to Document", e);
            return null;
        }
    }

    /**
     * 删除文档
     *
     * @param filter 删除条件
     * @return 删除的文档数量
     */
    public int deleteDocuments(Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            log.warn("Delete filter is empty, skipping deletion");
            return 0;
        }

        try (Transaction txn = dgraphClient.newTransaction()) {
            // 首先查询要删除的文档
            String queryStr = buildDeleteQuery(filter);
            io.dgraph.DgraphProto.Response response = txn.query(queryStr);
            
            List<String> uidsToDelete = extractUidsFromResponse(response);
            if (CollectionUtils.isEmpty(uidsToDelete)) {
                return 0;
            }
            
            // 删除文档
            for (String uid : uidsToDelete) {
                io.dgraph.DgraphProto.Mutation mutation = io.dgraph.DgraphProto.Mutation.newBuilder()
                        .setDelNquads(com.google.protobuf.ByteString.copyFromUtf8(String.format("<%s> * * .", uid)))
                        .build();
                txn.mutate(mutation);
            }
            
            txn.commit();
            log.info("Successfully deleted {} documents from Dgraph", uidsToDelete.size());
            return uidsToDelete.size();
            
        } catch (Exception e) {
            log.error("Failed to delete documents from Dgraph", e);
            throw new RuntimeException("Failed to delete documents from Dgraph", e);
        }
    }

    /**
     * 构建删除查询
     */
    private String buildDeleteQuery(Map<String, Object> filter) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("{\n");
        queryBuilder.append("  delete(func: type(VectorDocument)) @filter(");
        
        List<String> filterConditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            filterConditions.add(String.format("eq(%s, \"%s\")", entry.getKey(), entry.getValue()));
        }
        queryBuilder.append(String.join(" AND ", filterConditions));
        queryBuilder.append(") {\n");
        queryBuilder.append("    uid\n");
        queryBuilder.append("  }\n");
        queryBuilder.append("}\n");
        
        return queryBuilder.toString();
    }

    /**
     * 从响应中提取 UID 列表
     */
    private List<String> extractUidsFromResponse(io.dgraph.DgraphProto.Response response) {
        try {
            String jsonResult = response.getJson().toStringUtf8();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = JSON.parseObject(jsonResult, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deleteResults = (List<Map<String, Object>>) result.get("delete");
            
            if (CollectionUtils.isEmpty(deleteResults)) {
                return Collections.emptyList();
            }
            
            return deleteResults.stream()
                    .map(r -> (String) r.get("uid"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Failed to extract UIDs from response", e);
            return Collections.emptyList();
        }
    }

    /**
     * 将列表分割为指定大小的批次
     */
    private <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown();
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            log.warn("Failed to shutdown Dgraph channel gracefully", e);
            Thread.currentThread().interrupt();
        }
    }
}
