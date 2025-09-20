package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

@Data
public class DeleteRequest {
    
    /**
     * The name of the model to delete
     */
    private String name;
}