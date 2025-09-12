package com.alibaba.langengine.perplexity.sdk.request;

/**
 * Request for listing async chat completion jobs.
 */
public class AsyncListRequest {
    
    private Integer limit;
    private String nextToken;
    
    public AsyncListRequest() {
    }
    
    public AsyncListRequest(Integer limit) {
        this.limit = limit;
    }
    
    public AsyncListRequest(Integer limit, String nextToken) {
        this.limit = limit;
        this.nextToken = nextToken;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public String getNextToken() {
        return nextToken;
    }
    
    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }
}