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
package com.alibaba.langengine.brave.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a search request to the Brave Search API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {
    @JsonProperty("q")
    private String query;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("safesearch")
    private String safesearch;

    @JsonProperty("country")
    private String country;

    @JsonProperty("search_lang")
    private String searchLang;

    @JsonProperty("ui_lang")
    private String uiLang;

    @JsonProperty("spellcheck")
    private Integer spellcheck;

    @JsonProperty("result_filter")
    private String resultFilter;

    // Getters and Setters

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getSafesearch() {
        return safesearch;
    }

    public void setSafesearch(String safesearch) {
        this.safesearch = safesearch;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSearchLang() {
        return searchLang;
    }

    public void setSearchLang(String searchLang) {
        this.searchLang = searchLang;
    }

    public String getUiLang() {
        return uiLang;
    }

    public void setUiLang(String uiLang) {
        this.uiLang = uiLang;
    }

    public Integer getSpellcheck() {
        return spellcheck;
    }

    public void setSpellcheck(Integer spellcheck) {
        this.spellcheck = spellcheck;
    }

    public String getResultFilter() {
        return resultFilter;
    }

    public void setResultFilter(String resultFilter) {
        this.resultFilter = resultFilter;
    }
}