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
package com.alibaba.langengine.notion;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
public class NotionConfiguration {
    
    /**
     * Notion Integration Token (Internal Integration)
     */
    private String token;
    
    /**
     * Notion API版本
     */
    private String version = "2022-06-28";
    
    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 30000;
    
    /**
     * 是否启用调试模式
     */
    private boolean debug = false;
    
    /**
     * 默认构造函数
     */
    public NotionConfiguration() {
    }
    
    /**
     * 构造函数
     * 
     * @param token Notion集成令牌
     */
    public NotionConfiguration(String token) {
        this.token = token;
    }
    
    /**
     * 构造函数
     * 
     * @param token Notion集成令牌
     * @param version API版本
     */
    public NotionConfiguration(String token, String version) {
        this.token = token;
        this.version = version;
    }
    
    /**
     * 验证配置
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        if (token == null || token.trim().isEmpty()) {
            log.error("Notion token is required");
            return false;
        }
        
        if (!token.startsWith("secret_")) {
            log.warn("Notion token should start with 'secret_'");
        }
        
        return true;
    }
}
