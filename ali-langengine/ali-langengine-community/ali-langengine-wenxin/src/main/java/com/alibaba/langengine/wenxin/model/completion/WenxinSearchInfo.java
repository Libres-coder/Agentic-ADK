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
package com.alibaba.langengine.wenxin.model.completion;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;


@Data
public class WenxinSearchInfo {

    /**
     * 搜索结果列表
     */
    @JSONField(name = "search_results")
    private List<WenxinSearchResult> searchResults;

    /**
     * 搜索结果
     */
    @Data
    public static class WenxinSearchResult {

        /**
         * 搜索结果序号
         */
        @JSONField(name = "index")
        private Integer index;

        /**
         * 搜索结果URL
         */
        @JSONField(name = "url")
        private String url;

        /**
         * 搜索结果标题
         */
        @JSONField(name = "title")
        private String title;

        /**
         * 搜索结果内容
         */
        @JSONField(name = "content")
        private String content;
    }
}
