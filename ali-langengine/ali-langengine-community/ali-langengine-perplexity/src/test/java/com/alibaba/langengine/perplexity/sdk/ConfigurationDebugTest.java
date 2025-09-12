package com.alibaba.langengine.perplexity.sdk;

import com.alibaba.langengine.perplexity.PerplexityConfiguration;
import org.junit.jupiter.api.Test;

/**
 * Debug test to check configuration loading
 */
public class ConfigurationDebugTest {
    
    @Test
    void debugConfigurationLoading() {
        System.out.println("=== Configuration Debug Info ===");
        System.out.println("API Key from config: '" + PerplexityConfiguration.PERPLEXITY_API_KEY + "'");
        System.out.println("API Key is null: " + (PerplexityConfiguration.PERPLEXITY_API_KEY == null));
        System.out.println("API Key is empty: " + (PerplexityConfiguration.PERPLEXITY_API_KEY != null && PerplexityConfiguration.PERPLEXITY_API_KEY.trim().isEmpty()));
        System.out.println("API Key length: " + (PerplexityConfiguration.PERPLEXITY_API_KEY != null ? PerplexityConfiguration.PERPLEXITY_API_KEY.length() : "null"));
        
        // Check if it contains default values
        if (PerplexityConfiguration.PERPLEXITY_API_KEY != null) {
            System.out.println("Contains 'your_perplexity_api_key_here': " + PerplexityConfiguration.PERPLEXITY_API_KEY.equals("your_perplexity_api_key_here"));
            System.out.println("Contains 'your_test_api_key_here': " + PerplexityConfiguration.PERPLEXITY_API_KEY.equals("your_test_api_key_here"));
            System.out.println("Starts with 'pplx-': " + PerplexityConfiguration.PERPLEXITY_API_KEY.startsWith("pplx-"));
        }
        
        System.out.println("Base URL: " + PerplexityConfiguration.PERPLEXITY_API_URL);
        System.out.println("=======================");
    }
}