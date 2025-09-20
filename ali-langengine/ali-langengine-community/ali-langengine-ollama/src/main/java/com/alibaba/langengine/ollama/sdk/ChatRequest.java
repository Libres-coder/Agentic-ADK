package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    
    /**
     * The model name
     */
    private String model;
    
    /**
     * The messages of the chat, this can be used to keep a chat memory
     */
    private List<Message> messages;
    
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
     * Controls how long the model will stay loaded into memory following the request (default: 5m)
     */
    private String keep_alive;
    
    /**
     * Format of the response
     */
    private String format;
    
    @Data
    public static class Message {
        /**
         * The role of the message
         */
        private String role;
        
        /**
         * The content of the message
         */
        private String content;
        
        /**
         * Base64-encoded images (for multimodal models such as llava)
         */
        private List<String> images;
    }
}