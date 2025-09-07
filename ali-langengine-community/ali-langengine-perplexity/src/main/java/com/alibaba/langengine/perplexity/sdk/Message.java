package com.alibaba.langengine.perplexity.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a message in a Perplexity chat completion conversation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("content")
    private Object content;
    
    public Message() {
    }
    
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
    
    public Message(String role, List<MessageContentPart> content) {
        this.role = role;
        this.content = content;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Object getContent() {
        return content;
    }
    
    public void setContent(Object content) {
        this.content = content;
    }
    
    public String getTextContent() {
        if (content instanceof String) {
            return (String) content;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public List<MessageContentPart> getMultimodalContent() {
        if (content instanceof List) {
            return (List<MessageContentPart>) content;
        }
        return null;
    }
    
    public static class Role {
        public static final String SYSTEM = "system";
        public static final String USER = "user";
        public static final String ASSISTANT = "assistant";
    }
}