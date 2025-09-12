package com.alibaba.langengine.perplexity;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

import static com.alibaba.langengine.perplexity.sdk.PerplexityConstant.*;

/**
 * Perplexity Search API configuration
 * Manages configuration parameters for Perplexity AI API integration
 */
public class PerplexityConfiguration {

    /**
     * Perplexity API key for authentication
     * Retrieved from properties file using key "perplexity_api_key"
     */
    public static String PERPLEXITY_API_KEY = WorkPropertiesUtils.get("perplexity_api_key");

    /**
     * Perplexity API base URL
     * Defaults to the constant BASE_URL if not configured
     * Can be overridden using property key "perplexity_api_url"
     */
    public static String PERPLEXITY_API_URL = WorkPropertiesUtils.get("perplexity_api_url", BASE_URL);

    /**
     * Default timeout for synchronous API requests in seconds
     * Can be overridden using property key "perplexity_sync_timeout"
     */
    public static final int SYNC_TIMEOUT = Integer.parseInt(
            WorkPropertiesUtils.get("perplexity_sync_timeout", String.valueOf(DEFAULT_SEARCH_TIMEOUT)));

    /**
     * Default timeout for deep research API requests in seconds
     * Can be overridden using property key "perplexity_deep_research_timeout"
     */
    public static final int DEEP_RESEARCH_TIMEOUT = Integer.parseInt(
            WorkPropertiesUtils.get("perplexity_deep_research_timeout", String.valueOf(DEFAULT_DEEP_RESEARCH_TIMEOUT)));

    /**
     * Synchronous search endpoint
     * Can be overridden using property key "perplexity_sync_endpoint"
     */
    public static final String SYNC_ENDPOINT = WorkPropertiesUtils.get("perplexity_sync_endpoint", SYNC_SEARCH_ENDPOINT);

    /**
     * Asynchronous search create endpoint
     * Can be overridden using property key "perplexity_async_endpoint"
     */
    public static final String ASYNC_ENDPOINT = WorkPropertiesUtils.get("perplexity_async_endpoint", ASYNC_SEARCH_ENDPOINT);

    /**
     * Asynchronous search list endpoint
     * Can be overridden using property key "perplexity_async_list_endpoint"
     */
    public static final String ASYNC_LIST_ENDPOINT = WorkPropertiesUtils.get("perplexity_async_list_endpoint", ASYNC_SEARCH_List_ENDPOINT);
}