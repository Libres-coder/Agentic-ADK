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
package com.alibaba.langengine.twitter.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TweetTimelineResponse {
    
    @JsonProperty("data")
    private List<Tweet> data;
    
    @JsonProperty("meta")
    private Meta meta;
    
    @Data
    public static class Tweet {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("public_metrics")
        private PublicMetrics publicMetrics;
        
        @JsonProperty("context_annotations")
        private List<ContextAnnotation> contextAnnotations;
    }
    
    @Data
    public static class PublicMetrics {
        @JsonProperty("retweet_count")
        private int retweetCount;
        
        @JsonProperty("like_count")
        private int likeCount;
        
        @JsonProperty("reply_count")
        private int replyCount;
        
        @JsonProperty("quote_count")
        private int quoteCount;
    }
    
    @Data
    public static class ContextAnnotation {
        @JsonProperty("domain")
        private Domain domain;
        
        @JsonProperty("entity")
        private Entity entity;
    }
    
    @Data
    public static class Domain {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
    }
    
    @Data
    public static class Entity {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
    }
    
    @Data
    public static class Meta {
        @JsonProperty("newest_id")
        private String newestId;
        
        @JsonProperty("oldest_id")
        private String oldestId;
        
        @JsonProperty("result_count")
        private int resultCount;
        
        @JsonProperty("next_token")
        private String nextToken;
    }
}