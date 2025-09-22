package com.alibaba.langengine.netflix;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.netflix.tools.NetflixSearchTool;
import com.alibaba.langengine.netflix.tools.NetflixTrendingTool;
import com.alibaba.langengine.netflix.tools.NetflixDiscoverTool;

import java.util.ArrayList;
import java.util.List;

public class NetflixToolFactory {
    
    private final NetflixConfiguration config;
    
    public NetflixToolFactory() {
        this.config = new NetflixConfiguration();
    }
    
    public NetflixToolFactory(NetflixConfiguration config) {
        this.config = config;
    }
    
    public NetflixSearchTool createNetflixSearchTool() {
        return new NetflixSearchTool(config);
    }
    
    public NetflixTrendingTool createNetflixTrendingTool() {
        return new NetflixTrendingTool(config);
    }
    
    public NetflixDiscoverTool createNetflixDiscoverTool() {
        return new NetflixDiscoverTool(config);
    }
    
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createNetflixSearchTool());
        tools.add(createNetflixTrendingTool());
        tools.add(createNetflixDiscoverTool());
        return tools;
    }
}
