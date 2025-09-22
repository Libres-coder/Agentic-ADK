package com.alibaba.langengine.tencenttranslate;

/**
 * 腾讯翻译配置类
 *
 * @author Makoto
 */
public class TencentTranslateConfiguration {
    
    public static final String TENCENT_TRANSLATE_SECRET_ID = System.getProperty("tencent.translate.secret.id", "");
    public static final String TENCENT_TRANSLATE_SECRET_KEY = System.getProperty("tencent.translate.secret.key", "");
    public static final String TENCENT_TRANSLATE_REGION = System.getProperty("tencent.translate.region", "ap-beijing");
    public static final String TENCENT_TRANSLATE_SERVER_URL = System.getProperty("tencent.translate.server.url", "https://tmt.tencentcloudapi.com");
    public static final String TENCENT_TRANSLATE_SERVICE = System.getProperty("tencent.translate.service", "tmt");
    public static final String TENCENT_TRANSLATE_VERSION = System.getProperty("tencent.translate.version", "2018-03-21");
    public static final String TENCENT_TRANSLATE_ACTION = System.getProperty("tencent.translate.action", "TextTranslate");
    public static final String TENCENT_TRANSLATE_TIMEOUT = System.getProperty("tencent.translate.timeout", "30");
    
    public static String getSecretId() {
        return TENCENT_TRANSLATE_SECRET_ID;
    }
    
    public static String getSecretKey() {
        return TENCENT_TRANSLATE_SECRET_KEY;
    }
    
    public static String getRegion() {
        return TENCENT_TRANSLATE_REGION;
    }
    
    public static String getServerUrl() {
        return TENCENT_TRANSLATE_SERVER_URL;
    }
    
    public static String getService() {
        return TENCENT_TRANSLATE_SERVICE;
    }
    
    public static String getVersion() {
        return TENCENT_TRANSLATE_VERSION;
    }
    
    public static String getAction() {
        return TENCENT_TRANSLATE_ACTION;
    }
    
    public static int getTimeout() {
        return Integer.parseInt(TENCENT_TRANSLATE_TIMEOUT);
    }
}
