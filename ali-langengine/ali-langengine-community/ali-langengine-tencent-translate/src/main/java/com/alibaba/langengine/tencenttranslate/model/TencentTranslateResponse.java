package com.alibaba.langengine.tencenttranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 腾讯翻译响应
 *
 * @author Makoto
 */
@Data
public class TencentTranslateResponse {
    
    @JsonProperty("Response")
    private Response response;
    
    @Data
    public static class Response {
        @JsonProperty("TargetText")
        private String targetText;
        
        @JsonProperty("Source")
        private String source;
        
        @JsonProperty("Target")
        private String target;
        
        @JsonProperty("RequestId")
        private String requestId;
        
        @JsonProperty("Error")
        private Error error;
    }
    
    @Data
    public static class Error {
        @JsonProperty("Code")
        private String code;
        
        @JsonProperty("Message")
        private String message;
    }
}
