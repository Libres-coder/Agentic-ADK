package com.alibaba.langengine.perplexity.sdk;

/**
 * This interface defines the Perplexity Search models.
 * Perplexity offers different models optimized for various search and reasoning tasks.
 */
public interface PerplexityModelConstant {
    
    /**
     * Sonar - Lightweight search model
     * Optimized for quick searches with balanced performance and cost
     */
    String SONAR = "sonar";
    
    /**
     * Sonar Pro - Advanced search model  
     * Enhanced search capabilities with higher quality results
     */
    String SONAR_PRO = "sonar-pro";
    
    /**
     * Sonar Deep Research - Exhaustive research model
     * Designed for comprehensive, in-depth research tasks
     * Supports reasoning_effort parameter (low/medium/high)
     */
    String SONAR_DEEP_RESEARCH = "sonar-deep-research";
    
    /**
     * Sonar Reasoning - Fast reasoning model
     * Optimized for quick reasoning and analysis tasks
     */
    String SONAR_REASONING = "sonar-reasoning";
    
    /**
     * Sonar Reasoning Pro - Premier reasoning model
     * Advanced reasoning capabilities with highest quality outputs
     */
    String SONAR_REASONING_PRO = "sonar-reasoning-pro";
}
