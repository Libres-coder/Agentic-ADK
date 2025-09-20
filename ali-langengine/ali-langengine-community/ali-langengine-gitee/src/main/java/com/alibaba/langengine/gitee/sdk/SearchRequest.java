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

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SearchRequest {

    private String q;
    private String sort;
    private String order;
    private Integer page;
    private Integer perPage;
    
    // Repository specific parameters
    private String owner;
    private Boolean fork;
    private String language;
    
    // Issue specific parameters
    private String repo;
    private String state;
    private String label;
    private String author;
    private String assignee;

    public static SearchRequest forUsers(String query) {
        return new SearchRequest().setQ(query);
    }

    public static SearchRequest forIssues(String query) {
        return new SearchRequest().setQ(query);
    }

    public static SearchRequest forRepositories(String query) {
        return new SearchRequest().setQ(query);
    }

    public SearchRequest withSort(String sort, String order) {
        this.sort = sort;
        this.order = order;
        return this;
    }

    public SearchRequest withPagination(Integer page, Integer perPage) {
        this.page = page;
        this.perPage = perPage;
        return this;
    }

    public SearchRequest withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public SearchRequest withFork(Boolean fork) {
        this.fork = fork;
        return this;
    }

    public SearchRequest withLanguage(String language) {
        this.language = language;
        return this;
    }

    public SearchRequest withRepo(String repo) {
        this.repo = repo;
        return this;
    }

    public SearchRequest withState(String state) {
        this.state = state;
        return this;
    }

    public SearchRequest withLabel(String label) {
        this.label = label;
        return this;
    }

    public SearchRequest withAuthor(String author) {
        this.author = author;
        return this;
    }

    public SearchRequest withAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }
}