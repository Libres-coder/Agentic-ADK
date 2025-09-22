package com.alibaba.langengine.deepl;

/**
 * DeepL 配置类
 *
 * @author Makoto
 */
public class DeepLConfiguration {
    
    public static final String DEEPL_API_KEY = System.getProperty("deepl.api.key", "");
    public static final String DEEPL_SERVER_URL = System.getProperty("deepl.server.url", "https://api-free.deepl.com");
    public static final String DEEPL_PRO_SERVER_URL = System.getProperty("deepl.pro.server.url", "https://api.deepl.com");
    public static final String DEEPL_TIMEOUT = System.getProperty("deepl.timeout", "30");
    public static final String DEEPL_IS_PRO = System.getProperty("deepl.is.pro", "false");
    
    public static String getApiKey() {
        return DEEPL_API_KEY;
    }
    
    public static String getServerUrl() {
        return Boolean.parseBoolean(DEEPL_IS_PRO) ? DEEPL_PRO_SERVER_URL : DEEPL_SERVER_URL;
    }
    
    public static int getTimeout() {
        return Integer.parseInt(DEEPL_TIMEOUT);
    }
    
    public static boolean isPro() {
        return Boolean.parseBoolean(DEEPL_IS_PRO);
    }
}
