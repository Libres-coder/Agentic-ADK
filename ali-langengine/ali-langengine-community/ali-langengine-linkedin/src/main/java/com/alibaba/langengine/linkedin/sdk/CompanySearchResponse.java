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

import java.util.List;

@Data
public class CompanySearchResponse {
    
    @JsonProperty("elements")
    private List<Company> elements;
    
    @JsonProperty("paging")
    private Paging paging;
    
    @Data
    public static class Company {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("websiteUrl")
        private String websiteUrl;
        
        @JsonProperty("industry")
        private String industry;
        
        @JsonProperty("companySize")
        private String companySize;
        
        @JsonProperty("logoUrl")
        private String logoUrl;
    }
    
    @Data
    public static class Paging {
        @JsonProperty("count")
        private int count;
        
        @JsonProperty("start")
        private int start;
        
        @JsonProperty("links")
        private List<Link> links;
    }
    
    @Data
    public static class Link {
        @JsonProperty("rel")
        private String rel;
        
        @JsonProperty("href")
        private String href;
    }
}