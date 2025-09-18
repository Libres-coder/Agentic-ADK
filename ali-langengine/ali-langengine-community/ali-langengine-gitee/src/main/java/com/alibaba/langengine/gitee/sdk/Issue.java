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
public class Issue {

    private Integer id;
    private String number;
    private String title;
    private String body;
    
    @JsonProperty("body_html")
    private String bodyHtml;
    
    private String state;
    private String branch;
    
    @JsonProperty("html_url")
    private String htmlUrl;
    
    private String url;
    
    @JsonProperty("repository_url")
    private String repositoryUrl;
    
    @JsonProperty("comments_url")
    private String commentsUrl;
    
    @JsonProperty("labels_url")
    private String labelsUrl;
    
    @JsonProperty("parent_url")
    private String parentUrl;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("finished_at")
    private String finishedAt;
    
    @JsonProperty("plan_started_at")
    private String planStartedAt;
    
    private String deadline;
    
    private Integer comments;
    private Integer priority;
    private Integer depth;
    
    @JsonProperty("parent_id")
    private Integer parentId;
    
    @JsonProperty("scheduled_time")
    private Integer scheduledTime;
    
    @JsonProperty("security_hole")
    private Boolean securityHole;
    
    @JsonProperty("cve_id")
    private String cveId;
    
    private User assignee;
    private User user;
    private User author;
    private User assigner;
    
    private Repository repository;
    private Milestone milestone;
    private Program program;
    
    @JsonProperty("issue_state_detail")
    private IssueStateDetail issueStateDetail;
    
    @JsonProperty("issue_type_detail")
    private IssueTypeDetail issueTypeDetail;
    
    private List<Label> labels;
    private List<User> collaborators;
    private List<User> assignees;
    private List<User> testers;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Milestone {
        private String url;
        
        @JsonProperty("html_url")
        private String htmlUrl;
        
        private Integer number;
        
        @JsonProperty("repository_id")
        private Integer repositoryId;
        
        private String state;
        private String title;
        private String description;
        
        @JsonProperty("updated_at")
        private String updatedAt;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("open_issues")
        private Integer openIssues;
        
        @JsonProperty("closed_issues")
        private Integer closedIssues;
        
        @JsonProperty("due_on")
        private String dueOn;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Program {
        private Integer id;
        private String name;
        private String description;
        private User assignee;
        private User author;
        private Repository repository;
        private User assigner;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueStateDetail {
        private Integer id;
        private String title;
        private String color;
        private String icon;
        private String command;
        private Integer serial;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("updated_at")
        private String updatedAt;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueTypeDetail {
        private Integer id;
        private String title;
        private String template;
        private String ident;
        private String color;
        
        @JsonProperty("is_system")
        private Boolean isSystem;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("updated_at")
        private String updatedAt;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Label {
        private Integer id;
        private String name;
        private String color;
        
        @JsonProperty("repository_id")
        private Integer repositoryId;
        
        private String url;
    }
}