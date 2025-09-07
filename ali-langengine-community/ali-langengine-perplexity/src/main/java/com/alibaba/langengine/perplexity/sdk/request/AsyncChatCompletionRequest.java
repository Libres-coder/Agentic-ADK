package com.alibaba.langengine.perplexity.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request wrapper for async chat completion API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsyncChatCompletionRequest {
    
    @JsonProperty("request")
    private ChatCompletionRequest request;
    
    public AsyncChatCompletionRequest() {
    }
    
    public AsyncChatCompletionRequest(ChatCompletionRequest request) {
        this.request = request;
    }
    
    public ChatCompletionRequest getRequest() {
        return request;
    }
    
    public void setRequest(ChatCompletionRequest request) {
        this.request = request;
    }
}