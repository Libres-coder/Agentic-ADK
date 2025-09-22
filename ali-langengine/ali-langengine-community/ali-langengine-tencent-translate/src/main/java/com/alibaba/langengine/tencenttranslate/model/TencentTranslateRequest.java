package com.alibaba.langengine.tencenttranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 腾讯翻译请求
 *
 * @author Makoto
 */
@Data
public class TencentTranslateRequest {
    
    @JsonProperty("SourceText")
    private String sourceText;
    
    @JsonProperty("Source")
    private String source;
    
    @JsonProperty("Target")
    private String target;
    
    @JsonProperty("ProjectId")
    private Long projectId;
    
    @JsonProperty("UntranslatedText")
    private String untranslatedText;
}
