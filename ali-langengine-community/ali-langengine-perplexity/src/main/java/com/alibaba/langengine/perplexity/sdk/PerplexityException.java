package com.alibaba.langengine.perplexity.sdk;

/**
 * Exception thrown when Perplexity API operations fail.
 */
public class PerplexityException extends RuntimeException {
    
    private int httpStatusCode;
    private String responseBody;
    
    public PerplexityException(String message) {
        super(message);
    }
    
    public PerplexityException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PerplexityException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
    
    public PerplexityException(String message, int httpStatusCode, String responseBody) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
    }
    
    public PerplexityException(String message, int httpStatusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = responseBody;
    }
    
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
}