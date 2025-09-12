package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

import java.util.Map;

@Data
public class ShowResponse {
    
    /**
     * The license of the model
     */
    private String license;
    
    /**
     * The modelfile of the model
     */
    private String modelfile;
    
    /**
     * The parameters of the model
     */
    private String parameters;
    
    /**
     * The template of the model
     */
    private String template;
    
    /**
     * The details of the model
     */
    private Map<String, Object> details;
}