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
package com.alibaba.langengine.cassandra.vectorstore;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.data.CqlVector;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Data
public class CassandraService {

    private String keyspace;
    private String tableName;
    private List<String> contactPoints;
    private String localDatacenter;
    private String username;
    private String password;
    private CassandraParam cassandraParam;
    private CqlSession session;
    private ObjectMapper objectMapper;

    public CassandraService(String keyspace,
                           String tableName,
                           List<String> contactPoints,
                           String localDatacenter,
                           String username,
                           String password,
                           CassandraParam cassandraParam) {
        this.keyspace = keyspace;
        this.tableName = tableName;
        this.contactPoints = contactPoints != null ? contactPoints : Arrays.asList("127.0.0.1");
        this.localDatacenter = localDatacenter != null ? localDatacenter : "datacenter1";
        this.username = username;
        this.password = password;
        this.cassandraParam = cassandraParam != null ? cassandraParam : new CassandraParam();
        
        initializeObjectMapper();
        initializeSession();
    }

    private void initializeObjectMapper() {
        try {
            this.objectMapper = new ObjectMapper();
        } catch (NoClassDefFoundError e) {
            log.warn("Jackson ObjectMapper initialization failed, using fallback: {}", e.getMessage());
            this.objectMapper = null;
        }
    }

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private void initializeSession() {
        try {
            List<InetSocketAddress> addresses = contactPoints.stream()
                    .map(cp -> {
                        String[] parts = cp.split(":");
                        if (parts.length == 2) {
                            return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
                        } else {
                            return new InetSocketAddress(parts[0], 9042);
                        }
                    })
                    .collect(Collectors.toList());

            var builder = CqlSession.builder()
                    .addContactPoints(addresses)
                    .withLocalDatacenter(localDatacenter);

            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                builder.withAuthCredentials(username, password);
            }

            this.session = builder.build();
        } catch (Exception e) {
            log.error("Failed to initialize Cassandra session", e);
            throw new RuntimeException("Failed to initialize Cassandra session", e);
        }
    }

    public void init() {
        createKeyspaceIfNotExists();
        createTableIfNotExists();
    }

    private void createKeyspaceIfNotExists() {
        try {
            Optional<KeyspaceMetadata> keyspaceMetadata = session.getMetadata()
                    .getKeyspace(keyspace);

            if (keyspaceMetadata.isPresent()) {
                return;
            }

            String createKeyspaceCql = String.format(
                    "CREATE KEYSPACE IF NOT EXISTS %s WITH replication = " +
                    "{'class': 'SimpleStrategy', 'replication_factor': %d}",
                    keyspace,
                    cassandraParam.getInitParam().getReplicationFactor()
            );

            session.execute(createKeyspaceCql);
            log.info("Created keyspace: {}", keyspace);
        } catch (Exception e) {
            log.error("Failed to create keyspace: {}", keyspace, e);
            throw new RuntimeException("Failed to create keyspace", e);
        }
    }

    private void createTableIfNotExists() {
        try {
            Optional<TableMetadata> tableMetadata = session.getMetadata()
                    .getKeyspace(keyspace)
                    .flatMap(ks -> ks.getTable(tableName));

            if (tableMetadata.isPresent()) {
                return;
            }

            String contentField = cassandraParam.getFieldNamePageContent();
            String idField = cassandraParam.getFieldNameUniqueId();
            String metaField = cassandraParam.getFieldMeta();
            String vectorField = cassandraParam.getFieldNameVector();
            Integer vectorDimensions = cassandraParam.getInitParam().getVectorDimensions();

            String createTableCql = String.format(
                    "CREATE TABLE IF NOT EXISTS %s.%s (" +
                    "%s TEXT PRIMARY KEY, " +
                    "%s TEXT, " +
                    "%s TEXT, " +
                    "%s VECTOR<FLOAT, %d>" +
                    ")",
                    keyspace, tableName,
                    idField,
                    contentField,
                    metaField,
                    vectorField, vectorDimensions
            );

            session.execute(createTableCql);
            log.info("Created table: {}.{}", keyspace, tableName);

            // Create vector index
            createVectorIndex();
        } catch (Exception e) {
            log.error("Failed to create table: {}.{}", keyspace, tableName, e);
            throw new RuntimeException("Failed to create table", e);
        }
    }

    private void createVectorIndex() {
        try {
            String vectorField = cassandraParam.getFieldNameVector();
            String indexName = tableName + "_" + vectorField + "_idx";
            String similarityFunction = cassandraParam.getInitParam().getVectorSimilarityFunction();

            String createIndexCql = String.format(
                    "CREATE CUSTOM INDEX IF NOT EXISTS %s ON %s.%s (%s) " +
                    "USING 'StorageAttachedIndex' " +
                    "WITH OPTIONS = {'similarity_function': '%s'}",
                    indexName, keyspace, tableName, vectorField, similarityFunction
            );

            session.execute(createIndexCql);
            log.info("Created vector index: {}", indexName);
        } catch (Exception e) {
            log.warn("Failed to create vector index, continuing without it: {}", e.getMessage());
        }
    }

    public void addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        try {
            for (Document document : documents) {
                addDocument(document);
            }
        } catch (Exception e) {
            log.error("Failed to add documents to Cassandra", e);
            throw new RuntimeException("Failed to add documents to Cassandra", e);
        }
    }

    private void addDocument(Document document) {
        String documentId = generateDocumentId(document);
        String contentField = cassandraParam.getFieldNamePageContent();
        String idField = cassandraParam.getFieldNameUniqueId();
        String metaField = cassandraParam.getFieldMeta();
        String vectorField = cassandraParam.getFieldNameVector();

        try {
            // Prepare metadata as JSON string
            String metadataJson = null;
            if (document.getMetadata() != null) {
                ObjectMapper mapper = getObjectMapper();
                if (mapper != null) {
                    metadataJson = mapper.writeValueAsString(document.getMetadata());
                } else {
                    metadataJson = JSON.toJSONString(document.getMetadata());
                }
            }

            // Convert embedding to CqlVector
            CqlVector<Float> vectorValue = null;
            if (document.getEmbedding() != null && !document.getEmbedding().isEmpty()) {
                List<Float> embedding = parseEmbedding(document.getEmbedding());
                if (embedding != null) {
                    vectorValue = CqlVector.newInstance(embedding);
                }
            }

            // Build insert statement with all values
            Insert insert = QueryBuilder.insertInto(keyspace, tableName)
                    .value(idField, literal(documentId))
                    .value(contentField, literal(document.getPageContent()));

            // Add metadata if present
            if (metadataJson != null) {
                insert = QueryBuilder.insertInto(keyspace, tableName)
                        .value(idField, literal(documentId))
                        .value(contentField, literal(document.getPageContent()))
                        .value(metaField, literal(metadataJson));
            }

            // Add vector if present
            if (vectorValue != null) {
                if (metadataJson != null) {
                    insert = QueryBuilder.insertInto(keyspace, tableName)
                            .value(idField, literal(documentId))
                            .value(contentField, literal(document.getPageContent()))
                            .value(metaField, literal(metadataJson))
                            .value(vectorField, literal(vectorValue));
                } else {
                    insert = QueryBuilder.insertInto(keyspace, tableName)
                            .value(idField, literal(documentId))
                            .value(contentField, literal(document.getPageContent()))
                            .value(vectorField, literal(vectorValue));
                }
            }

            session.execute(insert.build());
            log.debug("Added document: {}", documentId);
        } catch (Exception e) {
            log.error("Failed to add document: {}", documentId, e);
            throw new RuntimeException("Failed to add document", e);
        }
    }

    private String generateDocumentId(Document document) {
        if (document.getUniqueId() != null) {
            return document.getUniqueId();
        }
        return UUID.randomUUID().toString();
    }

    private List<Float> parseEmbedding(List<Double> embeddingList) {
        try {
            if (embeddingList == null || embeddingList.isEmpty()) {
                return null;
            }
            return embeddingList.stream()
                    .map(Double::floatValue)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse embedding", e);
            return null;
        }
    }

    public List<Document> similaritySearch(List<Float> queryVector, int k, Double maxDistanceValue, Integer type) {
        try {
            String vectorField = cassandraParam.getFieldNameVector();
            String contentField = cassandraParam.getFieldNamePageContent();
            String idField = cassandraParam.getFieldNameUniqueId();
            String metaField = cassandraParam.getFieldMeta();

            CqlVector<Float> queryVectorCql = CqlVector.newInstance(queryVector);

            String cql = String.format(
                    "SELECT %s, %s, %s, similarity_%s(%s, ?) as similarity " +
                    "FROM %s.%s " +
                    "ORDER BY %s ANN OF ? " +
                    "LIMIT ?",
                    idField, contentField, metaField,
                    cassandraParam.getInitParam().getVectorSimilarityFunction(),
                    vectorField,
                    keyspace, tableName,
                    vectorField
            );

            PreparedStatement prepared = session.prepare(cql);
            ResultSet resultSet = session.execute(prepared.bind(queryVectorCql, queryVectorCql, k));

            List<Document> documents = new ArrayList<>();
            for (Row row : resultSet) {
                Document document = new Document();
                document.setUniqueId(row.getString(idField));
                document.setPageContent(row.getString(contentField));

                String metadataJson = row.getString(metaField);
                if (StringUtils.isNotBlank(metadataJson)) {
                    Map<String, Object> metadata = parseMetadata(metadataJson);
                    document.setMetadata(metadata);
                }

                // Add similarity score
                try {
                    double similarity = row.getDouble("similarity");
                    if (document.getMetadata() == null) {
                        document.setMetadata(new HashMap<>());
                    }
                    document.getMetadata().put("similarity", similarity);
                } catch (Exception e) {
                    log.debug("No similarity score available: {}", e.getMessage());
                }

                documents.add(document);
            }

            return documents;
        } catch (Exception e) {
            log.error("Failed to perform similarity search", e);
            throw new RuntimeException("Failed to perform similarity search", e);
        }
    }

    private Map<String, Object> parseMetadata(String metadataJson) {
        try {
            ObjectMapper mapper = getObjectMapper();
            if (mapper != null) {
                return mapper.readValue(metadataJson, Map.class);
            } else {
                return JSON.parseObject(metadataJson, Map.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse metadata JSON: {}", metadataJson, e);
            return new HashMap<>();
        }
    }

    public void close() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception e) {
            log.error("Failed to close Cassandra session", e);
        }
    }
}
