package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

@Data
public class CreateRequest {
    
    /**
     * The name of the model to create
     */
    private String name;
    
    /**
     * The path to the Modelfile
     */
    private String path;
    
    /**
     * The model definition
     */
    private String modelfile;
    
    /**
     * If false the response will be returned as a single response object, rather than a stream of objects
     */
    private Boolean stream = true;
}