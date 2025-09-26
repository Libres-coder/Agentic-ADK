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
package com.alibaba.langengine.rockset.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class RocksetCreateCollectionResponse {
    
    @JsonProperty("data")
    private CollectionData data;
    
    @Data
    public static class CollectionData {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("workspace")
        private String workspace;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("created_at")
        private String createdAt;
    }
}
