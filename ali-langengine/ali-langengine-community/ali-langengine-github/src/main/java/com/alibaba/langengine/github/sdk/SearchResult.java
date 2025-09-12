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
package com.alibaba.langengine.github.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 全名 (仓库)
     */
    @JsonProperty("full_name")
    private String fullName;

    /**
     * 描述
     */
    private String description;

    /**
     * URL
     */
    private String url;

    /**
     * HTML URL
     */
    @JsonProperty("html_url")
    private String htmlUrl;

    /**
     * Git URL
     */
    @JsonProperty("git_url")
    private String gitUrl;

    /**
     * Clone URL
     */
    @JsonProperty("clone_url")
    private String cloneUrl;

    /**
     * SSH URL
     */
    @JsonProperty("ssh_url")
    private String sshUrl;

    /**
     * 是否私有
     */
    @JsonProperty("private")
    private Boolean isPrivate;

    /**
     * 是否fork
     */
    private Boolean fork;

    /**
     * 星标数
     */
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;

    /**
     * 观察者数
     */
    @JsonProperty("watchers_count")
    private Integer watchersCount;

    /**
     * Fork数
     */
    @JsonProperty("forks_count")
    private Integer forksCount;

    /**
     * 大小
     */
    private Integer size;

    /**
     * 默认分支
     */
    @JsonProperty("default_branch")
    private String defaultBranch;

    /**
     * 主要编程语言
     */
    private String language;

    /**
     * 主题标签
     */
    private List<String> topics;

    /**
     * 所有者
     */
    private Owner owner;

    /**
     * 创建时间
     */
    @JsonProperty("created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonProperty("updated_at")
    private Date updatedAt;

    /**
     * 推送时间
     */
    @JsonProperty("pushed_at")
    private Date pushedAt;

    /**
     * 评分
     */
    private Double score;

    /**
     * 文件内容 (代码搜索)
     */
    private String content;

    /**
     * 文件路径 (代码搜索)
     */
    private String path;

    /**
     * 仓库信息 (代码搜索)
     */
    private Repository repository;

    /**
     * 文本匹配 (搜索高亮)
     */
    @JsonProperty("text_matches")
    private List<TextMatch> textMatches;

    /**
     * 许可证信息
     */
    private License license;

    /**
     * 其他属性
     */
    private Map<String, Object> additionalProperties;

    /**
     * 仓库所有者
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private Long id;
        private String login;
        @JsonProperty("avatar_url")
        private String avatarUrl;
        private String url;
        @JsonProperty("html_url")
        private String htmlUrl;
        private String type;
    }

    /**
     * 仓库信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private Long id;
        private String name;
        @JsonProperty("full_name")
        private String fullName;
        private String description;
        @JsonProperty("html_url")
        private String htmlUrl;
        private Owner owner;
        @JsonProperty("private")
        private Boolean isPrivate;
    }

    /**
     * 文本匹配信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextMatch {
        @JsonProperty("object_url")
        private String objectUrl;
        @JsonProperty("object_type")
        private String objectType;
        private String property;
        private String fragment;
        private List<Match> matches;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Match {
            private String text;
            private List<Integer> indices;
        }
    }

    /**
     * 许可证信息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class License {
        private String key;
        private String name;
        @JsonProperty("spdx_id")
        private String spdxId;
        private String url;
    }
}
