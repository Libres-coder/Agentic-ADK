package com.alibaba.langengine.ollama.sdk;

public class OllamaException extends RuntimeException {
    
    public OllamaException(String message) {
        super(message);
    }
    
    public OllamaException(String message, Throwable cause) {
        super(message, cause);
    }
}