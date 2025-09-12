package com.alibaba.langengine.perplexity.sdk;


public interface PerplexityConstant {
    /**
     * Perplexity Search API base URL
     */

    String BASE_URL="https://api.perplexity.ai/";

    /**
     * Perplexity Search sync search API endpoint
     */
    String SYNC_SEARCH_ENDPOINT="chat/completions";

    /**
     * Perplexity Search async search tasks create API endpoint
     */
    String ASYNC_SEARCH_ENDPOINT="async/chat/completions";

    /**
     * Perplexity Search async search tasks List API endpoint
     */
    String ASYNC_SEARCH_List_ENDPOINT="async/chat/completions";

    /**
     * Perplexity Search async search tasks result API endpoint
     */
    String ASYNC_SEARCH_RESULT_ENDPOINT="async/chat/completions/{request_id}";


    /**
     * Default search API TIME OUT time
     */
    int DEFAULT_SEARCH_TIMEOUT=60;

    /**
     * Default deep research API TIME OUT time
     */

    int DEFAULT_DEEP_RESEARCH_TIMEOUT=300;


}
