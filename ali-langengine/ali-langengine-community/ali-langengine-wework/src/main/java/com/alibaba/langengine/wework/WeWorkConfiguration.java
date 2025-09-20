package com.alibaba.langengine.wework;

import lombok.Data;

@Data
public class WeWorkConfiguration {
    
    public static final String WEWORK_CORP_ID = System.getProperty("wework.corp.id");
    public static final String WEWORK_CORP_SECRET = System.getProperty("wework.corp.secret");
    public static final String WEWORK_AGENT_ID = System.getProperty("wework.agent.id");
    public static final String WEWORK_SERVER_URL = System.getProperty("wework.server.url", "https://qyapi.weixin.qq.com");
    public static final int WEWORK_TIMEOUT = Integer.parseInt(System.getProperty("wework.timeout", "30"));
    
    private String corpId;
    private String corpSecret;
    private String agentId;
    private String serverUrl;
    private int timeout;
    
    public WeWorkConfiguration() {
        this.corpId = WEWORK_CORP_ID;
        this.corpSecret = WEWORK_CORP_SECRET;
        this.agentId = WEWORK_AGENT_ID;
        this.serverUrl = WEWORK_SERVER_URL;
        this.timeout = WEWORK_TIMEOUT;
    }
    
    public WeWorkConfiguration(String corpId, String corpSecret, String agentId) {
        this.corpId = corpId;
        this.corpSecret = corpSecret;
        this.agentId = agentId;
        this.serverUrl = WEWORK_SERVER_URL;
        this.timeout = WEWORK_TIMEOUT;
    }
}
