package com.alibaba.langengine.rockset.vectorstore.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class RocksetQueryResponse {
    
    @JsonProperty("results")
    private List<Map<String, Object>> results;
    
    @JsonProperty("query_id")
    private String queryId;
    
    @JsonProperty("stats")
    private QueryStats stats;
    
    /**
     * Query statistics
     */
    @Data
    public static class QueryStats {
        @JsonProperty("elapsed_time_ms")
        private Long elapsedTimeMs;
        
        @JsonProperty("throttled_time_micros")
        private Long throttledTimeMicros;
        
        @JsonProperty("total_scanned_bytes")
        private Long totalScannedBytes;
    }
}
