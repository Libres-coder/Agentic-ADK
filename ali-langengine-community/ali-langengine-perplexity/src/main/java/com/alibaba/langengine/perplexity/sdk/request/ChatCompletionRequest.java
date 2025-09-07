package com.alibaba.langengine.perplexity.sdk.request;

import com.alibaba.langengine.perplexity.sdk.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Request for Perplexity chat completion API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("messages")
    private List<Message> messages;
    
    @JsonProperty("search_mode")
    private String searchMode;
    
    @JsonProperty("reasoning_effort")
    private String reasoningEffort;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    @JsonProperty("temperature")
    private Double temperature;
    
    @JsonProperty("top_p")
    private Double topP;
    
    @JsonProperty("search_domain_filter")
    private List<String> searchDomainFilter;
    
    @JsonProperty("return_images")
    private Boolean returnImages;
    
    @JsonProperty("return_related_questions")
    private Boolean returnRelatedQuestions;
    
    @JsonProperty("search_recency_filter")
    private String searchRecencyFilter;
    
    @JsonProperty("search_after_date_filter")
    private String searchAfterDateFilter;
    
    @JsonProperty("search_before_date_filter")
    private String searchBeforeDateFilter;
    
    @JsonProperty("last_updated_after_filter")
    private String lastUpdatedAfterFilter;
    
    @JsonProperty("last_updated_before_filter")
    private String lastUpdatedBeforeFilter;
    
    @JsonProperty("top_k")
    private Double topK;
    
    @JsonProperty("stream")
    private Boolean stream;
    
    @JsonProperty("presence_penalty")
    private Double presencePenalty;
    
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;
    
    @JsonProperty("response_format")
    private Map<String, Object> responseFormat;
    
    @JsonProperty("disable_search")
    private Boolean disableSearch;
    
    @JsonProperty("enable_search_classifier")
    private Boolean enableSearchClassifier;
    
    @JsonProperty("web_search_options")
    private WebSearchOptions webSearchOptions;
    
    public ChatCompletionRequest() {
    }
    
    // Getters and setters
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public String getSearchMode() {
        return searchMode;
    }
    
    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }
    
    public String getReasoningEffort() {
        return reasoningEffort;
    }
    
    public void setReasoningEffort(String reasoningEffort) {
        this.reasoningEffort = reasoningEffort;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Double getTopP() {
        return topP;
    }
    
    public void setTopP(Double topP) {
        this.topP = topP;
    }
    
    public List<String> getSearchDomainFilter() {
        return searchDomainFilter;
    }
    
    public void setSearchDomainFilter(List<String> searchDomainFilter) {
        this.searchDomainFilter = searchDomainFilter;
    }
    
    public Boolean getReturnImages() {
        return returnImages;
    }
    
    public void setReturnImages(Boolean returnImages) {
        this.returnImages = returnImages;
    }
    
    public Boolean getReturnRelatedQuestions() {
        return returnRelatedQuestions;
    }
    
    public void setReturnRelatedQuestions(Boolean returnRelatedQuestions) {
        this.returnRelatedQuestions = returnRelatedQuestions;
    }
    
    public String getSearchRecencyFilter() {
        return searchRecencyFilter;
    }
    
    public void setSearchRecencyFilter(String searchRecencyFilter) {
        this.searchRecencyFilter = searchRecencyFilter;
    }
    
    public String getSearchAfterDateFilter() {
        return searchAfterDateFilter;
    }
    
    public void setSearchAfterDateFilter(String searchAfterDateFilter) {
        this.searchAfterDateFilter = searchAfterDateFilter;
    }
    
    public String getSearchBeforeDateFilter() {
        return searchBeforeDateFilter;
    }
    
    public void setSearchBeforeDateFilter(String searchBeforeDateFilter) {
        this.searchBeforeDateFilter = searchBeforeDateFilter;
    }
    
    public String getLastUpdatedAfterFilter() {
        return lastUpdatedAfterFilter;
    }
    
    public void setLastUpdatedAfterFilter(String lastUpdatedAfterFilter) {
        this.lastUpdatedAfterFilter = lastUpdatedAfterFilter;
    }
    
    public String getLastUpdatedBeforeFilter() {
        return lastUpdatedBeforeFilter;
    }
    
    public void setLastUpdatedBeforeFilter(String lastUpdatedBeforeFilter) {
        this.lastUpdatedBeforeFilter = lastUpdatedBeforeFilter;
    }
    
    public Double getTopK() {
        return topK;
    }
    
    public void setTopK(Double topK) {
        this.topK = topK;
    }
    
    public Boolean getStream() {
        return stream;
    }
    
    public void setStream(Boolean stream) {
        this.stream = stream;
    }
    
    public Double getPresencePenalty() {
        return presencePenalty;
    }
    
    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }
    
    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }
    
    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
    
    public Map<String, Object> getResponseFormat() {
        return responseFormat;
    }
    
    public void setResponseFormat(Map<String, Object> responseFormat) {
        this.responseFormat = responseFormat;
    }
    
    public Boolean getDisableSearch() {
        return disableSearch;
    }
    
    public void setDisableSearch(Boolean disableSearch) {
        this.disableSearch = disableSearch;
    }
    
    public Boolean getEnableSearchClassifier() {
        return enableSearchClassifier;
    }
    
    public void setEnableSearchClassifier(Boolean enableSearchClassifier) {
        this.enableSearchClassifier = enableSearchClassifier;
    }
    
    public WebSearchOptions getWebSearchOptions() {
        return webSearchOptions;
    }
    
    public void setWebSearchOptions(WebSearchOptions webSearchOptions) {
        this.webSearchOptions = webSearchOptions;
    }
    
    // Constants for enum values
    public static class SearchMode {
        public static final String WEB = "web";
        public static final String ACADEMIC = "academic";
    }
    
    public static class ReasoningEffort {
        public static final String LOW = "low";
        public static final String MEDIUM = "medium";
        public static final String HIGH = "high";
    }
}