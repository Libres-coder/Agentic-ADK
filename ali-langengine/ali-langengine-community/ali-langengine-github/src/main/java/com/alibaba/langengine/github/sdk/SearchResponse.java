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
package com.alibaba.langengine.github.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {

    /**
     * 总结果数
     */
    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * 是否不完整的结果
     */
    @JsonProperty("incomplete_results")
    private Boolean incompleteResults;

    /**
     * 搜索结果项
     */
    private List<SearchResult> items;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应状态
     */
    private String status;
}
