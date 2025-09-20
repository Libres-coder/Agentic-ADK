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
package com.alibaba.langengine.vald.vectorstore.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

// Vald gRPC generated classes (will be generated after compiling proto files)
import com.alibaba.langengine.vald.api.v1.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Data
public class ValdService {

    private final String host;
    private final int port;
    private ManagedChannel channel;
    
    // gRPC client stubs
    private InsertServiceGrpc.InsertServiceBlockingStub insertStub;
    private SearchServiceGrpc.SearchServiceBlockingStub searchStub;
    private UpdateServiceGrpc.UpdateServiceBlockingStub updateStub;
    private RemoveServiceGrpc.RemoveServiceBlockingStub removeStub;

    public ValdService(String host, int port) {
        this.host = host;
        this.port = port;
        
        // Initialize gRPC channel
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(64 * 1024 * 1024) // 64MB
                .build();
        
        // Initialize gRPC stubs
        this.insertStub = InsertServiceGrpc.newBlockingStub(channel);
        this.searchStub = SearchServiceGrpc.newBlockingStub(channel);
        this.updateStub = UpdateServiceGrpc.newBlockingStub(channel);
        this.removeStub = RemoveServiceGrpc.newBlockingStub(channel);
        
        log.info("Vald service initialized with {}:{}", host, port);
    }

    /**
     * 插入向量
     *
     * @param request 插入请求
     * @return 插入结果位置信息
     */
    public ObjectLocation insert(ValdInsertRequest request) {
        try {
            log.debug("Inserting vector with id: {}", request.getId());
            
            // 构建protobuf请求
            com.alibaba.langengine.vald.api.v1.Object.Builder vectorBuilder = com.alibaba.langengine.vald.api.v1.Object.newBuilder()
                    .setId(request.getId());
            
            // 添加向量数据
            if (request.getVector() != null) {
                for (Double value : request.getVector()) {
                    vectorBuilder.addVector(value.floatValue());
                }
            }
            
            // 构建插入配置
            InsertConfig.Builder configBuilder = InsertConfig.newBuilder()
                    .setSkipStrictExistCheck(false)
                    .setTimestamp(System.currentTimeMillis());
            
            InsertRequest insertRequest = InsertRequest.newBuilder()
                    .setVector(vectorBuilder.build())
                    .setConfig(configBuilder.build())
                    .build();
            
            // 执行gRPC调用
            ObjectLocation location = insertStub.insert(insertRequest);
            log.debug("Successfully inserted vector with id: {}, uuid: {}", request.getId(), location.getUuid());
            
            return location;
        } catch (StatusRuntimeException e) {
            log.error("gRPC error inserting vector with id: {}", request.getId(), e);
            throw new RuntimeException("Failed to insert vector: " + e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to insert vector with id: {}", request.getId(), e);
            throw new RuntimeException("Failed to insert vector", e);
        }
    }

    /**
     * 批量插入向量
     *
     * @param requests 插入请求列表
     * @return 插入结果位置信息列表
     */
    public ObjectLocations multiInsert(List<ValdInsertRequest> requests) {
        try {
            log.debug("Multi-inserting {} vectors", requests.size());
            
            // 构建批量插入请求
            List<InsertRequest> insertRequests = new ArrayList<>();
            
            for (ValdInsertRequest request : requests) {
                com.alibaba.langengine.vald.api.v1.Object.Builder vectorBuilder = com.alibaba.langengine.vald.api.v1.Object.newBuilder()
                        .setId(request.getId());
                
                // 添加向量数据
                if (request.getVector() != null) {
                    for (Double value : request.getVector()) {
                        vectorBuilder.addVector(value.floatValue());
                    }
                }
                
                InsertConfig.Builder configBuilder = InsertConfig.newBuilder()
                        .setSkipStrictExistCheck(false)
                        .setTimestamp(System.currentTimeMillis());
                
                InsertRequest insertRequest = InsertRequest.newBuilder()
                        .setVector(vectorBuilder.build())
                        .setConfig(configBuilder.build())
                        .build();
                
                insertRequests.add(insertRequest);
            }
            
            MultiInsertRequest multiRequest = MultiInsertRequest.newBuilder()
                    .addAllRequests(insertRequests)
                    .build();
            
            // 执行gRPC调用
            ObjectLocations locations = insertStub.multiInsert(multiRequest);
            log.debug("Successfully multi-inserted {} vectors", requests.size());
            
            return locations;
        } catch (StatusRuntimeException e) {
            log.error("gRPC error multi-inserting vectors", e);
            throw new RuntimeException("Failed to multi-insert vectors: " + e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to multi-insert vectors", e);
            throw new RuntimeException("Failed to multi-insert vectors", e);
        }
    }

    /**
     * 搜索向量
     *
     * @param request 搜索请求
     * @return 搜索响应
     */
    public ValdSearchResponse search(ValdSearchRequest request) {
        try {
            log.debug("Searching for {} nearest neighbors", request.getK());
            
            // 构建搜索配置
            SearchConfig.Builder configBuilder = SearchConfig.newBuilder()
                    .setNum(request.getK())
                    .setTimeout(30000000000L); // 30 seconds in nanoseconds
            
            if (request.getRadius() != null) {
                configBuilder.setRadius(request.getRadius().floatValue());
            }
            if (request.getEpsilon() != null) {
                configBuilder.setEpsilon(request.getEpsilon().floatValue());
            }
            
            // 构建搜索请求
            SearchRequest.Builder searchRequestBuilder = SearchRequest.newBuilder()
                    .setConfig(configBuilder.build());
            
            // 添加查询向量
            if (request.getVector() != null) {
                for (Double value : request.getVector()) {
                    searchRequestBuilder.addVector(value.floatValue());
                }
            }
            
            SearchRequest searchRequest = searchRequestBuilder.build();
            
            // 执行gRPC调用
            SearchResponse response = searchStub.search(searchRequest);
            
            // 转换响应
            List<ValdSearchResponse.ValdSearchResult> results = response.getResultsList()
                    .stream()
                    .map(result -> {
                        // Vald搜索结果只包含ID和距离，不包含向量数据
                        return new ValdSearchResponse.ValdSearchResult(
                                result.getId(),
                                (double) result.getDistance(),
                                null  // 元数据为空
                        );
                    })
                    .collect(Collectors.toList());
            
            log.debug("Search completed, found {} results", results.size());
            return new ValdSearchResponse(results);
            
        } catch (StatusRuntimeException e) {
            log.error("gRPC error searching vectors", e);
            throw new RuntimeException("Failed to search vectors: " + e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to search vectors", e);
            throw new RuntimeException("Failed to search vectors", e);
        }
    }

    /**
     * 根据ID搜索向量
     *
     * @param id 向量ID
     * @param k 返回结果数量
     * @return 搜索响应
     */
    public ValdSearchResponse searchByID(String id, int k) {
        try {
            log.debug("Searching by ID: {} for {} nearest neighbors", id, k);
            
            // 构建搜索配置
            SearchConfig.Builder configBuilder = SearchConfig.newBuilder()
                    .setNum(k)
                    .setTimeout(30000000000L); // 30 seconds in nanoseconds
            
            SearchByIDRequest request = SearchByIDRequest.newBuilder()
                    .setId(id)
                    .setConfig(configBuilder.build())
                    .build();
            
            // 执行gRPC调用
            SearchResponse response = searchStub.searchByID(request);
            
            // 转换响应
            List<ValdSearchResponse.ValdSearchResult> results = response.getResultsList()
                    .stream()
                    .map(result -> {
                        // Vald搜索结果只包含ID和距离，不包含向量数据
                        return new ValdSearchResponse.ValdSearchResult(
                                result.getId(),
                                (double) result.getDistance(),
                                null  // 元数据为空
                        );
                    })
                    .collect(Collectors.toList());
            
            return new ValdSearchResponse(results);
            
        } catch (StatusRuntimeException e) {
            log.error("gRPC error searching by ID: {}", id, e);
            throw new RuntimeException("Failed to search by ID: " + e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to search by ID: {}", id, e);
            throw new RuntimeException("Failed to search by ID", e);
        }
    }

    /**
     * 更新向量
     *
     * @param request 更新请求
     * @return 更新结果位置信息
     */
    public ObjectLocation update(ValdInsertRequest request) {
        try {
            log.debug("Updating vector with id: {}", request.getId());
            
            // 构建protobuf请求
            com.alibaba.langengine.vald.api.v1.Object.Builder vectorBuilder = com.alibaba.langengine.vald.api.v1.Object.newBuilder()
                    .setId(request.getId());
            
            // 添加向量数据
            if (request.getVector() != null) {
                for (Double value : request.getVector()) {
                    vectorBuilder.addVector(value.floatValue());
                }
            }
            
            // 构建更新配置
            UpdateConfig.Builder configBuilder = UpdateConfig.newBuilder()
                    .setSkipStrictExistCheck(false)
                    .setTimestamp(System.currentTimeMillis());
            
            UpdateRequest updateRequest = UpdateRequest.newBuilder()
                    .setVector(vectorBuilder.build())
                    .setConfig(configBuilder.build())
                    .build();
            
            // 执行gRPC调用
            ObjectLocation location = updateStub.update(updateRequest);
            log.debug("Successfully updated vector with id: {}, uuid: {}", request.getId(), location.getUuid());
            
            return location;
        } catch (StatusRuntimeException e) {
            log.error("gRPC error updating vector with id: {}", request.getId(), e);
            throw new RuntimeException("Failed to update vector: " + e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to update vector with id: {}", request.getId(), e);
            throw new RuntimeException("Failed to update vector", e);
        }
    }

    /**
     * 删除向量
     *
     * @param id 向量ID
     * @return 删除结果位置信息
     */
    public ObjectLocation remove(String id) {
        try {
            log.debug("Removing vector with id: {}", id);
            
            // 构建删除配置
            RemoveConfig.Builder configBuilder = RemoveConfig.newBuilder()
                    .setSkipStrictExistCheck(false)
                    .setTimestamp(System.currentTimeMillis());
            
            RemoveRequest request = RemoveRequest.newBuilder()
                    .setId(id)
                    .setConfig(configBuilder.build())
                    .build();
            
            // 执行gRPC调用
            ObjectLocation location = removeStub.remove(request);
            log.debug("Successfully removed vector with id: {}, uuid: {}", id, location.getUuid());
            
            return location;
        } catch (StatusRuntimeException e) {
            log.error("gRPC error removing vector with id: {}", id, e);
            throw new RuntimeException("Failed to remove vector: " + e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to remove vector with id: {}", id, e);
            throw new RuntimeException("Failed to remove vector", e);
        }
    }

    /**
     * 批量删除向量
     *
     * @param ids 向量ID列表
     * @return 删除结果位置信息列表
     */
    public ObjectLocations multiRemove(List<String> ids) {
        try {
            log.debug("Multi-removing {} vectors", ids.size());
            
            List<RemoveRequest> removeRequests = new ArrayList<>();
            
            for (String id : ids) {
                RemoveConfig.Builder configBuilder = RemoveConfig.newBuilder()
                        .setSkipStrictExistCheck(false)
                        .setTimestamp(System.currentTimeMillis());
                
                RemoveRequest request = RemoveRequest.newBuilder()
                        .setId(id)
                        .setConfig(configBuilder.build())
                        .build();
                
                removeRequests.add(request);
            }
            
            MultiRemoveRequest multiRequest = MultiRemoveRequest.newBuilder()
                    .addAllRequests(removeRequests)
                    .build();
            
            // 执行gRPC调用
            ObjectLocations locations = removeStub.multiRemove(multiRequest);
            log.debug("Successfully multi-removed {} vectors", ids.size());
            
            return locations;
        } catch (StatusRuntimeException e) {
            log.error("gRPC error multi-removing vectors", e);
            throw new RuntimeException("Failed to multi-remove vectors: " + e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to multi-remove vectors", e);
            throw new RuntimeException("Failed to multi-remove vectors", e);
        }
    }

    /**
     * 检查连接状态
     *
     * @return 是否连接正常
     */
    public boolean isConnected() {
        return channel != null && !channel.isShutdown() && !channel.isTerminated();
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (channel != null && !channel.isShutdown()) {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("Vald service connection closed");
            }
        } catch (InterruptedException e) {
            log.error("Failed to close Vald service", e);
            Thread.currentThread().interrupt();
        }
    }

    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    public void setInsertStub(InsertServiceGrpc.InsertServiceBlockingStub insertStub) {
        this.insertStub = insertStub;
    }

    public void setSearchStub(SearchServiceGrpc.SearchServiceBlockingStub searchStub) {
        this.searchStub = searchStub;
    }

    public void setUpdateStub(UpdateServiceGrpc.UpdateServiceBlockingStub updateStub) {
        this.updateStub = updateStub;
    }

    public void setRemoveStub(RemoveServiceGrpc.RemoveServiceBlockingStub removeStub) {
        this.removeStub = removeStub;
    }

}