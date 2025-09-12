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
package com.alibaba.langengine.weibo.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WeiboTimelineResponse {
    
    @JsonProperty("statuses")
    private List<Status> statuses;
    
    @Data
    public static class Status {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("reposts_count")
        private int repostsCount;
        
        @JsonProperty("comments_count")
        private int commentsCount;
        
        @JsonProperty("attitudes_count")
        private int attitudesCount;
    }
}