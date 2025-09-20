package com.alibaba.langengine.perplexity.sdk.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Summary information for async job in list responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsyncJobSummary {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("created_at")
    private Long createdAt;
    
    @JsonProperty("started_at")
    private Long startedAt;
    
    @JsonProperty("completed_at")
    private Long completedAt;
    
    @JsonProperty("failed_at")
    private Long failedAt;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("status")
    private String status;
    
    public AsyncJobSummary() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(Long startedAt) {
        this.startedAt = startedAt;
    }
    
    public Long getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }
    
    public Long getFailedAt() {
        return failedAt;
    }
    
    public void setFailedAt(Long failedAt) {
        this.failedAt = failedAt;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}