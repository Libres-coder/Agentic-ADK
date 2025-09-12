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
package com.alibaba.langengine.elasticsearch.vectorstore;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.alibaba.langengine.core.indexes.Document;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
public class ElasticsearchService {

    private final String indexName;
    private final ElasticsearchClient client;
    private final ElasticsearchParam elasticsearchParam;

    public ElasticsearchService(String serverUrl, String indexName, ElasticsearchParam elasticsearchParam) {
        this(serverUrl, null, null, null, indexName, elasticsearchParam);
    }

    public ElasticsearchService(String serverUrl, String username, String password, String apiKey, 
                               String indexName, ElasticsearchParam elasticsearchParam) {
        this.indexName = indexName;
        this.elasticsearchParam = elasticsearchParam != null ? elasticsearchParam : new ElasticsearchParam();
        this.client = createClient(serverUrl, username, password, apiKey);
    }


    private ElasticsearchClient createClient(String serverUrl, String username, String password, String apiKey) {
        try {
            HttpHost httpHost = HttpHost.create(serverUrl);
            RestClientBuilder builder = RestClient.builder(httpHost);

            // Configure authentication
            if (username != null && password != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password)
                );
                builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                );
            } else if (apiKey != null) {
                builder.setDefaultHeaders(new org.apache.http.Header[]{
                    new org.apache.http.message.BasicHeader("Authorization", "ApiKey " + apiKey)
                });
            }

            RestClient restClient = builder.build();
            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            log.error("Failed to create Elasticsearch client: {}", e.getMessage());
            throw new RuntimeException("Failed to create Elasticsearch client", e);
        }
    }


    public void init() throws IOException {
        if (!indexExists()) {
            createIndex();
        }
    }


    private boolean indexExists() throws IOException {
        try {
            ExistsRequest request = ExistsRequest.of(builder -> builder.index(indexName));
            return client.indices().exists(request).value();
        } catch (ElasticsearchException e) {
            log.warn("Error checking if index exists: {}", e.getMessage());
            return false;
        }
    }


    private void createIndex() throws IOException {
        ElasticsearchParam.IndexParam indexParam = elasticsearchParam.getIndexParam();
        
        // Define properties
        Map<String, Property> properties = new HashMap<>();
        
        // Text field for page content
        properties.put(elasticsearchParam.getFieldNamePageContent(), 
            Property.of(p -> p.text(TextProperty.of(tp -> tp.analyzer("standard")))));
        
        // Keyword field for unique ID
        properties.put(elasticsearchParam.getFieldNameUniqueId(), 
            Property.of(p -> p.keyword(kp -> kp)));
        
        // Dense vector field for embeddings
        DenseVectorProperty.Builder vectorPropertyBuilder = new DenseVectorProperty.Builder()
            .dims(indexParam.getVectorDimension())
            .similarity(indexParam.getVectorSimilarity());
            
        if ("hnsw".equals(indexParam.getVectorIndexType())) {
            vectorPropertyBuilder.index(true)
                .indexOptions(io -> io.type("hnsw"));
        } else {
            vectorPropertyBuilder.index(false);
        }
        
        properties.put(elasticsearchParam.getFieldNameVector(),
            Property.of(p -> p.denseVector(vectorPropertyBuilder.build())));
        
        // Object field for metadata
        properties.put(elasticsearchParam.getFieldNameMetadata(),
            Property.of(p -> p.object(op -> op)));

        TypeMapping mapping = TypeMapping.of(tm -> tm.properties(properties));

        CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
            .index(indexName)
            .mappings(mapping)
            .settings(is -> is
                .numberOfShards(String.valueOf(indexParam.getNumberOfShards()))
                .numberOfReplicas(String.valueOf(indexParam.getNumberOfReplicas()))
            )
        );

        CreateIndexResponse response = client.indices().create(request);
        if (response.acknowledged()) {
            log.info("Successfully created index: {}", indexName);
        } else {
            log.warn("Index creation not acknowledged: {}", indexName);
        }
    }


    public void addDocuments(List<Document> documents) throws IOException {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        
        for (Document document : documents) {
            Map<String, Object> docMap = new HashMap<>();
            
            if (document.getPageContent() != null) {
                docMap.put(elasticsearchParam.getFieldNamePageContent(), document.getPageContent());
            }
            
            if (document.getUniqueId() != null) {
                docMap.put(elasticsearchParam.getFieldNameUniqueId(), document.getUniqueId());
            }
            
            if (document.getEmbedding() != null && !document.getEmbedding().isEmpty()) {
                docMap.put(elasticsearchParam.getFieldNameVector(), document.getEmbedding());
            }
            
            if (document.getMetadata() != null && !document.getMetadata().isEmpty()) {
                docMap.put(elasticsearchParam.getFieldNameMetadata(), document.getMetadata());
            }

            String id = document.getUniqueId() != null ? document.getUniqueId() : UUID.randomUUID().toString();
            
            IndexOperation<Object> indexOp = IndexOperation.of(io -> io
                .index(indexName)
                .id(id)
                .document(docMap)
            );
            
            bulkBuilder.operations(BulkOperation.of(bo -> bo.index(indexOp)));
        }

        if (elasticsearchParam.getIndexParam().isRefreshAfterWrite()) {
            bulkBuilder.refresh(Refresh.True);
        }

        BulkResponse response = client.bulk(bulkBuilder.build());
        
        if (response.errors()) {
            log.error("Bulk indexing had errors");
            response.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("Error indexing document: {}", item.error().reason());
                }
            });
        } else {
            log.debug("Successfully indexed {} documents", documents.size());
        }
    }


    public List<Document> similaritySearch(List<Float> queryVector, int k, Double maxDistanceValue, Integer type) 
            throws IOException {
        if (queryVector == null || queryVector.isEmpty()) {
            return Collections.emptyList();
        }

        SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
            .index(indexName)
            .size(k);

        // Build KNN query for vector similarity
        Query knnQuery = Query.of(q -> q.knn(knn -> {
            knn.field(elasticsearchParam.getFieldNameVector())
               .queryVector(queryVector)
               .numCandidates(k * 2);
            
            if (maxDistanceValue != null) {
                // Convert distance to similarity score if needed
                // For cosine similarity: similarity = 1 - distance
                // For dot product: similarity = distance (assuming normalized vectors)
                // For l2_norm: similarity = 1 / (1 + distance)
                String similarity = elasticsearchParam.getIndexParam().getVectorSimilarity();
                if ("cosine".equals(similarity)) {
                    knn.similarity(Math.max(0.0f, (float)(1.0 - maxDistanceValue)));
                } else if ("l2_norm".equals(similarity)) {
                    knn.similarity(Math.max(0.0f, (float)(1.0 / (1.0 + maxDistanceValue))));
                } else {
                    knn.similarity(maxDistanceValue.floatValue());
                }
            }
            
            return knn;
        }));

        searchBuilder.query(knnQuery);

        SearchResponse<Map> response = client.search(searchBuilder.build(), Map.class);
        
        List<Document> documents = new ArrayList<>();
        for (Hit<Map> hit : response.hits().hits()) {
            Map<String, Object> source = hit.source();
            if (source == null) continue;

            Document document = new Document();
            
            Object uniqueId = source.get(elasticsearchParam.getFieldNameUniqueId());
            if (uniqueId != null) {
                document.setUniqueId(String.valueOf(uniqueId));
            }
            
            Object pageContent = source.get(elasticsearchParam.getFieldNamePageContent());
            if (pageContent != null) {
                document.setPageContent(String.valueOf(pageContent));
            }
            
            Object metadata = source.get(elasticsearchParam.getFieldNameMetadata());
            if (metadata instanceof Map) {
                document.setMetadata((Map<String, Object>) metadata);
            }
            
            // Set similarity score
            if (hit.score() != null) {
                document.setScore(hit.score().doubleValue());
            }
            
            documents.add(document);
        }
        
        return documents;
    }


    public void deleteIndex() throws IOException {
        try {
            client.indices().delete(builder -> builder.index(indexName));
            log.info("Successfully deleted index: {}", indexName);
        } catch (ElasticsearchException e) {
            log.warn("Error deleting index {}: {}", indexName, e.getMessage());
        }
    }


    public void close() throws IOException {
        if (client != null) {
            client._transport().close();
        }
    }
}
