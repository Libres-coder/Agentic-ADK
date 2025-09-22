package com.alibaba.langengine.spotify;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.spotify.tools.SpotifySearchTool;

import java.util.ArrayList;
import java.util.List;

public class SpotifyToolFactory {
    
    private final SpotifyConfiguration config;
    
    public SpotifyToolFactory() {
        this.config = new SpotifyConfiguration();
    }
    
    public SpotifyToolFactory(SpotifyConfiguration config) {
        this.config = config;
    }
    
    public SpotifySearchTool createSpotifySearchTool() {
        return new SpotifySearchTool(config);
    }
    
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createSpotifySearchTool());
        return tools;
    }
}
