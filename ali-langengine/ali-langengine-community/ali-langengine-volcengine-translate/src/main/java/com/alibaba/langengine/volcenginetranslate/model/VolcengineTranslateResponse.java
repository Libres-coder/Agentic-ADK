package com.alibaba.langengine.volcenginetranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 火山翻译响应
 *
 * @author Makoto
 */
@Data
public class VolcengineTranslateResponse {
    
    @JsonProperty("ResponseMetadata")
    private ResponseMetadata responseMetadata;
    
    @JsonProperty("Result")
    private Result result;
    
    @Data
    public static class ResponseMetadata {
        @JsonProperty("RequestId")
        private String requestId;
        
        @JsonProperty("Action")
        private String action;
        
        @JsonProperty("Version")
        private String version;
        
        @JsonProperty("Service")
        private String service;
        
        @JsonProperty("Region")
        private String region;
        
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
    
    @Data
    public static class Result {
        @JsonProperty("TranslationList")
        private List<Translation> translationList;
        
        @JsonProperty("DetectedLanguage")
        private String detectedLanguage;
    }
    
    @Data
    public static class Translation {
        @JsonProperty("Translation")
        private String translation;
        
        @JsonProperty("DetectedSourceLanguage")
        private String detectedSourceLanguage;
        
        @JsonProperty("Extra")
        private String extra;
    }
}
