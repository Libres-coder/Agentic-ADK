package com.alibaba.langgengine.kagi.sdk;

public interface KagiConstant {

    /**
     * The base_url for Kagi API.
     */
    String BASE_URL = "https://kagi.com/api/v0";

    /**
     * The  endpoint for Kagi Search API
     */
    String SEARCH_ENDPOINT = "/search";

    /**
     * The default timeout in seconds for API requests.
     */
    int DEFAULT_KAGI_TIMEOUT = 30;

}
