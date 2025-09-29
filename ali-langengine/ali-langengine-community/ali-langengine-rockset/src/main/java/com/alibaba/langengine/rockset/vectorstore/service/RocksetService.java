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
package com.alibaba.langengine.rockset.vectorstore.service;

import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import com.alibaba.langengine.rockset.vectorstore.RocksetException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Response;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class RocksetService extends RetrofitInitService<RocksetApi> {

    private String workspace;
    private String apiKey;
    private RocksetApi service;

    public RocksetService(String serverUrl, String apiKey, String workspace, Duration timeout) {
        super(serverUrl, timeout, false, null);
        this.apiKey = apiKey;
        this.workspace = workspace;
        this.service = getService();
    }

    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.isNotEmpty(apiKey)) {
            headers.put("Authorization", "ApiKey " + apiKey);
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    @Override
    public Class<RocksetApi> getServiceApiClass() {
        return RocksetApi.class;
    }

    /**
     * 创建集合
     */
    public RocksetCreateCollectionResponse createCollection(String collectionName, String description) {
        try {
            RocksetCreateCollectionRequest request = new RocksetCreateCollectionRequest();
            request.setName(collectionName);
            request.setDescription(description);
            request.setRetentionSecs(30L * 24 * 60 * 60); // 30 days

            Response<RocksetCreateCollectionResponse> response = 
                service.createCollection(workspace, request, createHeaders()).execute();
            
            if (!response.isSuccessful()) {
                log.error("Failed to create collection: {}, error: {}", collectionName, response.message());
                throw RocksetException.operationFailed("Failed to create collection: " + collectionName);
            }
            
            log.info("Collection created successfully: {}", collectionName);
            return response.body();
        } catch (Exception e) {
            log.error("Error creating collection: {}", collectionName, e);
            throw RocksetException.operationFailed("Error creating collection: " + collectionName, e);
        }
    }

    /**
     * 获取集合信息
     */
    public RocksetCollectionData getCollection(String collectionName) {
        try {
            Response<RocksetCollectionData> response = 
                service.getCollection(workspace, collectionName, createHeaders()).execute();
            
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return null; // 集合不存在
                }
                log.error("Failed to get collection: {}, error: {}", collectionName, response.message());
                throw RocksetException.operationFailed("Failed to get collection: " + collectionName);
            }
            
            return response.body();
        } catch (Exception e) {
            log.error("Error getting collection: {}", collectionName, e);
            throw RocksetException.operationFailed("Error getting collection: " + collectionName, e);
        }
    }

    /**
     * 插入文档
     */
    public RocksetInsertResponse insertDocuments(String collectionName, List<Document> documents,
                                               String fieldNamePageContent, String fieldNameUniqueId,
                                               String fieldMeta, String fieldNameVector) {
        if (CollectionUtils.isEmpty(documents)) {
            throw RocksetException.invalidParameter("Documents list cannot be empty");
        }

        try {
            List<RocksetInsertRequest.DocumentData> documentDataList = documents.stream()
                .map(doc -> {
                    RocksetInsertRequest.DocumentData documentData = new RocksetInsertRequest.DocumentData();
                    documentData.setContentId(doc.getUniqueId());
                    documentData.setId(doc.getUniqueId());
                    
                    // 设置页面内容
                    documentData.setPageContent(doc.getPageContent());
                    
                    // 设置元数据
                    if (MapUtils.isNotEmpty(doc.getMetadata())) {
                        documentData.setMetaData(doc.getMetadata());
                    }
                    
                    // 设置向量
                    if (CollectionUtils.isNotEmpty(doc.getEmbedding())) {
                        documentData.setVector(doc.getEmbedding());
                    }
                    
                    return documentData;
                }).collect(Collectors.toList());

            RocksetInsertRequest request = new RocksetInsertRequest();
            request.setData(documentDataList);

            Response<RocksetInsertResponse> response = 
                service.insertDocuments(workspace, collectionName, request, createHeaders()).execute();
            
            if (!response.isSuccessful()) {
                log.error("Failed to insert documents to collection: {}, error: {}", collectionName, response.message());
                throw RocksetException.operationFailed("Failed to insert documents to collection: " + collectionName);
            }
            
            log.info("Documents inserted successfully to collection: {}, count: {}", collectionName, documents.size());
            return response.body();
        } catch (Exception e) {
            log.error("Error inserting documents to collection: {}", collectionName, e);
            throw RocksetException.operationFailed("Error inserting documents to collection: " + collectionName, e);
        }
    }

    /**
     * 执行查询
     */
    public RocksetQueryResponse query(String sql, List<RocksetQueryRequest.Parameter> parameters) {
        try {
            RocksetQueryRequest request = new RocksetQueryRequest();
            request.setSql(sql);
            request.setParameters(parameters != null ? parameters : new ArrayList<>());

            Response<RocksetQueryResponse> response = 
                service.query(request, createHeaders()).execute();
            
            if (!response.isSuccessful()) {
                log.error("Failed to execute query, error: {}", response.message());
                throw RocksetException.operationFailed("Failed to execute query");
            }
            
            RocksetQueryResponse result = response.body();
            log.info("Query executed successfully, result count: {}", 
                result != null && result.getResults() != null ? result.getResults().size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Error executing query", e);
            throw RocksetException.operationFailed("Error executing query", e);
        }
    }

    /**
     * 删除文档
     */
    public Long deleteDocuments(String collectionName, List<String> documentIds) {
        if (CollectionUtils.isEmpty(documentIds)) {
            return 0L;
        }

        try {
            List<RocksetDeleteRequest.DeleteData> deleteDataList = documentIds.stream()
                .map(id -> {
                    RocksetDeleteRequest.DeleteData deleteData = new RocksetDeleteRequest.DeleteData();
                    deleteData.setId(id);
                    return deleteData;
                }).collect(Collectors.toList());

            RocksetDeleteRequest request = new RocksetDeleteRequest();
            request.setData(deleteDataList);

            Response<RocksetDeleteResponse> response = 
                service.deleteDocuments(workspace, collectionName, request, createHeaders()).execute();
            
            if (!response.isSuccessful()) {
                log.error("Failed to delete documents from collection: {}, error: {}", collectionName, response.message());
                throw RocksetException.operationFailed("Failed to delete documents from collection: " + collectionName);
            }
            
            RocksetDeleteResponse result = response.body();
            Long deleteCount = result != null ? 
                (long) result.getDeletedCount() : 0L;
            log.info("Documents deleted successfully from collection: {}, count: {}", collectionName, deleteCount);
            return deleteCount;
        } catch (Exception e) {
            log.error("Error deleting documents from collection: {}", collectionName, e);
            throw RocksetException.operationFailed("Error deleting documents from collection: " + collectionName, e);
        }
    }
}