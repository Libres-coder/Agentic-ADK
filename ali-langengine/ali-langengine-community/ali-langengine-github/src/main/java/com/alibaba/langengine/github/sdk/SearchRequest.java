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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class SearchRequest {

    /**
     * 查询关键字
     */
    private String query;

    /**
     * 排序字段
     */
    private String sort;

    /**
     * 排序顺序 (asc/desc)
     */
    private String order;

    /**
     * 每页结果数 (1-100)
     */
    @JsonProperty("per_page")
    private Integer perPage;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 搜索类型 (repositories, code, users, issues)
     */
    private String type;

    /**
     * 默认构造函数
     */
    public SearchRequest() {
    }

    /**
     * 构造函数
     *
     * @param query 查询关键字
     */
    public SearchRequest(String query) {
        this.query = query;
    }

    /**
     * 构造函数
     *
     * @param query 查询关键字
     * @param type 搜索类型
     */
    public SearchRequest(String query, String type) {
        this.query = query;
        this.type = type;
    }
}
