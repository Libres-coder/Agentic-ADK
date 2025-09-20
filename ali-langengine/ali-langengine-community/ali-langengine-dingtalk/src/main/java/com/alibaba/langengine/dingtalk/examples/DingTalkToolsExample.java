package com.alibaba.langengine.dingtalk.examples;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import com.alibaba.langengine.dingtalk.DingTalkToolFactory;
import com.alibaba.langengine.dingtalk.tools.DingTalkDepartmentTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkMessageTool;
import com.alibaba.langengine.dingtalk.tools.DingTalkUserTool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 钉钉工具使用示例
 * 
 * @author langengine
 */
@Slf4j
public class DingTalkToolsExample {
    
    public static void main(String[] args) {
        // 创建配置
        DingTalkConfiguration config = new DingTalkConfiguration(
            "your_app_key",
            "your_app_secret", 
            "your_agent_id",
            "your_corp_id"
        );
        
        // 创建工具工厂
        DingTalkToolFactory factory = new DingTalkToolFactory(config);
        
        // 示例1: 发送文本消息
        sendTextMessageExample(factory);
        
        // 示例2: 发送链接消息
        sendLinkMessageExample(factory);
        
        // 示例3: 查询用户信息
        getUserInfoExample(factory);
        
        // 示例4: 查询部门列表
        getDepartmentListExample(factory);
        
        // 示例5: 使用所有工具
        useAllToolsExample(factory);
    }
    
    /**
     * 发送文本消息示例
     */
    private static void sendTextMessageExample(DingTalkToolFactory factory) {
        log.info("=== 钉钉文本消息发送示例 ===");
        
        DingTalkMessageTool messageTool = factory.createMessageTool();
        
        String toolInput = "{\n" +
                "  \"userIds\": \"user1,user2\",\n" +
                "  \"messageType\": \"text\",\n" +
                "  \"content\": \"这是一条测试消息\"\n" +
                "}";
        
        ToolExecuteResult result = messageTool.run(toolInput, null);
        log.info("发送结果: {}", result.getResult());
    }
    
    /**
     * 发送链接消息示例
     */
    private static void sendLinkMessageExample(DingTalkToolFactory factory) {
        log.info("=== 钉钉链接消息发送示例 ===");
        
        DingTalkMessageTool messageTool = factory.createMessageTool();
        
        String toolInput = "{\n" +
                "  \"userIds\": \"user1\",\n" +
                "  \"messageType\": \"link\",\n" +
                "  \"content\": \"点击查看详情\",\n" +
                "  \"title\": \"重要通知\",\n" +
                "  \"messageUrl\": \"https://www.example.com\",\n" +
                "  \"picUrl\": \"https://www.example.com/image.jpg\"\n" +
                "}";
        
        ToolExecuteResult result = messageTool.run(toolInput, null);
        log.info("发送结果: {}", result.getResult());
    }
    
    /**
     * 查询用户信息示例
     */
    private static void getUserInfoExample(DingTalkToolFactory factory) {
        log.info("=== 钉钉用户信息查询示例 ===");
        
        DingTalkUserTool userTool = factory.createUserTool();
        
        String toolInput = "{\n" +
                "  \"userId\": \"user123\"\n" +
                "}";
        
        ToolExecuteResult result = userTool.run(toolInput, null);
        log.info("查询结果: {}", result.getResult());
    }
    
    /**
     * 查询部门列表示例
     */
    private static void getDepartmentListExample(DingTalkToolFactory factory) {
        log.info("=== 钉钉部门列表查询示例 ===");
        
        DingTalkDepartmentTool deptTool = factory.createDepartmentTool();
        
        String toolInput = "{\n" +
                "  \"deptId\": 1\n" +
                "}";
        
        ToolExecuteResult result = deptTool.run(toolInput, null);
        log.info("查询结果: {}", result.getResult());
    }
    
    /**
     * 使用所有工具示例
     */
    private static void useAllToolsExample(DingTalkToolFactory factory) {
        log.info("=== 钉钉所有工具使用示例 ===");
        
        List<BaseTool> tools = factory.getAllTools();
        
        for (BaseTool tool : tools) {
            log.info("工具: {} - {}", tool.getName(), tool.getDescription());
        }
    }
}
