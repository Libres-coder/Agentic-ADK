package com.alibaba.langengine.microsofttranslate;

/**
 * Microsoft 翻译配置类
 *
 * @author Makoto
 */
public class MicrosoftTranslateConfiguration {
    
    public static final String MICROSOFT_TRANSLATE_SUBSCRIPTION_KEY = System.getProperty("microsoft.translate.subscription.key", "");
    public static final String MICROSOFT_TRANSLATE_REGION = System.getProperty("microsoft.translate.region", "");
    public static final String MICROSOFT_TRANSLATE_SERVER_URL = System.getProperty("microsoft.translate.server.url", "https://api.cognitive.microsofttranslator.com");
    public static final String MICROSOFT_TRANSLATE_API_VERSION = System.getProperty("microsoft.translate.api.version", "3.0");
    public static final String MICROSOFT_TRANSLATE_TIMEOUT = System.getProperty("microsoft.translate.timeout", "30");
    
    public static String getSubscriptionKey() {
        return MICROSOFT_TRANSLATE_SUBSCRIPTION_KEY;
    }
    
    public static String getRegion() {
        return MICROSOFT_TRANSLATE_REGION;
    }
    
    public static String getServerUrl() {
        return MICROSOFT_TRANSLATE_SERVER_URL;
    }
    
    public static String getApiVersion() {
        return MICROSOFT_TRANSLATE_API_VERSION;
    }
    
    public static int getTimeout() {
        return Integer.parseInt(MICROSOFT_TRANSLATE_TIMEOUT);
    }
}
