package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

@Data
public class CopyRequest {
    
    /**
     * The name of the model to copy from
     */
    private String source;
    
    /**
     * The name of the model to copy to
     */
    private String destination;
}