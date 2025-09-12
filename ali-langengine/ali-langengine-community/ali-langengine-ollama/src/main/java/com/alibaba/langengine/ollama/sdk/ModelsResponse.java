package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

import java.util.List;

@Data
public class ModelsResponse {
    
    /**
     * List of models
     */
    private List<Model> models;
    
    @Data
    public static class Model {
        
        /**
         * The name of the model
         */
        private String name;
        
        /**
         * The model's size in bytes
         */
        private Long size;
        
        /**
         * The time the model was last modified
         */
        private String modified_at;
        
        /**
         * The model's digest
         */
        private String digest;
    }
}