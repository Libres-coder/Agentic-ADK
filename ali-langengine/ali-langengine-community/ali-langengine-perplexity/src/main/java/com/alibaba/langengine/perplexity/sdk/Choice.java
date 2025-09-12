package com.alibaba.langengine.perplexity.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a choice in the chat completion response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Choice {
    
    @JsonProperty("index")
    private Integer index;
    
    @JsonProperty("message")
    private Message message;
    
    @JsonProperty("delta")
    private Message delta;
    
    @JsonProperty("finish_reason")
    private String finishReason;
    
    public Choice() {
    }
    
    public Integer getIndex() {
        return index;
    }
    
    public void setIndex(Integer index) {
        this.index = index;
    }
    
    public Message getMessage() {
        return message;
    }
    
    public void setMessage(Message message) {
        this.message = message;
    }
    
    public Message getDelta() {
        return delta;
    }
    
    public void setDelta(Message delta) {
        this.delta = delta;
    }
    
    public String getFinishReason() {
        return finishReason;
    }
    
    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
    
    public static class FinishReason {
        public static final String STOP = "stop";
        public static final String LENGTH = "length";
    }
}