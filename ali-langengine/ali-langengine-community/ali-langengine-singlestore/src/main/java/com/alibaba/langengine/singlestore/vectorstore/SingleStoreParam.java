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
package com.alibaba.langengine.singlestore.vectorstore;

import lombok.Data;


@Data
public class SingleStoreParam {

    /**
     * 唯一ID字段名
     */
    private String fieldNameUniqueId = "content_id";

    /**
     * 向量字段名
     */
    private String fieldNameEmbedding = "embeddings";

    /**
     * 文本内容字段名
     */
    private String fieldNamePageContent = "row_content";

    /**
     * 表名
     */
    private String tableName = "vector_documents";

    /**
     * 初始化参数, 用于创建表
     */
    private InitParam initParam = new InitParam();

    @Data
    public static class InitParam {

        /**
         * 是否使用uniqueId作为主键, 如果是的话, addDocuments的时候uniqueId不要为空
         */
        private boolean fieldUniqueIdAsPrimaryKey = true;

        /**
         * pageContent字段VARCHAR长度
         */
        private int fieldPageContentMaxLength = 8192;

        /**
         * embeddings字段向量维度, 如果设置为0, 则会通过embedding模型查询一条数据, 看维度是多少
         */
        private int fieldEmbeddingsDimension = 1536;

        /**
         * 向量索引类型 (SingleStore支持的向量索引类型)
         */
        private String vectorIndexType = "IVF_PQFS";

        /**
         * 向量距离度量类型 (SingleStore支持: EUCLIDEAN_DISTANCE, DOT_PRODUCT, COSINE_DISTANCE)
         */
        private String vectorMetricType = "EUCLIDEAN_DISTANCE";

        /**
         * 是否使用SSL连接 (生产环境建议设为true)
         */
        private boolean useSsl = false;

    }

}