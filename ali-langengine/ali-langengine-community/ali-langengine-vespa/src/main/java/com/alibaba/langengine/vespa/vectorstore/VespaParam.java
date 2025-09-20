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
package com.alibaba.langengine.vespa.vectorstore;

import lombok.Data;


@Data
public class VespaParam {
    
    private String namespace = "default";
    
    private String documentType = "document";
    
    private String fieldNamePageContent = "page_content";
    
    private String fieldNameUniqueId = "content_id";
    
    private String fieldMeta = "meta_data";
    
    private String fieldNameVector = "vector";
    
    private IndexParam indexParam = new IndexParam();

    @Data
    public static class IndexParam {
        
        private String vectorDistance = "angular";
        
        private Integer maxLinksPerNode = 16;
        
        private Integer neighborsToExploreAtInsert = 200;
        
        private Boolean multiThreadedIndexing = true;
        
        private Integer vectorDimensions = 1536;
    }
}
