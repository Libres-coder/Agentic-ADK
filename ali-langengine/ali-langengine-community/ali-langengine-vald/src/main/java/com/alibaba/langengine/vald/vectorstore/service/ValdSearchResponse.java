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
package com.alibaba.langengine.vald.vectorstore.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValdSearchResponse {

    /**
     * 搜索结果
     */
    private List<ValdSearchResult> results;

    /**
     * 单个搜索结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValdSearchResult {
        /**
         * 向量ID
         */
        private String id;

        /**
         * 相似度分数（距离）
         */
        private double distance;

        /**
         * 元数据
         */
        private String metadata;
    }

}