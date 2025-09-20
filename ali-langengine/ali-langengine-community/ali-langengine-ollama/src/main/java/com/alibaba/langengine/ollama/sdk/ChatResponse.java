package com.alibaba.langengine.ollama.sdk;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    
    /**
     * The model name
     */
    private String model;
    
    /**
     * The time the response was created
     */
    private String created_at;
    
    /**
     * The message object
     */
    private Message message;
    
    /**
     * Whether the response is done
     */
    private Boolean done;
    
    /**
     * Total duration in nanoseconds
     */
    private Long total_duration;
    
    /**
     * Load duration in nanoseconds
     */
    private Long load_duration;
    
    /**
     * Number of samples
     */
    private Integer sample_count;
    
    /**
     * Sample duration in nanoseconds
     */
    private Long sample_duration;
    
    /**
     * Prompt evaluation count
     */
    private Integer prompt_eval_count;
    
    /**
     * Prompt evaluation duration in nanoseconds
     */
    private Long prompt_eval_duration;
    
    /**
     * Evaluation count
     */
    private Integer eval_count;
    
    /**
     * Evaluation duration in nanoseconds
     */
    private Long eval_duration;
    
    /**
     * Error message if any
     */
    private String error;
    
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
    }
}