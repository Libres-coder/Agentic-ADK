/**
 * Copyright (C) 2024import org.apache.hugegraph.define.DataType;
import org.apache.hugegraph.structure.schema.EdgeLabel;
import org.apache.hugegraph.structure.schema.IndexLabel;
import org.apache.hugegraph.structure.schema.PropertyKey;
import org.apache.hugegraph.structure.schema.VertexLabel;DC-AI
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
package com.alibaba.langengine.hugegraph.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.hugegraph.exception.HugeGraphVectorStoreException;
import com.alibaba.langengine.hugegraph.vectorstore.HugeGraphParam;
import org.apache.hugegraph.api.gremlin.GremlinRequest;
import org.apache.hugegraph.driver.GremlinManager;
import org.apache.hugegraph.driver.HugeClient;
import org.apache.hugegraph.driver.SchemaManager;
import org.apache.hugegraph.structure.constant.DataType;
import org.apache.hugegraph.structure.gremlin.ResultSet;
import org.apache.hugegraph.structure.schema.EdgeLabel;
import org.apache.hugegraph.structure.schema.IndexLabel;
import org.apache.hugegraph.structure.schema.PropertyKey;
import org.apache.hugegraph.structure.schema.VertexLabel;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class HugeGraphClient implements AutoCloseable {

    private final HugeClient hugeClient;
    private final HugeGraphParam.ServerConfig serverConfig;
    private final Map<String, SchemaManager> schemaManagerCache = new ConcurrentHashMap<>();
    private final Map<String, GremlinManager> gremlinManagerCache = new ConcurrentHashMap<>();

    public HugeGraphClient(HugeGraphParam.ServerConfig serverConfig, HugeGraphParam.ConnectionConfig connectionConfig) {
        this.serverConfig = serverConfig;
        try {
            this.hugeClient = HugeClient.builder(serverConfig.getFullUrl(), serverConfig.getGraph())
                    .configUser(serverConfig.getUsername(), serverConfig.getPassword())
                    .configTimeout(connectionConfig.getConnectionTimeout())
                    .configSSL(serverConfig.getTrustStoreFile(), serverConfig.getTrustStorePassword())
                    .build();
        } catch (Exception e) {
            throw HugeGraphVectorStoreException.connectionError("Failed to create HugeClient", e);
        }
    }

    /**
     * 获取Schema管理器
     */
    public SchemaManager getSchemaManager() {
        return schemaManagerCache.computeIfAbsent(serverConfig.getGraph(), g -> hugeClient.schema());
    }

    /**
     * 获取Gremlin管理器
     */
    public GremlinManager getGremlinManager() {
        return gremlinManagerCache.computeIfAbsent(serverConfig.getGraph(), g -> hugeClient.gremlin());
    }

    /**
     * 执行Gremlin查询
     */
    public ResultSet executeGremlin(String gremlin) {
        try {
            GremlinRequest request = new GremlinRequest(gremlin);
            return getGremlinManager().execute(request);
        } catch (Exception e) {
            log.error("Failed to execute Gremlin query: {}", gremlin, e);
            throw HugeGraphVectorStoreException.graphOperationFailed("Gremlin query execution failed", e);
        }
    }

    /**
     * 执行带参数绑定的Gremlin查询（安全方式，避免注入攻击）
     */
    public ResultSet executeGremlinWithBindings(String gremlin, Map<String, Object> bindings) {
        try {
            GremlinRequest request = new GremlinRequest(gremlin);
            if (bindings != null && !bindings.isEmpty()) {
                request.bindings(bindings);
            }
            return getGremlinManager().execute(request);
        } catch (Exception e) {
            log.error("Failed to execute Gremlin query with bindings: {}", gremlin, e);
            throw HugeGraphVectorStoreException.graphOperationFailed("Gremlin query execution failed", e);
        }
    }

    /**
     * 创建属性键（如果不存在）
     */
    public void createPropertyKeyIfNotExists(String name, Class<?> dataType) {
        SchemaManager schema = getSchemaManager();
        if (!schema.getPropertyKeys().stream().anyMatch(p -> p.name().equals(name))) {
            log.info("Creating property key: {}", name);
            DataType hugegraphDataType = convertToDataType(dataType);
            schema.propertyKey(name).dataType(hugegraphDataType).ifNotExist().create();
        }
    }

    /**
     * 创建顶点标签（如果不存在）
     */
    public void createVertexLabelIfNotExists(String name, String... properties) {
        SchemaManager schema = getSchemaManager();
        if (!schema.getVertexLabels().stream().anyMatch(v -> v.name().equals(name))) {
            log.info("Creating vertex label: {}", name);
            VertexLabel vl = schema.vertexLabel(name).properties(properties).ifNotExist().create();
        }
    }

    /**
     * 创建边标签（如果不存在）
     */
    public void createEdgeLabelIfNotExists(String name, String sourceLabel, String targetLabel, String... properties) {
        SchemaManager schema = getSchemaManager();
        if (!schema.getEdgeLabels().stream().anyMatch(e -> e.name().equals(name))) {
            log.info("Creating edge label: {}", name);
            schema.edgeLabel(name)
                  .sourceLabel(sourceLabel)
                  .targetLabel(targetLabel)
                  .properties(properties)
                  .ifNotExist()
                  .create();
        }
    }

    /**
     * 创建索引（如果不存在）
     */
    public void createIndexIfNotExists(String name, String baseType, String baseValue, String... fields) {
        SchemaManager schema = getSchemaManager();
        if (!schema.getIndexLabels().stream().anyMatch(i -> i.name().equals(name))) {
            log.info("Creating index: {} on {}={} for fields: {}", name, baseType, baseValue, String.join(",", fields));
            try {
                // 使用正确的API调用方式
                if ("vertex".equals(baseType) || "VERTEX_LABEL".equals(baseType)) {
                    schema.indexLabel(name)
                          .onV(baseValue)  // baseValue应该是顶点标签名称
                          .by(fields)
                          .ifNotExist()
                          .create();
                } else if ("edge".equals(baseType) || "EDGE_LABEL".equals(baseType)) {
                    schema.indexLabel(name)
                          .onE(baseValue)  // baseValue应该是边标签名称
                          .by(fields)
                          .ifNotExist()
                          .create();
                } else {
                    log.warn("Unsupported index base type: {}. Supported types are 'vertex' and 'edge'", baseType);
                }
            } catch (Exception e) {
                log.error("Failed to create index '{}': {}", name, e.getMessage());
                throw HugeGraphVectorStoreException.graphOperationFailed("Failed to create index: " + name, e);
            }
        } else {
            log.debug("Index '{}' already exists", name);
        }
    }

    /**
     * 检查图是否存在
     */
    public boolean graphExists() {
        try {
            // A simple way to check is to get schema, which will fail if graph doesn't exist.
            getSchemaManager().getVertexLabels();
            return true;
        } catch (Exception e) {
            // More specific exception handling might be needed based on HugeGraph client behavior
            log.warn("Graph '{}' may not exist or is not accessible: {}", serverConfig.getGraph(), e.getMessage());
            return false;
        }
    }

    /**
     * 获取所有属性键
     */
    public List<PropertyKey> getPropertyKeys() {
        return getSchemaManager().getPropertyKeys();
    }

    /**
     * 获取所有顶点标签
     */
    public List<VertexLabel> getVertexLabels() {
        return getSchemaManager().getVertexLabels();
    }

    /**
     * 获取所有边标签
     */
    public List<EdgeLabel> getEdgeLabels() {
        return getSchemaManager().getEdgeLabels();
    }

    /**
     * 获取所有索引标签
     */
    public List<IndexLabel> getIndexLabels() {
        return getSchemaManager().getIndexLabels();
    }

    /**
     * 将Java类型转换为HugeGraph DataType
     */
    private DataType convertToDataType(Class<?> javaType) {
        if (String.class.equals(javaType)) {
            return DataType.TEXT;
        } else if (Integer.class.equals(javaType) || int.class.equals(javaType)) {
            return DataType.INT;
        } else if (Long.class.equals(javaType) || long.class.equals(javaType)) {
            return DataType.LONG;
        } else if (Double.class.equals(javaType) || double.class.equals(javaType)) {
            return DataType.DOUBLE;
        } else if (Float.class.equals(javaType) || float.class.equals(javaType)) {
            return DataType.FLOAT;
        } else if (Boolean.class.equals(javaType) || boolean.class.equals(javaType)) {
            return DataType.BOOLEAN;
        } else {
            // 默认使用TEXT类型
            return DataType.TEXT;
        }
    }

    @Override
    public void close() {
        try {
            if (hugeClient != null) {
                hugeClient.close();
                log.info("HugeGraph client closed successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to close HugeGraph client", e);
        }
    }
}
