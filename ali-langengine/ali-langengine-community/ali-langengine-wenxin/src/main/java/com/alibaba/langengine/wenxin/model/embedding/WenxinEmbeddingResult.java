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
package com.alibaba.langengine.wenxin.model.embedding;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;


@Data
public class WenxinEmbeddingResult {

    /**
     * 本次请求的id
     */
    @JSONField(name = "id")
    private String id;

    /**
     * 回包类型
     */
    @JSONField(name = "object")
    private String object;

    /**
     * 时间戳
     */
    @JSONField(name = "created")
    private Long created;

    /**
     * 嵌入向量数据列表
     */
    @JSONField(name = "data")
    private List<WenxinEmbeddingData> data;

    /**
     * 模型名称
     */
    @JSONField(name = "model")
    private String model;

    /**
     * token统计信息
     */
    @JSONField(name = "usage")
    private WenxinEmbeddingUsage usage;

    /**
     * 错误码
     */
    @JSONField(name = "error_code")
    private Integer errorCode;

    /**
     * 错误描述信息
     */
    @JSONField(name = "error_msg")
    private String errorMsg;

    /**
     * 嵌入向量数据
     */
    @Data
    public static class WenxinEmbeddingData {

        /**
         * 数据类型，固定值"embedding"
         */
        @JSONField(name = "object")
        private String object;

        /**
         * 嵌入向量，浮点数数组
         */
        @JSONField(name = "embedding")
        private List<Double> embedding;

        /**
         * 数据索引位置
         */
        @JSONField(name = "index")
        private Integer index;
    }

    /**
     * Token使用统计
     */
    @Data
    public static class WenxinEmbeddingUsage {

        /**
         * 问题tokens数
         */
        @JSONField(name = "prompt_tokens")
        private Integer promptTokens;

        /**
         * tokens总数
         */
        @JSONField(name = "total_tokens")
        private Integer totalTokens;
    }
}
