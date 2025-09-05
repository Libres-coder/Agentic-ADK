package com.alibaba.langgengine.kagi.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents the top-level response from the Kagi Search API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    @JsonProperty("meta")
    private Map<String, Object> meta;

    @JsonProperty("data")
    private List<SearchResult> data;

    // Getters and Setters
    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public List<SearchResult> getData() {
        return data;
    }

    public void setData(List<SearchResult> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SearchResponse{");
        if (meta != null) {
            sb.append("meta=").append(meta);
        }
        if (data != null && !data.isEmpty()) {
            sb.append(", results=").append(data.size()).append(" items");
            sb.append(", first result: ").append(data.get(0));
        }
        sb.append('}');
        return sb.toString();
    }
}
