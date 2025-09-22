package com.alibaba.langengine.twitch;

import lombok.Data;

@Data
public class TwitchConfiguration {
    
    // Twitch API配置
    public static final String TWITCH_CLIENT_ID = System.getProperty("twitch.client.id");
    public static final String TWITCH_CLIENT_SECRET = System.getProperty("twitch.client.secret");
    public static final String TWITCH_API_BASE_URL = System.getProperty("twitch.api.base.url", "https://api.twitch.tv/helix");
    public static final String TWITCH_AUTH_URL = System.getProperty("twitch.auth.url", "https://id.twitch.tv/oauth2/token");
    
    // 通用配置
    public static final int TWITCH_TIMEOUT = Integer.parseInt(System.getProperty("twitch.timeout", "30"));
    public static final int DEFAULT_LIMIT = Integer.parseInt(System.getProperty("twitch.default.limit", "20"));
    public static final int MAX_LIMIT = Integer.parseInt(System.getProperty("twitch.max.limit", "100"));
    
    // 默认语言和地区
    public static final String DEFAULT_LANGUAGE = System.getProperty("twitch.default.language", "zh");
    public static final String DEFAULT_TIMEZONE = System.getProperty("twitch.default.timezone", "Asia/Shanghai");
    
    private String clientId;
    private String clientSecret;
    private String apiBaseUrl;
    private String authUrl;
    private int timeout;
    private int defaultLimit;
    private int maxLimit;
    private String defaultLanguage;
    private String defaultTimezone;
    
    public TwitchConfiguration() {
        this.clientId = TWITCH_CLIENT_ID;
        this.clientSecret = TWITCH_CLIENT_SECRET;
        this.apiBaseUrl = TWITCH_API_BASE_URL;
        this.authUrl = TWITCH_AUTH_URL;
        this.timeout = TWITCH_TIMEOUT;
        this.defaultLimit = DEFAULT_LIMIT;
        this.maxLimit = MAX_LIMIT;
        this.defaultLanguage = DEFAULT_LANGUAGE;
        this.defaultTimezone = DEFAULT_TIMEZONE;
    }
}
