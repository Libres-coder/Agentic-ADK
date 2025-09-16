package com.alibaba.langengine.microsofttranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Microsoft 翻译响应
 *
 * @author Makoto
 */
@Data
public class MicrosoftTranslateResponse {
    
    @JsonProperty("detectedLanguage")
    private DetectedLanguage detectedLanguage;
    
    @JsonProperty("translations")
    private List<Translation> translations;
    
    @Data
    public static class DetectedLanguage {
        @JsonProperty("language")
        private String language;
        
        @JsonProperty("score")
        private double score;
    }
    
    @Data
    public static class Translation {
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("to")
        private String to;
        
        @JsonProperty("alignment")
        private Alignment alignment;
        
        @JsonProperty("sentLen")
        private SentenceLength sentLen;
    }
    
    @Data
    public static class Alignment {
        @JsonProperty("proj")
        private String proj;
    }
    
    @Data
    public static class SentenceLength {
        @JsonProperty("srcSentLen")
        private List<Integer> srcSentLen;
        
        @JsonProperty("transSentLen")
        private List<Integer> transSentLen;
    }
}
