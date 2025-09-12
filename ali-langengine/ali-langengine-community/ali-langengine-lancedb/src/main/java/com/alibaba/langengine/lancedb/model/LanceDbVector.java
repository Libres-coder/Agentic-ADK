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
package com.alibaba.langengine.lancedb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanceDbVector {

    /**
     * 唯一标识符
     */
    @JsonProperty("id")
    private String id;

    /**
     * 向量数据
     */
    @JsonProperty("vector")
    private List<Double> vector;

    /**
     * 文档内容
     */
    @JsonProperty("text")
    private String text;

    /**
     * 文档元数据
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * 相似度分数（查询时返回）
     */
    @JsonProperty("score")
    private Double score;

    /**
     * 距离值（查询时返回）
     */
    @JsonProperty("distance")
    private Double distance;

    /**
     * 创建时间戳
     */
    @JsonProperty("created_at")
    private Long createdAt;

    /**
     * 更新时间戳
     */
    @JsonProperty("updated_at")
    private Long updatedAt;

    /**
     * 验证向量记录是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() && 
               vector != null && !vector.isEmpty() && 
               text != null && !text.trim().isEmpty();
    }

    /**
     * 获取向量维度
     *
     * @return 向量维度
     */
    public int getVectorDimension() {
        return vector != null ? vector.size() : 0;
    }

    /**
     * 设置当前时间戳
     */
    public void setCurrentTimestamp() {
        long now = System.currentTimeMillis();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    /**
     * 创建一个简单的向量记录
     *
     * @param id     标识符
     * @param vector 向量
     * @param text   文本
     * @return 向量记录
     */
    public static LanceDbVector of(String id, List<Double> vector, String text) {
        LanceDbVector record = LanceDbVector.builder()
                .id(id)
                .vector(vector)
                .text(text)
                .build();
        record.setCurrentTimestamp();
        return record;
    }

    /**
     * 创建一个带元数据的向量记录
     *
     * @param id       标识符
     * @param vector   向量
     * @param text     文本
     * @param metadata 元数据
     * @return 向量记录
     */
    public static LanceDbVector of(String id, List<Double> vector, String text, Map<String, Object> metadata) {
        LanceDbVector record = LanceDbVector.builder()
                .id(id)
                .vector(vector)
                .text(text)
                .metadata(metadata)
                .build();
        record.setCurrentTimestamp();
        return record;
    }
}
