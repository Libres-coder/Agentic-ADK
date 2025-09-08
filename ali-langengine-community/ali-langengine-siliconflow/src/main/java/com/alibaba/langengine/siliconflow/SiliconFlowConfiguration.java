package com.alibaba.langengine.siliconflow;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * SiliconFlow model configuration
 */
public class SiliconFlowConfiguration {

    /**
     * SiliconFlow server url
     */
    public static String SILICONFLOW_SERVER_URL = WorkPropertiesUtils.get("siliconflow_server_url");

    /**
     * SiliconFlow api key
     * siliconflow_api_key is configured in the system configuration file
     * SILICONFLOW_API_KEY is a common industry configuration in environment variables
     */
    public static String SILICONFLOW_API_KEY = WorkPropertiesUtils.getFirstAvailable("siliconflow_api_key","SILICONFLOW_API_KEY");

    /**
     * SiliconFlow api timeout (in seconds)
     */
    public static String SILICONFLOW_AI_TIMEOUT = WorkPropertiesUtils.get("siliconflow_api_timeout", 300L);
}