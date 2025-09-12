package com.alibaba.langengine.jieyue;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class JieYueConfiguration {

    /**
     * jieyue server url
     */
    public static String JIEYUE_SERVER_URL = WorkPropertiesUtils.get("jieyue_server_url");

    /**
     * jieyue api key
     * jieyue_api_key is configured in system configuration file
     * STEP_API_KEY is the common industry configuration in environment variables
     */
    public static String JIEYUE_API_KEY = WorkPropertiesUtils.getFirstAvailable("jieyue_api_key","STEP_API_KEY");

    /**
     * jieyue api timeout
     */
    public static String JIEYUE_API_TIMEOUT = WorkPropertiesUtils.get("jieyue_api_timeout", 300L);
}