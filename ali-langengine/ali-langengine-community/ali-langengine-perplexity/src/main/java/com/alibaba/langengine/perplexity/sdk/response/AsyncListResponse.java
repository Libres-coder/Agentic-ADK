package com.alibaba.langengine.perplexity.sdk.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response for listing async chat completion jobs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsyncListResponse {
    
    @JsonProperty("next_token")
    private String nextToken;
    
    @JsonProperty("requests")
    private List<AsyncJobSummary> requests;
    
    public AsyncListResponse() {
    }
    
    public String getNextToken() {
        return nextToken;
    }
    
    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }
    
    public List<AsyncJobSummary> getRequests() {
        return requests;
    }
    
    public void setRequests(List<AsyncJobSummary> requests) {
        this.requests = requests;
    }
}