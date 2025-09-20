package com.alibaba.langengine.dingtalk;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkDepartmentTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkMessageTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkUserTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 钉钉工具工厂
 * 
 * @author langengine
 */
public class DingTalkToolFactory {
    
    private final DingTalkConfiguration config;
    
    public DingTalkToolFactory() {
        this.config = new DingTalkConfiguration();
    }
    
    public DingTalkToolFactory(DingTalkConfiguration config) {
        this.config = config;
    }
    
    /**
     * 创建消息工具
     */
    public DingTalkMessageTool createMessageTool() {
        return new DingTalkMessageTool(config);
    }
    
    /**
     * 创建用户工具
     */
    public DingTalkUserTool createUserTool() {
        return new DingTalkUserTool(config);
    }
    
    /**
     * 创建部门工具
     */
    public DingTalkDepartmentTool createDepartmentTool() {
        return new DingTalkDepartmentTool(config);
    }
    
    /**
     * 获取所有工具
     */
    public List<BaseTool> getAllTools() {
        List<BaseTool> tools = new ArrayList<>();
        tools.add(createMessageTool());
        tools.add(createUserTool());
        tools.add(createDepartmentTool());
        return tools;
    }
}
