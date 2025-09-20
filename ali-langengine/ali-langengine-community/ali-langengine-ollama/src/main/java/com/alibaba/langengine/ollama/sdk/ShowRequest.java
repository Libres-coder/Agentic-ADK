package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

@Data
public class ShowRequest {
    
    /**
     * The name of the model to show
     */
    private String name;
    
    /**
     * Format of the response
     */
    private String format;
}