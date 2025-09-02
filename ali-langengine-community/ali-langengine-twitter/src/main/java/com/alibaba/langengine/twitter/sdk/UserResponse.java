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

@Data
public class UserResponse {
    
    @JsonProperty("data")
    private User data;
    
    @Data
    public static class User {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("username")
        private String username;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("verified")
        private boolean verified;
        
        @JsonProperty("public_metrics")
        private PublicMetrics publicMetrics;
    }
    
    @Data
    public static class PublicMetrics {
        @JsonProperty("followers_count")
        private int followersCount;
        
        @JsonProperty("following_count")
        private int followingCount;
        
        @JsonProperty("tweet_count")
        private int tweetCount;
        
        @JsonProperty("listed_count")
        private int listedCount;
    }
}