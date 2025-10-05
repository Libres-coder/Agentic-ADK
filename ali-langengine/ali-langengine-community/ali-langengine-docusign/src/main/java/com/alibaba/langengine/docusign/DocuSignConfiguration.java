package com.alibaba.langengine.docusign;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

public class DocuSignConfiguration {

    public static String DOCUSIGN_BASE_URL = WorkPropertiesUtils.get("docusign_base_url");
    public static String DOCUSIGN_ACCOUNT_ID = WorkPropertiesUtils.get("docusign_account_id");
    public static String DOCUSIGN_ACCESS_TOKEN = WorkPropertiesUtils.get("docusign_access_token");
}


