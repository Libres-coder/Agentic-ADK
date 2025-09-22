package com.alibaba.langengine.wework;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.wework.tools.WeWorkMessageTool;
import com.alibaba.langengine.wework.tools.WeWorkUserTool;

import java.util.ArrayList;
import java.util.List;

public class WeWorkToolFactory {
    
    private final WeWorkConfiguration config;
    
    public WeWorkToolFactory() {
        this.config = new WeWorkConfiguration();
    }
    
    public WeWorkToolFactory(WeWorkConfiguration config) {
        this.config = config;
    }
    
    public WeWorkMessageTool createMessageTool() {
        return new WeWorkMessageTool(config);
    }
    
    public WeWorkUserTool createUserTool() {
        return new WeWorkUserTool(config);
    }
    
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createMessageTool());
        tools.add(createUserTool());
        return tools;
    }
}
