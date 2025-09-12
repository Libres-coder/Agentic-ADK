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
package com.alibaba.langengine.instagram.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MediaResponse {
    
    @JsonProperty("data")
    private List<Media> data;
    
    @JsonProperty("paging")
    private Paging paging;
    
    @Data
    public static class Media {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("caption")
        private String caption;
        
        @JsonProperty("media_type")
        private String mediaType;
        
        @JsonProperty("media_url")
        private String mediaUrl;
        
        @JsonProperty("thumbnail_url")
        private String thumbnailUrl;
        
        @JsonProperty("permalink")
        private String permalink;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
    
    @Data
    public static class Paging {
        @JsonProperty("cursors")
        private Cursors cursors;
        
        @JsonProperty("next")
        private String next;
    }
    
    @Data
    public static class Cursors {
        @JsonProperty("before")
        private String before;
        
        @JsonProperty("after")
        private String after;
    }
}