package com.alibaba.langengine.perplexity.sdk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a part of multimodal message content.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageContentPart {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("image_url")
    private ImageUrl imageUrl;
    
    public MessageContentPart() {
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public ImageUrl getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(ImageUrl imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public static class Type {
        public static final String TEXT = "text";
        public static final String IMAGE_URL = "image_url";
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImageUrl {
        @JsonProperty("url")
        private String url;
        
        public ImageUrl() {
        }
        
        public ImageUrl(String url) {
            this.url = url;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
    }
}