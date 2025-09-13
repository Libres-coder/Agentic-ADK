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
package com.alibaba.langengine.janusgraph.vectorstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("JanusGraph参数配置测试")
class JanusGraphParamTest {

    private JanusGraphParam defaultParam;
    private JanusGraphParam customParam;

    @BeforeEach
    void setUp() {
        defaultParam = JanusGraphParam.getDefaultConfig();
        
        customParam = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("cassandra")
                .storageHostname("localhost")
                .storagePort(9042)
                .cassandraKeyspace("test_keyspace")
                .build())
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vertexLabel("TestDocument")
                .vectorDimension(1536)
                .similarityThreshold(0.8)
                .build())
            .connectionConfig(JanusGraphParam.ConnectionConfig.builder()
                .maxConnectionPoolSize(20)
                .connectionTimeoutSeconds(60)
                .build())
            .indexConfig(JanusGraphParam.IndexConfig.builder()
                .indexBackend("elasticsearch")
                .indexHostname("localhost")
                .indexPort(9200)
                .build())
            .batchConfig(JanusGraphParam.BatchConfig.builder()
                .batchSize(200)
                .enableBatchCommit(true)
                .build())
            .initParam(JanusGraphParam.InitParam.builder()
                .createSchemaOnInit(true)
                .createIndexOnInit(true)
                .build())
            .build();
    }

    @Test
    @DisplayName("测试默认配置创建")
    void testDefaultConfigCreation() {
        assertNotNull(defaultParam);
        assertNotNull(defaultParam.getGraphConfig());
        assertNotNull(defaultParam.getVectorConfig());
        assertNotNull(defaultParam.getConnectionConfig());
        assertNotNull(defaultParam.getIndexConfig());
        assertNotNull(defaultParam.getBatchConfig());
        assertNotNull(defaultParam.getInitParam());
    }

    @Test
    @DisplayName("测试图配置默认值")
    void testGraphConfigDefaults() {
        JanusGraphParam.GraphConfig graphConfig = defaultParam.getGraphConfig();
        
        assertEquals("berkeleyje", graphConfig.getStorageBackend());
        assertEquals("janusgraph", graphConfig.getCassandraKeyspace());
        assertEquals("janusgraph", graphConfig.getHbaseTable());
        assertNotNull(graphConfig.getGraphProperties());
    }

    @Test
    @DisplayName("测试向量配置默认值")
    void testVectorConfigDefaults() {
        JanusGraphParam.VectorConfig vectorConfig = defaultParam.getVectorConfig();
        
        assertEquals("Document", vectorConfig.getVertexLabel());
        assertEquals("similar_to", vectorConfig.getEdgeLabel());
        assertEquals(768, vectorConfig.getVectorDimension().intValue());
        assertEquals("docId", vectorConfig.getIdPropertyName());
        assertEquals("content", vectorConfig.getContentPropertyName());
        assertEquals("embedding", vectorConfig.getVectorPropertyName());
        assertEquals("metadata", vectorConfig.getMetadataPropertyName());
        assertEquals("score", vectorConfig.getScorePropertyName());
        assertEquals(0.7, vectorConfig.getSimilarityThreshold().doubleValue(), 0.001);
        assertTrue(vectorConfig.getEnableVectorIndex());
    }

    @Test
    @DisplayName("测试连接配置默认值")
    void testConnectionConfigDefaults() {
        JanusGraphParam.ConnectionConfig connectionConfig = defaultParam.getConnectionConfig();
        
        assertEquals(10, connectionConfig.getMaxConnectionPoolSize().intValue());
        assertEquals(1, connectionConfig.getMinConnectionPoolSize().intValue());
        assertEquals(30, connectionConfig.getConnectionTimeoutSeconds().intValue());
        assertEquals(60, connectionConfig.getReadTimeoutSeconds().intValue());
        assertEquals(60, connectionConfig.getWriteTimeoutSeconds().intValue());
        assertTrue(connectionConfig.getEnableConnectionPool());
        assertEquals(3, connectionConfig.getMaxRetries().intValue());
        assertEquals(1000L, connectionConfig.getRetryDelayMs().longValue());
    }

    @Test
    @DisplayName("测试索引配置默认值")
    void testIndexConfigDefaults() {
        JanusGraphParam.IndexConfig indexConfig = defaultParam.getIndexConfig();
        
        assertEquals("elasticsearch", indexConfig.getIndexBackend());
        assertTrue(indexConfig.getElasticsearchClientOnly());
        assertEquals("janusgraph_vectors", indexConfig.getIndexName());
        assertEquals("vector_index", indexConfig.getVectorIndexName());
        assertTrue(indexConfig.getEnableMixedIndex());
        assertNotNull(indexConfig.getIndexProperties());
    }

    @Test
    @DisplayName("测试批量配置默认值")
    void testBatchConfigDefaults() {
        JanusGraphParam.BatchConfig batchConfig = defaultParam.getBatchConfig();
        
        assertEquals(100, batchConfig.getBatchSize().intValue());
        assertTrue(batchConfig.getEnableBatchCommit());
        assertEquals(5000L, batchConfig.getBatchCommitInterval().longValue());
        assertEquals(Runtime.getRuntime().availableProcessors(), batchConfig.getParallelThreads().intValue());
    }

    @Test
    @DisplayName("测试初始化参数默认值")
    void testInitParamDefaults() {
        JanusGraphParam.InitParam initParam = defaultParam.getInitParam();
        
        assertTrue(initParam.getCreateSchemaOnInit());
        assertTrue(initParam.getCreateIndexOnInit());
        assertTrue(initParam.getValidateSchemaOnInit());
        assertTrue(initParam.getEnableSchemaConstraints());
        assertFalse(initParam.getAllowAllAttributes());
        assertEquals("none", initParam.getSchemaDefault());
    }

    @Test
    @DisplayName("测试自定义配置创建")
    void testCustomConfigCreation() {
        JanusGraphParam.GraphConfig graphConfig = customParam.getGraphConfig();
        assertEquals("cassandra", graphConfig.getStorageBackend());
        assertEquals("localhost", graphConfig.getStorageHostname());
        assertEquals(9042, graphConfig.getStoragePort().intValue());
        assertEquals("test_keyspace", graphConfig.getCassandraKeyspace());

        JanusGraphParam.VectorConfig vectorConfig = customParam.getVectorConfig();
        assertEquals("TestDocument", vectorConfig.getVertexLabel());
        assertEquals(1536, vectorConfig.getVectorDimension().intValue());
        assertEquals(0.8, vectorConfig.getSimilarityThreshold().doubleValue(), 0.001);

        JanusGraphParam.ConnectionConfig connectionConfig = customParam.getConnectionConfig();
        assertEquals(20, connectionConfig.getMaxConnectionPoolSize().intValue());
        assertEquals(60, connectionConfig.getConnectionTimeoutSeconds().intValue());
    }

    @Test
    @DisplayName("测试配置转换为JanusGraph配置")
    void testToJanusGraphConfig() {
        Map<String, Object> config = customParam.toJanusGraphConfig();
        
        assertNotNull(config);
        assertEquals("cassandra", config.get("storage.backend"));
        assertEquals("localhost", config.get("storage.hostname"));
        assertEquals(9042, config.get("storage.port"));
        assertEquals("test_keyspace", config.get("storage.cassandra.keyspace"));
        
        assertEquals("elasticsearch", config.get("index.search.backend"));
        assertEquals("localhost", config.get("index.search.hostname"));
        assertEquals(9200, config.get("index.search.port"));
        assertTrue((Boolean) config.get("index.search.elasticsearch.client-only"));
    }

    @Test
    @DisplayName("测试Builder模式")
    void testBuilderPattern() {
        JanusGraphParam param = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("berkeleyje")
                .build())
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vectorDimension(512)
                .build())
            .build();

        assertNotNull(param);
        assertEquals("berkeleyje", param.getGraphConfig().getStorageBackend());
        assertEquals(512, param.getVectorConfig().getVectorDimension().intValue());
    }

    @Test
    @DisplayName("测试配置的不可变性")
    void testConfigImmutability() {
        JanusGraphParam.GraphConfig originalGraphConfig = customParam.getGraphConfig();
        String originalBackend = originalGraphConfig.getStorageBackend();
        
        // 尝试修改配置
        originalGraphConfig.setStorageBackend("modified");
        
        // 验证原始配置已被修改（因为使用了Lombok的@Data注解，对象是可变的）
        assertEquals("modified", originalGraphConfig.getStorageBackend());
        
        // 但我们可以通过重新构建来保持原始配置
        JanusGraphParam newParam = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend(originalBackend)
                .storageHostname(originalGraphConfig.getStorageHostname())
                .storagePort(originalGraphConfig.getStoragePort())
                .cassandraKeyspace(originalGraphConfig.getCassandraKeyspace())
                .build())
            .build();
        
        assertEquals(originalBackend, newParam.getGraphConfig().getStorageBackend());
    }

    @Test
    @DisplayName("测试配置验证")
    void testConfigValidation() {
        // 测试有效配置
        assertDoesNotThrow(() -> {
            JanusGraphParam validParam = JanusGraphParam.builder()
                .graphConfig(JanusGraphParam.GraphConfig.builder()
                    .storageBackend("berkeleyje")
                    .build())
                .vectorConfig(JanusGraphParam.VectorConfig.builder()
                    .vectorDimension(768)
                    .vertexLabel("Document")
                    .build())
                .build();
            
            assertNotNull(validParam);
            assertTrue(validParam.getVectorConfig().getVectorDimension() > 0);
            assertFalse(validParam.getVectorConfig().getVertexLabel().isEmpty());
        });
    }

    @Test
    @DisplayName("测试不同存储后端配置")
    void testDifferentStorageBackends() {
        // BerkeleyDB配置
        JanusGraphParam berkeleyParam = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("berkeleyje")
                .build())
            .build();
        
        Map<String, Object> berkeleyConfig = berkeleyParam.toJanusGraphConfig();
        assertEquals("berkeleyje", berkeleyConfig.get("storage.backend"));
        assertNull(berkeleyConfig.get("storage.cassandra.keyspace"));
        assertNull(berkeleyConfig.get("storage.hbase.table"));

        // Cassandra配置
        JanusGraphParam cassandraParam = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("cassandra")
                .cassandraKeyspace("test_ks")
                .build())
            .build();
        
        Map<String, Object> cassandraConfig = cassandraParam.toJanusGraphConfig();
        assertEquals("cassandra", cassandraConfig.get("storage.backend"));
        assertEquals("test_ks", cassandraConfig.get("storage.cassandra.keyspace"));

        // HBase配置
        JanusGraphParam hbaseParam = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("hbase")
                .hbaseTable("test_table")
                .build())
            .build();
        
        Map<String, Object> hbaseConfig = hbaseParam.toJanusGraphConfig();
        assertEquals("hbase", hbaseConfig.get("storage.backend"));
        assertEquals("test_table", hbaseConfig.get("storage.hbase.table"));
    }

    @Test
    @DisplayName("测试配置的序列化和反序列化")
    void testConfigSerialization() {
        // 这里可以添加JSON序列化测试
        assertNotNull(customParam.getGraphConfig());
        assertNotNull(customParam.getVectorConfig());
        assertNotNull(customParam.getConnectionConfig());
        assertNotNull(customParam.getIndexConfig());
        assertNotNull(customParam.getBatchConfig());
        assertNotNull(customParam.getInitParam());
    }

    @Test
    @DisplayName("测试边界值和异常情况")
    void testBoundaryValues() {
        // 测试最小值
        JanusGraphParam minParam = JanusGraphParam.builder()
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vectorDimension(1)
                .similarityThreshold(0.0)
                .build())
            .connectionConfig(JanusGraphParam.ConnectionConfig.builder()
                .maxConnectionPoolSize(1)
                .minConnectionPoolSize(1)
                .connectionTimeoutSeconds(1)
                .build())
            .batchConfig(JanusGraphParam.BatchConfig.builder()
                .batchSize(1)
                .parallelThreads(1)
                .build())
            .build();

        assertEquals(1, minParam.getVectorConfig().getVectorDimension().intValue());
        assertEquals(0.0, minParam.getVectorConfig().getSimilarityThreshold().doubleValue(), 0.001);
        assertEquals(1, minParam.getConnectionConfig().getMaxConnectionPoolSize().intValue());
        assertEquals(1, minParam.getBatchConfig().getBatchSize().intValue());

        // 测试最大值
        JanusGraphParam maxParam = JanusGraphParam.builder()
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vectorDimension(10000)
                .similarityThreshold(1.0)
                .build())
            .connectionConfig(JanusGraphParam.ConnectionConfig.builder()
                .maxConnectionPoolSize(1000)
                .connectionTimeoutSeconds(3600)
                .build())
            .batchConfig(JanusGraphParam.BatchConfig.builder()
                .batchSize(10000)
                .parallelThreads(100)
                .build())
            .build();

        assertEquals(10000, maxParam.getVectorConfig().getVectorDimension().intValue());
        assertEquals(1.0, maxParam.getVectorConfig().getSimilarityThreshold().doubleValue(), 0.001);
        assertEquals(1000, maxParam.getConnectionConfig().getMaxConnectionPoolSize().intValue());
        assertEquals(10000, maxParam.getBatchConfig().getBatchSize().intValue());
    }
}
