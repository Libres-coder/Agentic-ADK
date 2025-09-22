/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.dingtalk;

import lombok.Data;

/**
 * 钉钉配置类
 * 
 * @author langengine
 */
@Data
public class DingTalkConfiguration {
    
    public static final String DINGTALK_APP_KEY = System.getProperty("dingtalk.app.key");
    public static final String DINGTALK_APP_SECRET = System.getProperty("dingtalk.app.secret");
    public static final String DINGTALK_AGENT_ID = System.getProperty("dingtalk.agent.id");
    public static final String DINGTALK_CORP_ID = System.getProperty("dingtalk.corp.id");
    public static final String DINGTALK_SERVER_URL = System.getProperty("dingtalk.server.url", "https://oapi.dingtalk.com");
    public static final int DINGTALK_TIMEOUT = Integer.parseInt(System.getProperty("dingtalk.timeout", "30"));
    
    private String appKey;
    private String appSecret;
    private String agentId;
    private String corpId;
    private String serverUrl;
    private int timeout;
    
    public DingTalkConfiguration() {
        this.appKey = DINGTALK_APP_KEY;
        this.appSecret = DINGTALK_APP_SECRET;
        this.agentId = DINGTALK_AGENT_ID;
        this.corpId = DINGTALK_CORP_ID;
        this.serverUrl = DINGTALK_SERVER_URL;
        this.timeout = DINGTALK_TIMEOUT;
    }
    
    public DingTalkConfiguration(String appKey, String appSecret, String agentId, String corpId) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.agentId = agentId;
        this.corpId = corpId;
        this.serverUrl = DINGTALK_SERVER_URL;
        this.timeout = DINGTALK_TIMEOUT;
    }
}
