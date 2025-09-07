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
package com.alibaba.langengine.trello;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class TrelloConfiguration {
    
    /**
     * Trello API Key
     */
    private String apiKey;
    
    /**
     * Trello API Token
     */
    private String token;
    
    /**
     * Trello API Base URL
     */
    private String baseUrl = "https://api.trello.com/1";
    
    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 30000;
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;
    
    /**
     * 是否启用调试模式
     */
    private boolean debug = false;
    
    /**
     * 默认每页结果数
     */
    private int defaultPageSize = 50;
    
    /**
     * 最大每页结果数
     */
    private int maxPageSize = 1000;
    
    /**
     * 默认构造函数
     */
    public TrelloConfiguration() {
        // 从环境变量获取配置
        this.apiKey = System.getenv("TRELLO_API_KEY");
        this.token = System.getenv("TRELLO_TOKEN");
    }
    
    /**
     * 构造函数
     * 
     * @param apiKey Trello API Key
     * @param token Trello Token
     */
    public TrelloConfiguration(String apiKey, String token) {
        this.apiKey = apiKey;
        this.token = token;
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 配置是否有效
     */
    public boolean isValid() {
        return apiKey != null && !apiKey.trim().isEmpty() 
            && token != null && !token.trim().isEmpty();
    }
    
    /**
     * 获取认证参数字符串
     * 
     * @return 认证参数字符串
     */
    public String getAuthParams() {
        return String.format("key=%s&token=%s", apiKey, token);
    }
    
    /**
     * 获取完整的API URL
     * 
     * @param endpoint API端点
     * @return 完整的API URL
     */
    public String getApiUrl(String endpoint) {
        String url = baseUrl + endpoint;
        if (endpoint.contains("?")) {
            url += "&" + getAuthParams();
        } else {
            url += "?" + getAuthParams();
        }
        return url;
    }
}
