package com.alibaba.langengine.volcenginetranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 火山翻译请求
 *
 * @author Makoto
 */
@Data
public class VolcengineTranslateRequest {
    
    @JsonProperty("SourceLanguage")
    private String sourceLanguage;
    
    @JsonProperty("TargetLanguage")
    private String targetLanguage;
    
    @JsonProperty("TextList")
    private List<String> textList;
    
    @JsonProperty("Scene")
    private String scene = "general";
    
    @JsonProperty("GlossaryInfo")
    private GlossaryInfo glossaryInfo;
    
    @Data
    public static class GlossaryInfo {
        @JsonProperty("GlossaryTmx")
        private String glossaryTmx;
    }
}
