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

package com.alibaba.langengine.gitcode.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {

    /**
     * Repository ID
     */
    private Integer id;

    /**
     * Repository full name
     */
    @JsonProperty("full_name")
    private String fullName;

    /**
     * Repository human readable name
     */
    @JsonProperty("human_name")
    private String humanName;

    /**
     * Repository URL
     */
    private String url;

    /**
     * Namespace information
     */
    private Namespace namespace;

    /**
     * Repository path
     */
    private String path;

    /**
     * Repository name
     */
    private String name;

    /**
     * Repository description
     */
    private String description;

    /**
     * Repository status
     */
    private String status;

    /**
     * SSH clone URL
     */
    @JsonProperty("ssh_url_to_repo")
    private String sshUrlToRepo;

    /**
     * HTTP clone URL
     */
    @JsonProperty("http_url_to_repo")
    private String httpUrlToRepo;

    /**
     * Web page URL
     */
    @JsonProperty("web_url")
    private String webUrl;

    /**
     * Created timestamp
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * Updated timestamp
     */
    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * Homepage URL
     */
    private String homepage;

    /**
     * Members list
     */
    private List<String> members;

    /**
     * Forks count
     */
    @JsonProperty("forks_count")
    private Integer forksCount;

    /**
     * Stars count
     */
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;

    /**
     * Relation type
     */
    private String relation;

    /**
     * Permission information
     */
    private Permission permission;

    /**
     * Whether it is an internal repository
     */
    private Boolean internal;

    /**
     * Open issues count
     */
    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;

    /**
     * Whether it has issues feature
     */
    @JsonProperty("has_issue")
    private Boolean hasIssue;

    /**
     * Watchers count
     */
    @JsonProperty("watchers_count")
    private Integer watchersCount;

    /**
     * Enterprise information
     */
    private Enterprise enterprise;

    /**
     * Default branch
     */
    @JsonProperty("default_branch")
    private String defaultBranch;

    /**
     * Whether it is a fork
     */
    private Boolean fork;

    /**
     * Last pushed timestamp
     */
    @JsonProperty("pushed_at")
    private String pushedAt;

    /**
     * Owner information
     */
    private User owner;

    /**
     * Issue template source
     */
    @JsonProperty("issue_template_source")
    private String issueTemplateSource;

    /**
     * Whether it is private
     */
    @JsonProperty("private")
    private Boolean privateRepo;

    /**
     * Whether it is public
     */
    @JsonProperty("public")
    private Boolean publicRepo;

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
    public static class Permission {
        private Boolean push;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Enterprise {
        private Integer id;
        private String path;
        @JsonProperty("html_url")
        private String htmlUrl;
        private String type;
    }
}