package com.alibaba.langengine.twitch.examples;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.twitch.TwitchConfiguration;
import com.alibaba.langengine.twitch.TwitchToolFactory;
import com.alibaba.langengine.twitch.tools.TwitchStreamSearchTool;
import com.alibaba.langengine.twitch.tools.TwitchGameSearchTool;
import com.alibaba.langengine.twitch.tools.TwitchUserSearchTool;
import com.alibaba.langengine.twitch.tools.TwitchTopGamesTool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwitchToolsExample {
    
    public static void main(String[] args) {
        // 创建配置
        TwitchConfiguration config = new TwitchConfiguration();
        
        // 创建工具工厂
        TwitchToolFactory factory = new TwitchToolFactory(config);
        
        // 示例1: 搜索直播
        searchStreamsExample(factory);
        
        // 示例2: 搜索游戏
        searchGamesExample(factory);
        
        // 示例3: 搜索用户
        searchUsersExample(factory);
        
        // 示例4: 获取热门游戏
        getTopGamesExample(factory);
        
        // 示例5: 按游戏搜索直播
        searchStreamsByGameExample(factory);
    }
    
    /**
     * 搜索直播示例
     */
    private static void searchStreamsExample(TwitchToolFactory factory) {
        log.info("=== Twitch直播搜索示例 ===");
        
        TwitchStreamSearchTool streamTool = factory.createTwitchStreamSearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"英雄联盟\",\n" +
                "  \"language\": \"zh\",\n" +
                "  \"limit\": 10,\n" +
                "  \"sortBy\": \"viewer_count\"\n" +
                "}";
        
        ToolExecuteResult result = streamTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 搜索游戏示例
     */
    private static void searchGamesExample(TwitchToolFactory factory) {
        log.info("=== Twitch游戏搜索示例 ===");
        
        TwitchGameSearchTool gameTool = factory.createTwitchGameSearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"League of Legends\",\n" +
                "  \"limit\": 5\n" +
                "}";
        
        ToolExecuteResult result = gameTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 搜索用户示例
     */
    private static void searchUsersExample(TwitchToolFactory factory) {
        log.info("=== Twitch用户搜索示例 ===");
        
        TwitchUserSearchTool userTool = factory.createTwitchUserSearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"ninja\",\n" +
                "  \"limit\": 5\n" +
                "}";
        
        ToolExecuteResult result = userTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 获取热门游戏示例
     */
    private static void getTopGamesExample(TwitchToolFactory factory) {
        log.info("=== Twitch热门游戏示例 ===");
        
        TwitchTopGamesTool topGamesTool = factory.createTwitchTopGamesTool();
        
        String toolInput = "{\n" +
                "  \"limit\": 10\n" +
                "}";
        
        ToolExecuteResult result = topGamesTool.run(toolInput, null);
        log.info("热门游戏: {}", result.getResult());
    }
    
    /**
     * 按游戏搜索直播示例
     */
    private static void searchStreamsByGameExample(TwitchToolFactory factory) {
        log.info("=== Twitch按游戏搜索直播示例 ===");
        
        TwitchStreamSearchTool streamTool = factory.createTwitchStreamSearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"游戏直播\",\n" +
                "  \"gameId\": \"21779\",\n" +
                "  \"language\": \"zh\",\n" +
                "  \"limit\": 5,\n" +
                "  \"sortBy\": \"viewer_count\"\n" +
                "}";
        
        ToolExecuteResult result = streamTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
}
