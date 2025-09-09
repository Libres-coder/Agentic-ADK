/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.linkedin.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("firstName")
    private Name firstName;
    
    @JsonProperty("lastName")
    private Name lastName;
    
    @JsonProperty("headline")
    private String headline;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("location")
    private Location location;
    
    @JsonProperty("industry")
    private String industry;
    
    @Data
    public static class Name {
        @JsonProperty("localized")
        private Localized localized;
        
        @JsonProperty("preferredLocale")
        private PreferredLocale preferredLocale;
    }
    
    @Data
    public static class Localized {
        @JsonProperty("en_US")
        private String enUs;
    }
    
    @Data
    public static class PreferredLocale {
        @JsonProperty("country")
        private String country;
        
        @JsonProperty("language")
        private String language;
    }
    
    @Data
    public static class Location {
        @JsonProperty("name")
        private String name;
    }
}