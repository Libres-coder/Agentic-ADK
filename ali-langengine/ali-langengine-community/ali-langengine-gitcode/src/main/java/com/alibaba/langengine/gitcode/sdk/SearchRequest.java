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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchRequest {

    /**
     * Search keyword
     */
    private String q;

    /**
     * Sort field
     */
    private String sort;

    /**
     * Sort order (asc/desc)
     */
    private String order;

    /**
     * Results per page (1-50)
     */
    @JsonProperty("per_page")
    private Integer perPage;

    /**
     * Page number (max 100)
     */
    private Integer page;

    /**
     * Repository path (for Issues search)
     */
    private String repo;

    /**
     * State filter (for Issues search, open/closed)
     */
    private String state;

    /**
     * Repository owner (for repository search)
     */
    private String owner;

    /**
     * Whether to search forked repositories (for repository search)
     */
    private String fork;

    /**
     * Filter repositories by language (for repository search)
     */
    private String language;

    /**
     * Default constructor
     */
    public SearchRequest() {
    }

    /**
     * Constructor
     *
     * @param q Search keyword
     */
    public SearchRequest(String q) {
        this.q = q;
    }

    /**
     * Constructor
     *
     * @param q Search keyword
     * @param page Page number
     * @param perPage Results per page
     */
    public SearchRequest(String q, Integer page, Integer perPage) {
        this.q = q;
        this.page = page;
        this.perPage = perPage;
    }

    /**
     * Build user search request
     *
     * @param q Search keyword
     * @return Search request
     */
    public static SearchRequest forUsers(String q) {
        return new SearchRequest(q);
    }

    /**
     * Build Issues search request
     *
     * @param q Search keyword
     * @return Search request
     */
    public static SearchRequest forIssues(String q) {
        return new SearchRequest(q);
    }

    /**
     * Build repository search request
     *
     * @param q Search keyword
     * @return Search request
     */
    public static SearchRequest forRepositories(String q) {
        return new SearchRequest(q);
    }

    /**
     * Set sort
     *
     * @param sort Sort field
     * @param order Sort order
     * @return Current request object
     */
    public SearchRequest withSort(String sort, String order) {
        this.sort = sort;
        this.order = order;
        return this;
    }

    /**
     * Set pagination
     *
     * @param page Page number
     * @param perPage Results per page
     * @return Current request object
     */
    public SearchRequest withPagination(Integer page, Integer perPage) {
        this.page = page;
        this.perPage = perPage;
        return this;
    }

    /**
     * Set repository filter (for Issues search)
     *
     * @param repo Repository path
     * @return Current request object
     */
    public SearchRequest withRepo(String repo) {
        this.repo = repo;
        return this;
    }

    /**
     * Set state filter (for Issues search)
     *
     * @param state State (open/closed)
     * @return Current request object
     */
    public SearchRequest withState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Set owner filter (for repository search)
     *
     * @param owner Repository owner
     * @return Current request object
     */
    public SearchRequest withOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Set language filter (for repository search)
     *
     * @param language Programming language
     * @return Current request object
     */
    public SearchRequest withLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Set fork filter (for repository search)
     *
     * @param fork Whether to include forks
     * @return Current request object
     */
    public SearchRequest withFork(String fork) {
        this.fork = fork;
        return this;
    }
}