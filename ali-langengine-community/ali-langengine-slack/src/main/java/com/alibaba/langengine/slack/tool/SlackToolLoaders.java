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
package com.alibaba.langengine.slack.tool;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.slack.client.SlackClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class SlackToolLoaders {

    /**
     * 加载所有Slack工具
     *
     * @return Slack工具列表
     */
    public static List<BaseTool> loadAllTools() {
        return loadAllTools(new SlackClient());
    }

    /**
     * 使用指定客户端加载所有Slack工具
     *
     * @param slackClient Slack客户端
     * @return Slack工具列表
     */
    public static List<BaseTool> loadAllTools(SlackClient slackClient) {
        log.info("Loading all Slack tools...");
        
        List<BaseTool> tools = new ArrayList<>();
        
        try {
            // 添加发送消息工具
            tools.add(new SlackSendMessageTool(slackClient));
            
            // 添加获取频道列表工具
            tools.add(new SlackGetChannelsTool(slackClient));
            
            // 添加获取用户列表工具
            tools.add(new SlackGetUsersTool(slackClient));
            
            // 添加获取频道历史工具
            tools.add(new SlackGetChannelHistoryTool(slackClient));
            
            // 添加查找频道工具
            tools.add(new SlackFindChannelTool(slackClient));
            
            log.info("Successfully loaded {} Slack tools", tools.size());
            
        } catch (Exception e) {
            log.error("Error loading Slack tools: {}", e.getMessage(), e);
        }
        
        return tools;
    }

    /**
     * 加载基本Slack工具（发送消息、获取频道、获取用户）
     *
     * @return 基本Slack工具列表
     */
    public static List<BaseTool> loadBasicTools() {
        return loadBasicTools(new SlackClient());
    }

    /**
     * 使用指定客户端加载基本Slack工具
     *
     * @param slackClient Slack客户端
     * @return 基本Slack工具列表
     */
    public static List<BaseTool> loadBasicTools(SlackClient slackClient) {
        log.info("Loading basic Slack tools...");
        
        return Arrays.asList(
            new SlackSendMessageTool(slackClient),
            new SlackGetChannelsTool(slackClient),
            new SlackGetUsersTool(slackClient)
        );
    }

    /**
     * 加载消息相关工具
     *
     * @return 消息相关工具列表
     */
    public static List<BaseTool> loadMessageTools() {
        return loadMessageTools(new SlackClient());
    }

    /**
     * 使用指定客户端加载消息相关工具
     *
     * @param slackClient Slack客户端
     * @return 消息相关工具列表
     */
    public static List<BaseTool> loadMessageTools(SlackClient slackClient) {
        log.info("Loading Slack message tools...");
        
        return Arrays.asList(
            new SlackSendMessageTool(slackClient),
            new SlackGetChannelHistoryTool(slackClient)
        );
    }

    /**
     * 加载信息查询工具
     *
     * @return 信息查询工具列表
     */
    public static List<BaseTool> loadQueryTools() {
        return loadQueryTools(new SlackClient());
    }

    /**
     * 使用指定客户端加载信息查询工具
     *
     * @param slackClient Slack客户端
     * @return 信息查询工具列表
     */
    public static List<BaseTool> loadQueryTools(SlackClient slackClient) {
        log.info("Loading Slack query tools...");
        
        return Arrays.asList(
            new SlackGetChannelsTool(slackClient),
            new SlackGetUsersTool(slackClient),
            new SlackFindChannelTool(slackClient)
        );
    }

    /**
     * 根据工具名称加载特定工具
     *
     * @param toolName 工具名称
     * @return 工具实例，如果未找到返回null
     */
    public static BaseTool loadToolByName(String toolName) {
        return loadToolByName(toolName, new SlackClient());
    }

    /**
     * 使用指定客户端根据工具名称加载特定工具
     *
     * @param toolName 工具名称
     * @param slackClient Slack客户端
     * @return 工具实例，如果未找到返回null
     */
    public static BaseTool loadToolByName(String toolName, SlackClient slackClient) {
        log.info("Loading Slack tool by name: {}", toolName);
        
        switch (toolName) {
            case "SlackSendMessage":
                return new SlackSendMessageTool(slackClient);
            case "SlackGetChannels":
                return new SlackGetChannelsTool(slackClient);
            case "SlackGetUsers":
                return new SlackGetUsersTool(slackClient);
            case "SlackGetChannelHistory":
                return new SlackGetChannelHistoryTool(slackClient);
            case "SlackFindChannel":
                return new SlackFindChannelTool(slackClient);
            default:
                log.warn("Unknown Slack tool name: {}", toolName);
                return null;
        }
    }

    /**
     * 获取所有支持的工具名称
     *
     * @return 工具名称列表
     */
    public static List<String> getSupportedToolNames() {
        return Arrays.asList(
            "SlackSendMessage",
            "SlackGetChannels", 
            "SlackGetUsers",
            "SlackGetChannelHistory",
            "SlackFindChannel"
        );
    }
}
