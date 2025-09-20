package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GenerateRequest {
    
    /**
     * The model name
     */
    private String model;
    
    /**
     * The prompt to generate a response for
     */
    private String prompt;
    
    /**
     * The text that comes after the inserted text
     */
    private String suffix;
    
    /**
     * A list of base64-encoded images (for multimodal models such as llava)
     */
    private List<String> images;
    
    /**
     * Additional model parameters listed in the documentation for the Modelfile
     */
    private Map<String, Object> options;
    
    /**
     * The system prompt to use
     */
    private String system;
    
    /**
     * The full prompt or prompt template (overrides what is defined in the Modelfile)
     */
    private String template;
    
    /**
     * If false the response will be returned as a single response object, rather than a stream of objects
     */
    private Boolean stream = true;
    
    /**
     * Additional context from previous generation
     */
    private List<Integer> context;
    
    /**
     * Controls how long the model will stay loaded into memory following the request (default: 5m)
     */
    private String keep_alive;
}