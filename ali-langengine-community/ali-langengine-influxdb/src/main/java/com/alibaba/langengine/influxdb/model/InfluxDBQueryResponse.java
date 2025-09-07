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

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
public class InfluxDBQueryResponse {

    /**
     * 查询结果向量列表
     */
    @Builder.Default
    private List<InfluxDBVector> results = new ArrayList<>();

    /**
     * 查询是否成功
     */
    @Builder.Default
    private boolean success = true;

    /**
     * 查询耗时（毫秒）
     */
    private long queryTimeMs;

    /**
     * 总结果数量
     */
    private int totalResults;

    /**
     * 返回结果数量
     */
    private int returnedResults;

    /**
     * 是否有更多结果
     */
    private boolean hasMore;

    /**
     * 页码
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * 页大小
     */
    @Builder.Default
    private Integer pageSize = 10;

    /**
     * 总页数
     */
    @Builder.Default
    private Integer totalPages = 1;

    /**
     * 错误代码（如果有）
     */
    private String errorCode;

    /**
     * 错误消息（如果有）
     */
    private String errorMessage;

    /**
     * 查询统计信息
     */
    private QueryStatistics statistics;

    /**
     * 查询状态枚举
     */
    public enum QueryStatus {
        SUCCESS("success"),
        PARTIAL("partial"),
        ERROR("error"),
        TIMEOUT("timeout");

        private final String value;

        QueryStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 查询统计信息
     */
    @Data
    @Builder
    public static class QueryStatistics {
        /**
         * 扫描的数据点数量
         */
        private long scannedPoints;

        /**
         * 过滤后的数据点数量
         */
        private long filteredPoints;

        /**
         * 计算相似度的数量
         */
        private long similarityCalculations;

        /**
         * 缓存命中数量
         */
        private long cacheHits;

        /**
         * 缓存未命中数量
         */
        private long cacheMisses;
    }

    /**
     * 检查查询是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取总页数（自动计算）
     *
     * @return 总页数
     */
    public Integer getTotalPages() {
        if (totalResults > 0 && pageSize != null && pageSize > 0) {
            return (int) Math.ceil((double) totalResults / pageSize);
        }
        return totalPages != null ? totalPages : 1;
    }

    /**
     * 获取结果数量
     *
     * @return 结果数量
     */
    public int size() {
        return results != null ? results.size() : 0;
    }

    /**
     * 检查是否有结果
     *
     * @return 是否有结果
     */
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }
}
