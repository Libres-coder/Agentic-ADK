/**
 * Copyright (C) 2025 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.gitee.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Integer id;
    private String login;
    private String name;
    
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    private String bio;
    private String blog;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("events_url")
    private String eventsUrl;
    
    private Integer followers;
    
    @JsonProperty("followers_url")
    private String followersUrl;
    
    private Integer following;
    
    @JsonProperty("following_url")
    private String followingUrl;
    
    @JsonProperty("gists_url")
    private String gistsUrl;
    
    @JsonProperty("html_url")
    private String htmlUrl;
    
    @JsonProperty("member_role")
    private String memberRole;
    
    @JsonProperty("organizations_url")
    private String organizationsUrl;
    
    @JsonProperty("public_gists")
    private Integer publicGists;
    
    @JsonProperty("public_repos")
    private Integer publicRepos;
    
    @JsonProperty("received_events_url")
    private String receivedEventsUrl;
    
    private String remark;
    
    @JsonProperty("repos_url")
    private String reposUrl;
    
    private Integer stared;
    
    @JsonProperty("starred_url")
    private String starredUrl;
    
    @JsonProperty("subscriptions_url")
    private String subscriptionsUrl;
    
    private String type;
    
    private String url;
    
    private Integer watched;
    
    private String weibo;
}