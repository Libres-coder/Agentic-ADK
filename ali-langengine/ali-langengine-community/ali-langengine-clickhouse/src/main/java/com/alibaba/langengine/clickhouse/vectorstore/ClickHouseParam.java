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
package com.alibaba.langengine.clickhouse.vectorstore;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class ClickHouseParam {

    /**
     * 表名
     */
    private String tableName = "langengine_clickhouse_collection";

    /**
     * 页面内容字段名
     */
    private String fieldNamePageContent = "page_content";

    /**
     * 唯一ID字段名
     */
    private String fieldNameUniqueId = "content_id";

    /**
     * 元数据字段名
     */
    private String fieldNameMetadata = "metadata";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "embedding";

    /**
     * 分数字段名
     */
    private String fieldNameScore = "score";

    /**
     * 批量操作大小
     */
    private int batchSize = 1000;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectionTimeout = 30000;

    /**
     * 查询超时时间（毫秒）
     */
    private int queryTimeout = 60000;

    /**
     * 最大连接池大小
     */
    private int maxPoolSize = 10;

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
         * 相似性度量函数
         */
        private ClickHouseSimilarityFunction similarityFunction = ClickHouseSimilarityFunction.COSINE;

        /**
         * 引擎类型
         */
        private String engineType = "MergeTree";

        /**
         * 排序键
         */
        private String orderBy = "content_id";

        /**
         * 分区键
         */
        private String partitionBy = "";

        /**
         * 索引粒度
         */
        private int indexGranularity = 8192;

        /**
         * 是否创建向量索引
         */
        private boolean createVectorIndex = true;

        /**
         * 向量索引类型
         */
        private String vectorIndexType = "annoy";

        /**
         * 向量索引参数
         */
        private Map<String, Object> vectorIndexParams = new HashMap<String, Object>() {{
            put("num_trees", 100);
        }};

        /**
         * 是否使用uniqueId作为主键
         */
        private boolean useUniqueIdAsPrimaryKey = true;

        /**
         * 页面内容字段最大长度
         */
        private int pageContentMaxLength = 65536;

        /**
         * 元数据字段最大长度
         */
        private int metadataMaxLength = 32768;
    }
}
