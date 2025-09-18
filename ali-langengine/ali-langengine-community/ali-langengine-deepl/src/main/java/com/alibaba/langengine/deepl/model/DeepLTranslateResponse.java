package com.alibaba.langengine.deepl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepL 翻译响应
 *
 * @author Makoto
 */
@Data
public class DeepLTranslateResponse {
    
    @JsonProperty("translations")
    private List<Translation> translations;
    
    @Data
    public static class Translation {
        @JsonProperty("detected_source_language")
        private String detectedSourceLanguage;
        
        @JsonProperty("text")
        private String text;
    }
}
