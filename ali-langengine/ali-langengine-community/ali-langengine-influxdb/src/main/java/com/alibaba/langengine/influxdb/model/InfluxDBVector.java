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
package com.alibaba.langengine.influxdb.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@EqualsAndHashCode
public class InfluxDBVector {

    /**
     * 文档唯一标识符
     */
    private String id;

    /**
     * 向量数据
     */
    private List<Double> vector;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 元数据信息
     */
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 时间戳
     */
    private Instant timestamp = Instant.now();

    /**
     * 测量名称
     */
    private String measurement = "vectors";

    /**
     * 标签信息
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * 字段信息
     */
    private Map<String, Object> fields = new HashMap<>();

    /**
     * 相似度分数（查询时使用）
     */
    private Double score;

    /**
     * 获取向量维度
     *
     * @return 向量维度
     */
    public int getDimension() {
        return vector != null ? vector.size() : 0;
    }

    /**
     * 获取向量维度 (测试兼容方法)
     *
     * @return 向量维度
     */
    public int getDimensions() {
        return getDimension();
    }

    /**
     * 验证向量数据有效性
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() 
               && vector != null && !vector.isEmpty()
               && content != null;
    }

    /**
     * 获取向量的字符串表示（用于存储）
     *
     * @return 向量字符串
     */
    public String getVectorAsString() {
        if (vector == null || vector.isEmpty()) {
            return null;
        }
        return vector.toString();
    }

    /**
     * 创建默认的标签映射
     *
     * @return 标签映射
     */
    public Map<String, String> getDefaultTags() {
        if (tags != null) {
            return tags;
        }
        return Map.of("doc_id", id);
    }

    /**
     * 创建默认的字段映射
     *
     * @return 字段映射
     */
    public Map<String, Object> getDefaultFields() {
        if (fields != null) {
            return fields;
        }
        Map<String, Object> defaultFields = new java.util.HashMap<>();
        defaultFields.put("content", content);
        defaultFields.put("vector", getVectorAsString());
        if (metadata != null) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                defaultFields.put("meta_" + entry.getKey(), entry.getValue());
            }
        }
        return defaultFields;
    }

    /**
     * 克隆向量对象
     *
     * @return 克隆的向量对象
     */
    public InfluxDBVector clone() {
        return InfluxDBVector.builder()
                .id(this.id)
                .vector(this.vector != null ? new java.util.ArrayList<>(this.vector) : null)
                .content(this.content)
                .metadata(this.metadata != null ? new java.util.HashMap<>(this.metadata) : new java.util.HashMap<>())
                .timestamp(this.timestamp)
                .measurement(this.measurement)
                .tags(this.tags != null ? new java.util.HashMap<>(this.tags) : new java.util.HashMap<>())
                .fields(this.fields != null ? new java.util.HashMap<>(this.fields) : new java.util.HashMap<>())
                .score(this.score)
                .build();
    }

    /**
     * 拷贝向量对象 (测试兼容方法)
     *
     * @return 拷贝的向量对象
     */
    public InfluxDBVector copy() {
        return clone();
    }

    /**
     * 验证向量对象
     */
    public void validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Vector ID cannot be null or empty");
        }
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("Vector data cannot be null or empty");
        }
        for (Double value : vector) {
            if (value == null || !Double.isFinite(value)) {
                throw new IllegalArgumentException("Vector values must be finite numbers");
            }
        }
    }

    /**
     * 创建Builder
     */
    public static InfluxDBVectorBuilder builder() {
        return new InfluxDBVectorBuilder();
    }

    /**
     * 自定义Builder类，支持验证
     */
    public static class InfluxDBVectorBuilder {
        private String id;
        private List<Double> vector;
        private String content;
        private Map<String, Object> metadata = new HashMap<>();
        private Instant timestamp = Instant.now();
        private String measurement = "vectors";
        private Map<String, String> tags = new HashMap<>();
        private Map<String, Object> fields = new HashMap<>();
        private Double score;

        public InfluxDBVectorBuilder id(String id) {
            this.id = id;
            return this;
        }

        public InfluxDBVectorBuilder vector(List<Double> vector) {
            this.vector = vector;
            return this;
        }

        public InfluxDBVectorBuilder content(String content) {
            this.content = content;
            return this;
        }

        public InfluxDBVectorBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public InfluxDBVectorBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public InfluxDBVectorBuilder measurement(String measurement) {
            this.measurement = measurement;
            return this;
        }

        public InfluxDBVectorBuilder tags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public InfluxDBVectorBuilder fields(Map<String, Object> fields) {
            this.fields = fields;
            return this;
        }

        public InfluxDBVectorBuilder score(Double score) {
            this.score = score;
            return this;
        }

        public InfluxDBVector build() {
            InfluxDBVector vector = new InfluxDBVector();
            vector.id = this.id;
            vector.vector = this.vector;
            vector.content = this.content;
            vector.metadata = this.metadata != null ? this.metadata : new HashMap<>();
            vector.timestamp = this.timestamp != null ? this.timestamp : Instant.now();
            vector.measurement = this.measurement != null ? this.measurement : "vectors";
            vector.tags = this.tags != null ? this.tags : new HashMap<>();
            vector.fields = this.fields != null ? this.fields : new HashMap<>();
            vector.score = this.score;
            
            // 验证
            vector.validate();
            
            return vector;
        }
    }
}
