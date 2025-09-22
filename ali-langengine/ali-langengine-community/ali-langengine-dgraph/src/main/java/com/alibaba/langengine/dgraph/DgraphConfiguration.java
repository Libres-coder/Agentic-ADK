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
package com.alibaba.langengine.dgraph;


public class DgraphConfiguration {

    /**
     * Dgraph 服务器地址
     */
    public static String DGRAPH_SERVER_URL = System.getProperty("dgraph.server.url", "localhost:9080");

    /**
     * Dgraph 默认超时时间（毫秒）
     */
    public static int DGRAPH_TIMEOUT_MS = Integer.parseInt(System.getProperty("dgraph.timeout.ms", "30000"));

    /**
     * Dgraph 连接池最大连接数
     */
    public static int DGRAPH_MAX_CONNECTIONS = Integer.parseInt(System.getProperty("dgraph.max.connections", "10"));

    /**
     * Dgraph 默认向量维度
     */
    public static int DGRAPH_DEFAULT_VECTOR_DIMENSION = Integer.parseInt(System.getProperty("dgraph.vector.dimension", "1536"));

    /**
     * Dgraph 向量相似度算法类型
     */
    public static String DGRAPH_SIMILARITY_ALGORITHM = System.getProperty("dgraph.similarity.algorithm", "cosine");

    /**
     * Dgraph 默认批量处理大小
     */
    public static int DGRAPH_DEFAULT_BATCH_SIZE = Integer.parseInt(System.getProperty("dgraph.batch.size", "100"));

    /**
     * Dgraph 默认搜索限制
     */
    public static int DGRAPH_DEFAULT_SEARCH_LIMIT = Integer.parseInt(System.getProperty("dgraph.search.limit", "10"));
}
