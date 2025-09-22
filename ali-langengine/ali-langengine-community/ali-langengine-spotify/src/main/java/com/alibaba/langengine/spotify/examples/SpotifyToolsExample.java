package com.alibaba.langengine.spotify.examples;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.spotify.SpotifyConfiguration;
import com.alibaba.langengine.spotify.SpotifyToolFactory;
import com.alibaba.langengine.spotify.tools.SpotifySearchTool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpotifyToolsExample {
    
    public static void main(String[] args) {
        // 创建配置
        SpotifyConfiguration config = new SpotifyConfiguration();
        
        // 创建工具工厂
        SpotifyToolFactory factory = new SpotifyToolFactory(config);
        
        // 示例1: 搜索歌曲
        searchTracksExample(factory);
        
        // 示例2: 搜索艺术家
        searchArtistsExample(factory);
        
        // 示例3: 搜索专辑
        searchAlbumsExample(factory);
        
        // 示例4: 搜索播放列表
        searchPlaylistsExample(factory);
        
        // 示例5: 综合搜索
        searchAllExample(factory);
    }
    
    /**
     * 搜索歌曲示例
     */
    private static void searchTracksExample(SpotifyToolFactory factory) {
        log.info("=== Spotify歌曲搜索示例 ===");
        
        SpotifySearchTool searchTool = factory.createSpotifySearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"周杰伦\",\n" +
                "  \"type\": \"track\",\n" +
                "  \"limit\": 5,\n" +
                "  \"market\": \"CN\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 搜索艺术家示例
     */
    private static void searchArtistsExample(SpotifyToolFactory factory) {
        log.info("=== Spotify艺术家搜索示例 ===");
        
        SpotifySearchTool searchTool = factory.createSpotifySearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"Taylor Swift\",\n" +
                "  \"type\": \"artist\",\n" +
                "  \"limit\": 3,\n" +
                "  \"market\": \"US\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 搜索专辑示例
     */
    private static void searchAlbumsExample(SpotifyToolFactory factory) {
        log.info("=== Spotify专辑搜索示例 ===");
        
        SpotifySearchTool searchTool = factory.createSpotifySearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"1989\",\n" +
                "  \"type\": \"album\",\n" +
                "  \"limit\": 3,\n" +
                "  \"market\": \"US\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 搜索播放列表示例
     */
    private static void searchPlaylistsExample(SpotifyToolFactory factory) {
        log.info("=== Spotify播放列表搜索示例 ===");
        
        SpotifySearchTool searchTool = factory.createSpotifySearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"pop music\",\n" +
                "  \"type\": \"playlist\",\n" +
                "  \"limit\": 3,\n" +
                "  \"market\": \"US\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 综合搜索示例
     */
    private static void searchAllExample(SpotifyToolFactory factory) {
        log.info("=== Spotify综合搜索示例 ===");
        
        SpotifySearchTool searchTool = factory.createSpotifySearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"Ed Sheeran\",\n" +
                "  \"type\": \"track,artist,album\",\n" +
                "  \"limit\": 3,\n" +
                "  \"market\": \"US\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
}
