package com.alibaba.langengine.netflix.examples;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.netflix.NetflixConfiguration;
import com.alibaba.langengine.netflix.NetflixToolFactory;
import com.alibaba.langengine.netflix.tools.NetflixSearchTool;
import com.alibaba.langengine.netflix.tools.NetflixTrendingTool;
import com.alibaba.langengine.netflix.tools.NetflixDiscoverTool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetflixToolsExample {
    
    public static void main(String[] args) {
        // 创建配置
        NetflixConfiguration config = new NetflixConfiguration();
        
        // 创建工具工厂
        NetflixToolFactory factory = new NetflixToolFactory(config);
        
        // 示例1: 搜索电影
        searchMoviesExample(factory);
        
        // 示例2: 搜索电视剧
        searchTvShowsExample(factory);
        
        // 示例3: 获取热门内容
        getTrendingExample(factory);
        
        // 示例4: 发现内容
        discoverContentExample(factory);
        
        // 示例5: 综合搜索
        searchAllExample(factory);
    }
    
    /**
     * 搜索电影示例
     */
    private static void searchMoviesExample(NetflixToolFactory factory) {
        log.info("=== Netflix电影搜索示例 ===");
        
        NetflixSearchTool searchTool = factory.createNetflixSearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"复仇者联盟\",\n" +
                "  \"type\": \"movie\",\n" +
                "  \"page\": 1,\n" +
                "  \"language\": \"zh-CN\",\n" +
                "  \"region\": \"CN\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 搜索电视剧示例
     */
    private static void searchTvShowsExample(NetflixToolFactory factory) {
        log.info("=== Netflix电视剧搜索示例 ===");
        
        NetflixSearchTool searchTool = factory.createNetflixSearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"权力的游戏\",\n" +
                "  \"type\": \"tv\",\n" +
                "  \"page\": 1,\n" +
                "  \"language\": \"zh-CN\",\n" +
                "  \"region\": \"CN\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
    
    /**
     * 获取热门内容示例
     */
    private static void getTrendingExample(NetflixToolFactory factory) {
        log.info("=== Netflix热门内容示例 ===");
        
        NetflixTrendingTool trendingTool = factory.createNetflixTrendingTool();
        
        String toolInput = "{\n" +
                "  \"timeWindow\": \"day\",\n" +
                "  \"type\": \"all\",\n" +
                "  \"page\": 1,\n" +
                "  \"language\": \"zh-CN\",\n" +
                "  \"region\": \"CN\"\n" +
                "}";
        
        ToolExecuteResult result = trendingTool.run(toolInput, null);
        log.info("热门内容: {}", result.getResult());
    }
    
    /**
     * 发现内容示例
     */
    private static void discoverContentExample(NetflixToolFactory factory) {
        log.info("=== Netflix内容发现示例 ===");
        
        NetflixDiscoverTool discoverTool = factory.createNetflixDiscoverTool();
        
        String toolInput = "{\n" +
                "  \"type\": \"movie\",\n" +
                "  \"genre\": \"28\",\n" +
                "  \"minYear\": 2020,\n" +
                "  \"maxYear\": 2024,\n" +
                "  \"minRating\": 7.0,\n" +
                "  \"sortBy\": \"vote_average.desc\",\n" +
                "  \"page\": 1,\n" +
                "  \"language\": \"zh-CN\",\n" +
                "  \"region\": \"CN\"\n" +
                "}";
        
        ToolExecuteResult result = discoverTool.run(toolInput, null);
        log.info("发现结果: {}", result.getResult());
    }
    
    /**
     * 综合搜索示例
     */
    private static void searchAllExample(NetflixToolFactory factory) {
        log.info("=== Netflix综合搜索示例 ===");
        
        NetflixSearchTool searchTool = factory.createNetflixSearchTool();
        
        String toolInput = "{\n" +
                "  \"query\": \"漫威\",\n" +
                "  \"type\": \"movie,tv\",\n" +
                "  \"page\": 1,\n" +
                "  \"language\": \"zh-CN\",\n" +
                "  \"region\": \"CN\"\n" +
                "}";
        
        ToolExecuteResult result = searchTool.run(toolInput, null);
        log.info("搜索结果: {}", result.getResult());
    }
}
