package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

@Data
public class PullRequest {
    
    /**
     * The name of the model to pull
     */
    private String name;
    
    /**
     * If false the response will be returned as a single response object, rather than a stream of objects
     */
    private Boolean stream = true;
    
    /**
     * Allow insecure connections to the library registry
     */
    private Boolean insecure;
    
    /**
     * Username for authenticating to the library registry
     */
    private String username;
    
    /**
     * Password for authenticating to the library registry
     */
    private String password;
}