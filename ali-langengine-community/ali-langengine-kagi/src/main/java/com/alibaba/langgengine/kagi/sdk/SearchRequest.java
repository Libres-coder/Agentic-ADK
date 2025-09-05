package com.alibaba.langgengine.kagi.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;



/**
 * Request object for kagi search
 */
public class SearchRequest {
    @JsonProperty("q")
    private String query;

    @JsonProperty("limit")
    private Integer limit;


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}
