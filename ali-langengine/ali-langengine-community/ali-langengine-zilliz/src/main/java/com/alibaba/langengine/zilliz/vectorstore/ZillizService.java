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
package com.alibaba.langengine.zilliz.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.google.common.collect.Lists;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.response.MutationResultWrapper;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
public class ZillizService {

    private static final int DEFAULT_VARCHAR_MAX_LENGTH = 512;

    private final MilvusServiceClient milvusClient;
    private final String collectionName;
    private final String partitionName;
    private final ZillizParam zillizParam;

    public ZillizService(String clusterEndpoint, String apiKey, String databaseName, String collectionName, String partitionName, ZillizParam zillizParam) {
        this.collectionName = collectionName;
        this.partitionName = partitionName;
        this.zillizParam = zillizParam != null ? zillizParam : new ZillizParam();

        ConnectParam connectParam = ConnectParam.newBuilder()
                .withUri(clusterEndpoint)
                .withToken(apiKey)
                .withDatabaseName(databaseName)
                .build();

        this.milvusClient = new MilvusServiceClient(connectParam);
    }

    public void init(Embeddings embedding) {
        try {
            if (!hasCollection()) {
                createCollection(embedding);
                createIndex();
                loadCollection();
            }
        } catch (Exception e) {
            log.error("Failed to initialize Zilliz collection", e);
            throw new RuntimeException("Failed to initialize Zilliz collection", e);
        }
    }

    private boolean hasCollection() {
        HasCollectionParam param = HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        R<Boolean> response = milvusClient.hasCollection(param);
        return response.getData();
    }

    private void createCollection(Embeddings embedding) {
        int dimension = zillizParam.getInitParam().getFieldEmbeddingsDimension();
        if (dimension <= 0) {
            List<String> testEmbedding = embedding.embedQuery("test", 1);
            if (CollectionUtils.isNotEmpty(testEmbedding)) {
                List<Float> embeddings = JSON.parseArray(testEmbedding.get(0), Float.class);
                dimension = embeddings.size();
            }
        }

        List<FieldType> fields = new ArrayList<>();
        
        // Primary key field
        fields.add(FieldType.newBuilder()
                .withName(zillizParam.getFieldNameUniqueId())
                .withDataType(DataType.VarChar)
                .withMaxLength(DEFAULT_VARCHAR_MAX_LENGTH)
                .withPrimaryKey(true)
                .withAutoID(false)
                .build());

        // Vector field
        fields.add(FieldType.newBuilder()
                .withName(zillizParam.getFieldNameEmbedding())
                .withDataType(DataType.FloatVector)
                .withDimension(dimension)
                .build());

        // Content field
        fields.add(FieldType.newBuilder()
                .withName(zillizParam.getFieldNamePageContent())
                .withDataType(DataType.VarChar)
                .withMaxLength(zillizParam.getInitParam().getFieldPageContentMaxLength())
                .build());

        CreateCollectionParam param = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("Zilliz Cloud collection for LangEngine")
                .withShardsNum(zillizParam.getInitParam().getShardsNum())
                .withFieldTypes(fields)
                .withConsistencyLevel(zillizParam.getInitParam().getConsistencyLevel())
                .build();

        R<RpcStatus> response = milvusClient.createCollection(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("Failed to create collection: " + response.getMessage());
        }
    }

    private void createIndex() {
        CreateIndexParam param = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(zillizParam.getFieldNameEmbedding())
                .withIndexType(zillizParam.getInitParam().getIndexEmbeddingsIndexType())
                .withMetricType(zillizParam.getInitParam().getIndexEmbeddingsMetricType())
                .withExtraParam(JSON.toJSONString(zillizParam.getInitParam().getIndexEmbeddingsExtraParam()))
                .build();

        R<RpcStatus> response = milvusClient.createIndex(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("Failed to create index: " + response.getMessage());
        }
    }

    private void loadCollection() {
        LoadCollectionParam param = LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();

        R<RpcStatus> response = milvusClient.loadCollection(param);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("Failed to load collection: " + response.getMessage());
        }
    }

    public void addDocuments(List<Document> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }

        List<List<Object>> data = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        List<List<Float>> embeddings = new ArrayList<>();
        List<String> contents = new ArrayList<>();

        for (Document document : documents) {
            String id = StringUtils.isNotEmpty(document.getUniqueId()) ? 
                    document.getUniqueId() : 
                    UUID.randomUUID().toString();
            
            ids.add(id);
            
            if (CollectionUtils.isNotEmpty(document.getEmbedding())) {
                List<Float> floatEmbedding = new ArrayList<>();
                for (Double d : document.getEmbedding()) {
                    floatEmbedding.add(d.floatValue());
                }
                embeddings.add(floatEmbedding);
            }
            
            contents.add(document.getPageContent());
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(zillizParam.getFieldNameUniqueId(), ids));
        fields.add(new InsertParam.Field(zillizParam.getFieldNameEmbedding(), embeddings));
        fields.add(new InsertParam.Field(zillizParam.getFieldNamePageContent(), contents));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        R<io.milvus.grpc.MutationResult> response = milvusClient.insert(insertParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("Failed to insert documents: " + response.getMessage());
        }
    }

    public List<Document> similaritySearch(List<Float> queryEmbedding, int topK) {
        List<String> searchOutputFields = Arrays.asList(
                zillizParam.getFieldNameUniqueId(),
                zillizParam.getFieldNamePageContent()
        );

        SearchParam.Builder builder = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withMetricType(zillizParam.getInitParam().getIndexEmbeddingsMetricType())
                .withOutFields(searchOutputFields)
                .withTopK(topK)
                .withVectors(Arrays.asList(queryEmbedding))
                .withVectorFieldName(zillizParam.getFieldNameEmbedding())
                .withParams(JSON.toJSONString(zillizParam.getSearchParams()));

        if (StringUtils.isNotEmpty(partitionName)) {
            builder.withPartitionNames(Arrays.asList(partitionName));
        }

        R<SearchResults> response = milvusClient.search(builder.build());
        if (response.getStatus() != R.Status.Success.getCode()) {
            log.error("Search failed: {}", response.getMessage());
            return Lists.newArrayList();
        }

        SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
        List<Document> documents = Lists.newArrayList();

        for (int i = 0; i < wrapper.getIDScore(0).size(); i++) {
            Document document = new Document();
            document.setUniqueId(String.valueOf(wrapper.getIDScore(0).get(i).getLongID()));
            
            List<?> fieldData = wrapper.getFieldData(zillizParam.getFieldNamePageContent(), 0);
            if (fieldData != null && i < fieldData.size()) {
                document.setPageContent(String.valueOf(fieldData.get(i)));
            }
            
            document.setScore((double) wrapper.getIDScore(0).get(i).getScore());
            documents.add(document);
        }

        return documents;
    }

    public void dropCollection() {
        DropCollectionParam param = DropCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        milvusClient.dropCollection(param);
    }

    public void close() {
        if (milvusClient != null) {
            milvusClient.close();
        }
    }
}