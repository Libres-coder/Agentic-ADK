package com.alibaba.langengine.twitch;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.twitch.tools.TwitchStreamSearchTool;
import com.alibaba.langengine.twitch.tools.TwitchGameSearchTool;
import com.alibaba.langengine.twitch.tools.TwitchUserSearchTool;
import com.alibaba.langengine.twitch.tools.TwitchTopGamesTool;

import java.util.ArrayList;
import java.util.List;

public class TwitchToolFactory {
    
    private final TwitchConfiguration config;
    
    public TwitchToolFactory() {
        this.config = new TwitchConfiguration();
    }
    
    public TwitchToolFactory(TwitchConfiguration config) {
        this.config = config;
    }
    
    public TwitchStreamSearchTool createTwitchStreamSearchTool() {
        return new TwitchStreamSearchTool(config);
    }
    
    public TwitchGameSearchTool createTwitchGameSearchTool() {
        return new TwitchGameSearchTool(config);
    }
    
    public TwitchUserSearchTool createTwitchUserSearchTool() {
        return new TwitchUserSearchTool(config);
    }
    
    public TwitchTopGamesTool createTwitchTopGamesTool() {
        return new TwitchTopGamesTool(config);
    }
    
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createTwitchStreamSearchTool());
        tools.add(createTwitchGameSearchTool());
        tools.add(createTwitchUserSearchTool());
        tools.add(createTwitchTopGamesTool());
        return tools;
    }
}
