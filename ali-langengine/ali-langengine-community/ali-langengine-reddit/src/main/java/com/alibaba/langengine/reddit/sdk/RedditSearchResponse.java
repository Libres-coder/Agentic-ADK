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
package com.alibaba.langengine.reddit.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedditSearchResponse {

    /**
     * 响应种类
     */
    private String kind;

    /**
     * 响应数据
     */
    private DataWrapper data;

    /**
     * 数据包装类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataWrapper {
        /**
         * 分页标识符 - 下一页
         */
        private String after;

        /**
         * 分页标识符 - 上一页
         */
        private String before;

        /**
         * 数据距离
         */
        private Integer dist;

        /**
         * 帖子列表
         */
        private List<PostWrapper> children;

        /**
         * 调制哈希
         */
        private String modhash;

        /**
         * geo过滤器
         */
        @JsonProperty("geo_filter")
        private String geoFilter;
    }

    /**
     * 帖子包装类
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostWrapper {
        /**
         * 种类
         */
        private String kind;

        /**
         * 帖子数据
         */
        private RedditPost data;
    }

    /**
     * 获取帖子列表
     *
     * @return 帖子列表
     */
    public List<RedditPost> getPosts() {
        if (data == null || data.getChildren() == null) {
            return List.of();
        }
        return data.getChildren().stream()
                .map(PostWrapper::getData)
                .toList();
    }

    /**
     * 获取下一页标识符
     *
     * @return 下一页标识符
     */
    public String getAfter() {
        return data != null ? data.getAfter() : null;
    }

    /**
     * 获取上一页标识符
     *
     * @return 上一页标识符
     */
    public String getBefore() {
        return data != null ? data.getBefore() : null;
    }

    /**
     * 是否有更多数据
     *
     * @return true如果有更多数据
     */
    public boolean hasMore() {
        return getAfter() != null;
    }
}
