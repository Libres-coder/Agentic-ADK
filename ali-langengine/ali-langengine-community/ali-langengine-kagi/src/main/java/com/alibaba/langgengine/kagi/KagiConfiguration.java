package com.alibaba.langgengine.kagi;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import com.alibaba.langgengine.kagi.sdk.KagiConstant;

import static com.alibaba.langgengine.kagi.sdk.KagiConstant.BASE_URL;

public class KagiConfiguration {

    /**
     *Kagi Search base URL,defaults to the constant BASE_URL if not configured
     */
    public static final String KAGI_API_URL =WorkPropertiesUtils.get("kagi_base_url",BASE_URL);

    /**
     *Kagi Search API key
     */
    public static String KAGI_API_KEY = WorkPropertiesUtils.get("kagi_api_key");

    /**
     * Default timeout for API requests in seconds
     */
    public static final int DEFAULT_TIMEOUT = Integer.parseInt(WorkPropertiesUtils.get("kagi_timeout", String.valueOf(KagiConstant.DEFAULT_KAGI_TIMEOUT)));

    /**
     * Search endpoint for Kagi API
     */
    public static final String SEARCH_ENDPOINT = WorkPropertiesUtils.get("kagi_search_endpoint", KagiConstant.SEARCH_ENDPOINT);

}