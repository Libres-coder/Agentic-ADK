package com.alibaba.langengine.perplexity.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents usage information for API requests.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsageInfo {
    
    @JsonProperty("prompt_tokens")
    private Integer promptTokens;
    
    @JsonProperty("completion_tokens")
    private Integer completionTokens;
    
    @JsonProperty("total_tokens")
    private Integer totalTokens;
    
    @JsonProperty("search_context_size")
    private String searchContextSize;
    
    @JsonProperty("citation_tokens")
    private Integer citationTokens;
    
    @JsonProperty("num_search_queries")
    private Integer numSearchQueries;
    
    @JsonProperty("reasoning_tokens")
    private Integer reasoningTokens;
    
    @JsonProperty("cost")
    private Object cost;
    
    public UsageInfo() {
    }
    
    public Integer getPromptTokens() {
        return promptTokens;
    }
    
    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }
    
    public Integer getCompletionTokens() {
        return completionTokens;
    }
    
    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }
    
    public Integer getTotalTokens() {
        return totalTokens;
    }
    
    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }
    
    public String getSearchContextSize() {
        return searchContextSize;
    }
    
    public void setSearchContextSize(String searchContextSize) {
        this.searchContextSize = searchContextSize;
    }
    
    public Integer getCitationTokens() {
        return citationTokens;
    }
    
    public void setCitationTokens(Integer citationTokens) {
        this.citationTokens = citationTokens;
    }
    
    public Integer getNumSearchQueries() {
        return numSearchQueries;
    }
    
    public void setNumSearchQueries(Integer numSearchQueries) {
        this.numSearchQueries = numSearchQueries;
    }
    
    public Integer getReasoningTokens() {
        return reasoningTokens;
    }
    
    public void setReasoningTokens(Integer reasoningTokens) {
        this.reasoningTokens = reasoningTokens;
    }
    
    public Object getCost() {
        return cost;
    }
    
    public void setCost(Object cost) {
        this.cost = cost;
    }
}