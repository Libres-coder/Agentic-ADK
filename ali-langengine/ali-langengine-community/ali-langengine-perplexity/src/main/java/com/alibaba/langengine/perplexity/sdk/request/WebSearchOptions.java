package com.alibaba.langengine.perplexity.sdk.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Web search options for Perplexity API requests.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSearchOptions {
    
    @JsonProperty("search_context_size")
    private String searchContextSize;
    
    @JsonProperty("user_location")
    private UserLocation userLocation;
    
    @JsonProperty("image_search_relevance_enhanced")
    private Boolean imageSearchRelevanceEnhanced;
    
    public WebSearchOptions() {
    }
    
    public String getSearchContextSize() {
        return searchContextSize;
    }
    
    public void setSearchContextSize(String searchContextSize) {
        this.searchContextSize = searchContextSize;
    }
    
    public UserLocation getUserLocation() {
        return userLocation;
    }
    
    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }
    
    public Boolean getImageSearchRelevanceEnhanced() {
        return imageSearchRelevanceEnhanced;
    }
    
    public void setImageSearchRelevanceEnhanced(Boolean imageSearchRelevanceEnhanced) {
        this.imageSearchRelevanceEnhanced = imageSearchRelevanceEnhanced;
    }
    
    public static class SearchContextSize {
        public static final String LOW = "low";
        public static final String MEDIUM = "medium";
        public static final String HIGH = "high";
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserLocation {
        @JsonProperty("latitude")
        private Double latitude;
        
        @JsonProperty("longitude")
        private Double longitude;
        
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("region")
        private String region;
        
        @JsonProperty("city")
        private String city;
        
        public UserLocation() {
        }
        
        public Double getLatitude() {
            return latitude;
        }
        
        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }
        
        public Double getLongitude() {
            return longitude;
        }
        
        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getRegion() {
            return region;
        }
        
        public void setRegion(String region) {
            this.region = region;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
    }
}