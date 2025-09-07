package com.alibaba.langengine.perplexity.sdk.response;

import com.alibaba.langengine.perplexity.sdk.Choice;
import com.alibaba.langengine.perplexity.sdk.SearchResult;
import com.alibaba.langengine.perplexity.sdk.UsageInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response from Perplexity chat completion API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("created")
    private Long created;
    
    @JsonProperty("usage")
    private UsageInfo usage;
    
    @JsonProperty("object")
    private String object;
    
    @JsonProperty("choices")
    private List<Choice> choices;
    
    @JsonProperty("search_results")
    private List<SearchResult> searchResults;
    
    @JsonProperty("citations")
    private List<Object> citations;
    
    public ChatCompletionResponse() {
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
    
    public Long getCreated() {
        return created;
    }
    
    public void setCreated(Long created) {
        this.created = created;
    }
    
    public UsageInfo getUsage() {
        return usage;
    }
    
    public void setUsage(UsageInfo usage) {
        this.usage = usage;
    }
    
    public String getObject() {
        return object;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    
    public List<SearchResult> getSearchResults() {
        return searchResults;
    }
    
    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }
    
    public List<Object> getCitations() {
        return citations;
    }
    
    public void setCitations(List<Object> citations) {
        this.citations = citations;
    }
    
    public static class ObjectType {
        public static final String CHAT_COMPLETION = "chat.completion";
        public static final String CHAT_COMPLETION_CHUNK = "chat.completion.chunk";
    }
}