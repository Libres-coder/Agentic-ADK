package com.alibaba.langengine.microsofttranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Microsoft 翻译请求
 *
 * @author Makoto
 */
@Data
public class MicrosoftTranslateRequest {
    
    @JsonProperty("text")
    private List<String> text;
    
    @JsonProperty("to")
    private String to;
    
    @JsonProperty("from")
    private String from;
    
    @JsonProperty("textType")
    private String textType = "Plain";
    
    @JsonProperty("category")
    private String category = "general";
    
    @JsonProperty("profanityAction")
    private String profanityAction = "NoAction";
    
    @JsonProperty("profanityMarker")
    private String profanityMarker = "Asterisk";
    
    @JsonProperty("includeAlignment")
    private boolean includeAlignment = false;
    
    @JsonProperty("includeSentenceLength")
    private boolean includeSentenceLength = false;
    
    @JsonProperty("suggestedFrom")
    private String suggestedFrom;
    
    @JsonProperty("fromScript")
    private String fromScript;
    
    @JsonProperty("toScript")
    private String toScript;
    
    @JsonProperty("allowFallback")
    private boolean allowFallback = true;
}
