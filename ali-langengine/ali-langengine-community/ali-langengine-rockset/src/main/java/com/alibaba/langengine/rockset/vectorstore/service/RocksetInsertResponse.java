package com.alibaba.langengine.rockset.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


@Data
public class RocksetInsertResponse {
    
    @JsonProperty("data")
    private List<InsertStatus> data;
    
    /**
     * Insert status for each document
     */
    @Data
    public static class InsertStatus {
        @JsonProperty("_id")
        private String id;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("error")
        private String error;
    }
    
    private int insertedCount;
    private String status;
}
