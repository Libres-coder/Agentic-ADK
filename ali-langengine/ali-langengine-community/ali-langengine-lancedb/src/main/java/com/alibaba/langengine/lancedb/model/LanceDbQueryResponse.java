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


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanceDbQueryResponse {

    /**
     * 查询结果
     */
    @JsonProperty("results")
    private List<LanceDbVector> results;

    /**
     * 总结果数量
     */
    @JsonProperty("total")
    private Integer total;

    /**
     * 查询时间（毫秒）
     */
    @JsonProperty("query_time_ms")
    private Long queryTimeMs;

    /**
     * 是否成功
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * 错误消息
     */
    @JsonProperty("error")
    private String error;

    /**
     * 请求ID
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 分页信息
     */
    @JsonProperty("pagination")
    private PaginationInfo pagination;

    /**
     * 是否查询成功
     *
     * @return 是否成功
     */
    public boolean isSuccessful() {
        return success != null ? success : (error == null || error.trim().isEmpty());
    }

    /**
     * 是否有结果
     *
     * @return 是否有结果
     */
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }

    /**
     * 获取结果数量
     *
     * @return 结果数量
     */
    public int getResultCount() {
        return results != null ? results.size() : 0;
    }

    /**
     * 获取错误消息，如果没有错误返回空字符串
     *
     * @return 错误消息
     */
    public String getErrorMessage() {
        return error != null ? error : "";
    }

    /**
     * 分页信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationInfo {
        
        /**
         * 当前页码
         */
        @JsonProperty("page")
        private Integer page;

        /**
         * 每页大小
         */
        @JsonProperty("page_size")
        private Integer pageSize;

        /**
         * 总页数
         */
        @JsonProperty("total_pages")
        private Integer totalPages;

        /**
         * 是否有下一页
         */
        @JsonProperty("has_next")
        private Boolean hasNext;

        /**
         * 是否有上一页
         */
        @JsonProperty("has_previous")
        private Boolean hasPrevious;
    }

    /**
     * 创建成功的响应
     *
     * @param results 查询结果
     * @return 响应对象
     */
    public static LanceDbQueryResponse success(List<LanceDbVector> results) {
        return LanceDbQueryResponse.builder()
                .results(results)
                .total(results != null ? results.size() : 0)
                .success(true)
                .build();
    }

    /**
     * 创建失败的响应
     *
     * @param error 错误消息
     * @return 响应对象
     */
    public static LanceDbQueryResponse failure(String error) {
        return LanceDbQueryResponse.builder()
                .success(false)
                .error(error)
                .build();
    }
}
