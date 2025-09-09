package com.alibaba.langengine.tianyancha;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import static com.alibaba.langengine.tianyancha.TianyanchaConstant.BASE_URL;

public class TianyanchaConfiguration {
    /**
     * Tianyancha API token, retrieved from work properties
     */
    public static String TIANYANCHA_API_TOKEN = WorkPropertiesUtils.get("tianyancha_api_token");
    /**
     * Tianyancha API base URL, defaults to the constant BASE_URL if not configured
     */
    public static String TIANYANCHA_BASE_URL = WorkPropertiesUtils.get("tianyancha_base_url", BASE_URL);
}