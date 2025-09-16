package com.alibaba.langengine.volcenginetranslate;

/**
 * 火山翻译配置类
 *
 * @author Makoto
 */
public class VolcengineTranslateConfiguration {
    
    public static final String VOLCENGINE_TRANSLATE_ACCESS_KEY = System.getProperty("volcengine.translate.access.key", "");
    public static final String VOLCENGINE_TRANSLATE_SECRET_KEY = System.getProperty("volcengine.translate.secret.key", "");
    public static final String VOLCENGINE_TRANSLATE_REGION = System.getProperty("volcengine.translate.region", "cn-north-1");
    public static final String VOLCENGINE_TRANSLATE_SERVER_URL = System.getProperty("volcengine.translate.server.url", "https://translate.volcengineapi.com");
    public static final String VOLCENGINE_TRANSLATE_SERVICE = System.getProperty("volcengine.translate.service", "translate");
    public static final String VOLCENGINE_TRANSLATE_VERSION = System.getProperty("volcengine.translate.version", "2020-06-01");
    public static final String VOLCENGINE_TRANSLATE_ACTION = System.getProperty("volcengine.translate.action", "TranslateText");
    public static final String VOLCENGINE_TRANSLATE_TIMEOUT = System.getProperty("volcengine.translate.timeout", "30");
    
    public static String getAccessKey() {
        return VOLCENGINE_TRANSLATE_ACCESS_KEY;
    }
    
    public static String getSecretKey() {
        return VOLCENGINE_TRANSLATE_SECRET_KEY;
    }
    
    public static String getRegion() {
        return VOLCENGINE_TRANSLATE_REGION;
    }
    
    public static String getServerUrl() {
        return VOLCENGINE_TRANSLATE_SERVER_URL;
    }
    
    public static String getService() {
        return VOLCENGINE_TRANSLATE_SERVICE;
    }
    
    public static String getVersion() {
        return VOLCENGINE_TRANSLATE_VERSION;
    }
    
    public static String getAction() {
        return VOLCENGINE_TRANSLATE_ACTION;
    }
    
    public static int getTimeout() {
        return Integer.parseInt(VOLCENGINE_TRANSLATE_TIMEOUT);
    }
}
