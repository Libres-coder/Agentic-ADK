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
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.Data;

import java.util.Map;


@Data
public class ZillizParam {

    /**
     * VarChar field name for unique id
     */
    private String fieldNameUniqueId = "content_id";

    /**
     * FloatVector field name for embeddings
     */
    private String fieldNameEmbedding = "embeddings";

    /**
     * VarChar field name for page content
     */
    private String fieldNamePageContent = "row_content";

    /**
     * Custom search parameters
     */
    private Map<String, Object> searchParams = JSON.parseObject("{\"nprobe\":10, \"offset\":0}");

    /**
     * Initialization parameters for creating collection
     */
    private InitParam initParam = new InitParam();

    @Data
    public static class InitParam {

        /**
         * Whether to use uniqueId as primary key
         */
        private boolean fieldUniqueIdAsPrimaryKey;

        /**
         * Maximum length for pageContent VarChar field
         */
        private int fieldPageContentMaxLength = 8192;

        /**
         * Vector dimension for embeddings field
         */
        private int fieldEmbeddingsDimension = 1536;

        /**
         * Number of shards
         */
        private int shardsNum = 2;

        /**
         * Index type for embeddings field
         */
        private IndexType indexEmbeddingsIndexType = IndexType.AUTOINDEX;

        /**
         * Metric type for embeddings field
         */
        private MetricType indexEmbeddingsMetricType = MetricType.COSINE;

        /**
         * Extra parameters for embeddings index
         */
        private Map<String, Object> indexEmbeddingsExtraParam = JSON.parseObject("{}");

        /**
         * Consistency level for Zilliz Cloud
         */
        private ConsistencyLevelEnum consistencyLevel = ConsistencyLevelEnum.BOUNDED;

    }

}