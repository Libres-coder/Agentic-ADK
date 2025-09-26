package com.alibaba.langengine.rockset.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class RocksetQueryRequest {
    
    @JsonProperty("sql")
    private String sql;
    
    @JsonProperty("parameters")
    private List<Parameter> parameters;
    
    @Data
    public static class Parameter {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("value")
        private Object value;
        
        @JsonProperty("type")
        private String type;
    }
}
