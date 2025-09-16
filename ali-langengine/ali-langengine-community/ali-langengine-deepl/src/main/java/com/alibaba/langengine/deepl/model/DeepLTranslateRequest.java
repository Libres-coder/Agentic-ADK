package com.alibaba.langengine.deepl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepL 翻译请求
 *
 * @author Makoto
 */
@Data
public class DeepLTranslateRequest {
    
    @JsonProperty("text")
    private List<String> text;
    
    @JsonProperty("source_lang")
    private String sourceLang;
    
    @JsonProperty("target_lang")
    private String targetLang;
    
    @JsonProperty("split_sentences")
    private String splitSentences = "1";
    
    @JsonProperty("preserve_formatting")
    private String preserveFormatting = "0";
    
    @JsonProperty("formality")
    private String formality;
    
    @JsonProperty("glossary_id")
    private String glossaryId;
    
    @JsonProperty("tag_handling")
    private String tagHandling;
    
    @JsonProperty("non_splitting_tags")
    private List<String> nonSplittingTags;
    
    @JsonProperty("outline_detection")
    private String outlineDetection = "1";
    
    @JsonProperty("splitting_tags")
    private List<String> splittingTags;
    
    @JsonProperty("ignore_tags")
    private List<String> ignoreTags;
}
