package com.alibaba.langengine.ollama.sdk;

/**
 * Callback interface for handling streaming responses from Ollama API
 *
 * @param <T> The type of response object
 */
public interface OllamaStreamCallback<T> {
    
    /**
     * Called when a partial response is received
     *
     * @param response The partial response
     */
    void onPartialResponse(T response);
    
    /**
     * Called when the stream is completed
     */
    void onComplete();
    
    /**
     * Called when an error occurs during streaming
     *
     * @param throwable The error that occurred
     */
    void onError(Throwable throwable);
}