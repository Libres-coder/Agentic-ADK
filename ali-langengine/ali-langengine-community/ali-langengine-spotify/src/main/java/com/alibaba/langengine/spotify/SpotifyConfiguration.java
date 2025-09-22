package com.alibaba.langengine.spotify;

import lombok.Data;

@Data
public class SpotifyConfiguration {
    
    // Spotify API配置
    public static final String SPOTIFY_CLIENT_ID = System.getProperty("spotify.client.id");
    public static final String SPOTIFY_CLIENT_SECRET = System.getProperty("spotify.client.secret");
    public static final String SPOTIFY_REDIRECT_URI = System.getProperty("spotify.redirect.uri", "http://localhost:8080/callback");
    public static final String SPOTIFY_API_BASE_URL = System.getProperty("spotify.api.base.url", "https://api.spotify.com/v1");
    public static final String SPOTIFY_AUTH_URL = System.getProperty("spotify.auth.url", "https://accounts.spotify.com/api/token");
    
    // 通用配置
    public static final int SPOTIFY_TIMEOUT = Integer.parseInt(System.getProperty("spotify.timeout", "30"));
    public static final int DEFAULT_LIMIT = Integer.parseInt(System.getProperty("spotify.default.limit", "20"));
    public static final int MAX_LIMIT = Integer.parseInt(System.getProperty("spotify.max.limit", "50"));
    
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String apiBaseUrl;
    private String authUrl;
    private int timeout;
    private int defaultLimit;
    private int maxLimit;
    
    public SpotifyConfiguration() {
        this.clientId = SPOTIFY_CLIENT_ID;
        this.clientSecret = SPOTIFY_CLIENT_SECRET;
        this.redirectUri = SPOTIFY_REDIRECT_URI;
        this.apiBaseUrl = SPOTIFY_API_BASE_URL;
        this.authUrl = SPOTIFY_AUTH_URL;
        this.timeout = SPOTIFY_TIMEOUT;
        this.defaultLimit = DEFAULT_LIMIT;
        this.maxLimit = MAX_LIMIT;
    }
}
