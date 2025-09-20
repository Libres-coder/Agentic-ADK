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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class RedditSearchRequest {

    /**
     * 搜索关键字
     */
    private String query;

    /**
     * 子论坛
     */
    private String subreddit;

    /**
     * 排序方式: hot, new, top, rising
     */
    private String sort = "hot";

    /**
     * 时间范围: hour, day, week, month, year, all
     */
    @JsonProperty("t")
    private String timeRange = "day";

    /**
     * 结果数量限制 (1-100)
     */
    private Integer limit = 25;

    /**
     * 分页标识符
     */
    private String after;

    /**
     * 搜索类型: sr, link, user
     */
    private String type = "link";

    /**
     * 是否包含NSFW内容
     */
    @JsonProperty("include_over_18")
    private Boolean includeOver18 = false;

    /**
     * 构造函数
     */
    public RedditSearchRequest() {
    }

    /**
     * 构造函数
     *
     * @param query 搜索查询
     */
    public RedditSearchRequest(String query) {
        this.query = query;
    }

    /**
     * 构造函数
     *
     * @param query     搜索查询
     * @param subreddit 子论坛
     */
    public RedditSearchRequest(String query, String subreddit) {
        this.query = query;
        this.subreddit = subreddit;
    }
}
