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
package com.alibaba.langengine.neo4j.vectorstore;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class Neo4jParam {

    /**
     * 节点标签名称
     */
    private String nodeLabel = "Document";

    /**
     * 向量索引名称
     */
    private String vectorIndexName = "vector_index";

    /**
     * 文档ID字段名
     */
    private String fieldNameUniqueId = "id";

    /**
     * 文档内容字段名
     */
    private String fieldNamePageContent = "content";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "embedding";

    /**
     * 元数据字段名
     */
    private String fieldNameMetadata = "metadata";

    /**
     * 向量相似性搜索参数
     */
    private Map<String, Object> searchParams = new HashMap<String, Object>() {{
        put("ef", 64);  // HNSW search parameter
    }};

    /**
     * 初始化参数
     */
    private InitParam initParam = new InitParam();

    @Data
    public static class InitParam {

        /**
         * 向量维度
         */
        private int vectorDimensions = 1536;

        /**
         * 相似性度量方式
         */
        private Neo4jSimilarityFunction similarityFunction = Neo4jSimilarityFunction.COSINE;

        /**
         * HNSW索引参数 - M值（每个节点的最大连接数）
         * 注意：此参数为Neo4j GDS库预留，原生向量索引暂不支持
         */
        private int hnswM = 16;

        /**
         * HNSW索引参数 - efConstruction值（构建时的搜索候选数）
         * 注意：此参数为Neo4j GDS库预留，原生向量索引暂不支持
         */
        private int hnswEfConstruction = 200;

        /**
         * 是否自动创建向量索引
         */
        private boolean autoCreateIndex = true;

        /**
         * 是否在节点不存在时自动创建
         */
        private boolean autoCreateNode = true;

        /**
         * 批量插入大小
         */
        private int batchSize = 1000;

        /**
         * 连接超时时间（秒）
         */
        private int connectionTimeoutSeconds = 30;

        /**
         * 最大连接池大小
         */
        private int maxConnectionPoolSize = 100;

    }

}
