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
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    private Integer id;
    private String name;
    
    @JsonProperty("full_name")
    private String fullName;
    
    @JsonProperty("human_name")
    private String humanName;
    
    private String description;
    
    @JsonProperty("private")
    private Boolean privateRepo;
    
    @JsonProperty("public")
    private Boolean publicRepo;
    
    private Boolean internal;
    private Boolean fork;
    
    @JsonProperty("html_url")
    private String htmlUrl;
    
    @JsonProperty("ssh_url")
    private String sshUrl;
    
    @JsonProperty("forks_url")
    private String forksUrl;
    
    @JsonProperty("keys_url")
    private String keysUrl;
    
    @JsonProperty("collaborators_url")
    private String collaboratorsUrl;
    
    @JsonProperty("hooks_url")
    private String hooksUrl;
    
    @JsonProperty("branches_url")
    private String branchesUrl;
    
    @JsonProperty("tags_url")
    private String tagsUrl;
    
    @JsonProperty("blobs_url")
    private String blobsUrl;
    
    @JsonProperty("stargazers_url")
    private String stargazersUrl;
    
    @JsonProperty("contributors_url")
    private String contributorsUrl;
    
    @JsonProperty("commits_url")
    private String commitsUrl;
    
    @JsonProperty("comments_url")
    private String commentsUrl;
    
    @JsonProperty("issue_comment_url")
    private String issueCommentUrl;
    
    @JsonProperty("issues_url")
    private String issuesUrl;
    
    @JsonProperty("pulls_url")
    private String pullsUrl;
    
    @JsonProperty("milestones_url")
    private String milestonesUrl;
    
    @JsonProperty("notifications_url")
    private String notificationsUrl;
    
    @JsonProperty("labels_url")
    private String labelsUrl;
    
    @JsonProperty("releases_url")
    private String releasesUrl;
    
    private String url;
    private String path;
    
    @JsonProperty("default_branch")
    private String defaultBranch;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("pushed_at")
    private String pushedAt;
    
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;
    
    @JsonProperty("forks_count")
    private Integer forksCount;
    
    @JsonProperty("watchers_count")
    private Integer watchersCount;
    
    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;
    
    private String language;
    private String license;
    private String homepage;
    
    @JsonProperty("has_issues")
    private Boolean hasIssues;
    
    @JsonProperty("has_wiki")
    private Boolean hasWiki;
    
    @JsonProperty("has_page")
    private Boolean hasPage;
    
    @JsonProperty("can_comment")
    private Boolean canComment;
    
    @JsonProperty("issue_comment")
    private Boolean issueComment;
    
    @JsonProperty("pull_requests_enabled")
    private Boolean pullRequestsEnabled;
    
    private Boolean recommend;
    private Boolean gvp;
    private Boolean outsourced;
    
    @JsonProperty("project_creator")
    private String projectCreator;
    
    private List<String> members;
    
    private Boolean stared;
    private Boolean watched;
    
    private String status;
    private String paas;
    
    @JsonProperty("assignees_number")
    private Integer assigneesNumber;
    
    @JsonProperty("testers_number")
    private Integer testersNumber;
    
    @JsonProperty("issue_template_source")
    private String issueTemplateSource;
    
    private User owner;
    private User assigner;
    private Namespace namespace;
    private Repository parent;
    private Enterprise enterprise;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Namespace {
        private Integer id;
        private String type;
        private String name;
        private String path;
        
        @JsonProperty("html_url")
        private String htmlUrl;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Enterprise {
        private Integer id;
        private String type;
        private String name;
        private String path;
        
        @JsonProperty("html_url")
        private String htmlUrl;
    }
}