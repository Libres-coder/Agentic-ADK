package com.alibaba.langengine.perplexity.sdk.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for async chat completion job operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsyncJobResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("created_at")
    private Long createdAt;
    
    @JsonProperty("started_at")
    private Long startedAt;
    
    @JsonProperty("completed_at")
    private Long completedAt;
    
    @JsonProperty("response")
    private ChatCompletionResponse response;
    
    @JsonProperty("failed_at")
    private Long failedAt;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("status")
    private String status;
    
    public AsyncJobResponse() {
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
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
    
    public ChatCompletionResponse getResponse() {
        return response;
    }
    
    public void setResponse(ChatCompletionResponse response) {
        this.response = response;
    }
    
    public Long getFailedAt() {
        return failedAt;
    }
    
    public void setFailedAt(Long failedAt) {
        this.failedAt = failedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public static class Status {
        public static final String CREATED = "CREATED";
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String COMPLETED = "COMPLETED";
        public static final String FAILED = "FAILED";
    }
}