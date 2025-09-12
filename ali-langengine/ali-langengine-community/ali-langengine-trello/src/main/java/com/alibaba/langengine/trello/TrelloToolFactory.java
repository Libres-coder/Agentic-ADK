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

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.trello.client.TrelloClient;
import com.alibaba.langengine.trello.tools.TrelloCardManageTool;
import com.alibaba.langengine.trello.tools.TrelloBoardOperationTool;
import com.alibaba.langengine.trello.tools.TrelloTeamCollaborationTool;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class TrelloToolFactory {
    
    private TrelloConfiguration configuration;
    private TrelloClient trelloClient;
    
    /**
     * 构造函数
     * 
     * @param configuration Trello配置
     */
    public TrelloToolFactory(TrelloConfiguration configuration) {
        this.configuration = configuration;
        this.trelloClient = new TrelloClient(configuration);
    }
    
    /**
     * 构造函数
     * 
     * @param apiKey API Key
     * @param token Token
     */
    public TrelloToolFactory(String apiKey, String token) {
        this.configuration = new TrelloConfiguration(apiKey, token);
        this.trelloClient = new TrelloClient(this.configuration);
    }
    
    /**
     * 创建卡片管理工具
     * 
     * @return 卡片管理工具实例
     */
    public TrelloCardManageTool createCardManageTool() {
        return new TrelloCardManageTool(trelloClient);
    }
    
    /**
     * 创建看板操作工具
     * 
     * @return 看板操作工具实例
     */
    public TrelloBoardOperationTool createBoardOperationTool() {
        return new TrelloBoardOperationTool(trelloClient);
    }
    
    /**
     * 创建团队协作工具
     * 
     * @return 团队协作工具实例
     */
    public TrelloTeamCollaborationTool createTeamCollaborationTool() {
        return new TrelloTeamCollaborationTool(trelloClient);
    }
    
    /**
     * 创建所有可用的工具
     * 
     * @return 工具列表
     */
    public List<BaseTool> createAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        
        try {
            tools.add(createCardManageTool());
            tools.add(createBoardOperationTool());
            tools.add(createTeamCollaborationTool());
            
            log.info("成功创建 {} 个Trello工具", tools.size());
        } catch (Exception e) {
            log.error("创建Trello工具失败", e);
        }
        
        return tools;
    }
    
    /**
     * 验证配置是否有效
     * 
     * @return 配置是否有效
     */
    public boolean validateConfiguration() {
        if (configuration == null) {
            log.error("Trello配置为空");
            return false;
        }
        
        if (!configuration.isValid()) {
            log.error("Trello配置无效，请检查API Key和Token");
            return false;
        }
        
        try {
            // 尝试获取当前用户信息来验证配置
            trelloClient.getCurrentMember();
            log.info("Trello配置验证成功");
            return true;
        } catch (Exception e) {
            log.error("Trello配置验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取配置信息
     * 
     * @return 配置信息
     */
    public TrelloConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * 获取客户端实例
     * 
     * @return 客户端实例
     */
    public TrelloClient getClient() {
        return trelloClient;
    }
    
    /**
     * 静态工厂方法：从环境变量创建
     * 
     * @return 工具工厂实例
     */
    public static TrelloToolFactory fromEnvironment() {
        TrelloConfiguration config = new TrelloConfiguration();
        if (!config.isValid()) {
            throw new IllegalStateException("无法从环境变量获取有效的Trello配置，请设置TRELLO_API_KEY和TRELLO_TOKEN环境变量");
        }
        return new TrelloToolFactory(config);
    }
    
    /**
     * 静态工厂方法：使用指定的认证信息创建
     * 
     * @param apiKey API Key
     * @param token Token
     * @return 工具工厂实例
     */
    public static TrelloToolFactory create(String apiKey, String token) {
        return new TrelloToolFactory(apiKey, token);
    }
}
