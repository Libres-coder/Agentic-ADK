package com.alibaba.langgengine.kagi.sdk;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a single search object from the Kagi API response.
 * Can be a search result (t=0) or related searches (t=1).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResult {

    @JsonProperty("t")
    private int type; // Use a more descriptive name in Java

    // Fields for Search Result (t=0)
    @JsonProperty("url")
    private String url;

    @JsonProperty("title")
    private String title;

    @JsonProperty("snippet")
    private String snippet;

    @JsonProperty("published")
    private String published;

    @JsonProperty("thumbnail")
    private Image thumbnail;
    
    // Field for Related Searches (t=1)
    @JsonProperty("list")
    private List<String> list;

    // Getters and Setters
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        if (type == 0) {
            // Search result
            return "SearchResult{type=0, title='" + title + "', url='" + url + "', snippet='" + 
                   (snippet != null && snippet.length() > 100 ? snippet.substring(0, 100) + "..." : snippet) + "'}";
        } else if (type == 1) {
            // Related searches
            return "SearchResult{type=1, relatedSearches=" + list + "}";
        }
        return "SearchResult{type=" + type + "}";
    }
}
