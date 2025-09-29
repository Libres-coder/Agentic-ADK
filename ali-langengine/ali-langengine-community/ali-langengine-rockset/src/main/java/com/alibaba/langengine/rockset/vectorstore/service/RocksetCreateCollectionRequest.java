package com.alibaba.langengine.rockset.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class RocksetCreateCollectionRequest {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("field_mappings")
    private FieldMapping[] fieldMappings;
    
    @JsonProperty("retention_secs")
    private Long retentionSecs;
    
    /**
     * Field mapping configuration
     */
    @Data
    public static class FieldMapping {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("is_drop")
        private Boolean isDrop;
    }
}
