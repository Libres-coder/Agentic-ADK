package com.alibaba.langengine.sms.examples;

import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.sms.SmsConfiguration;
import com.alibaba.langengine.sms.SmsToolFactory;
import com.alibaba.langengine.sms.tools.AliyunSmsTool;
import com.alibaba.langengine.sms.tools.HuaweiSmsTool;
import com.alibaba.langengine.sms.tools.TencentSmsTool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SmsToolsExample {
    
    public static void main(String[] args) {
        // 创建配置
        SmsConfiguration config = new SmsConfiguration();
        
        // 创建工具工厂
        SmsToolFactory factory = new SmsToolFactory(config);
        
        // 示例1: 发送阿里云短信
        sendAliyunSmsExample(factory);
        
        // 示例2: 发送腾讯云短信
        sendTencentSmsExample(factory);
        
        // 示例3: 发送华为云短信
        sendHuaweiSmsExample(factory);
        
        // 示例4: 使用所有工具
        useAllToolsExample(factory);
    }
    
    /**
     * 发送阿里云短信示例
     */
    private static void sendAliyunSmsExample(SmsToolFactory factory) {
        log.info("=== 阿里云短信发送示例 ===");
        
        AliyunSmsTool smsTool = factory.createAliyunSmsTool();
        
        String toolInput = "{\n" +
                "  \"phoneNumbers\": \"13800138000\",\n" +
                "  \"templateCode\": \"SMS_123456789\",\n" +
                "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                "  \"signName\": \"阿里云\"\n" +
                "}";
        
        ToolExecuteResult result = smsTool.run(toolInput, null);
        log.info("发送结果: {}", result.getResult());
    }
    
    /**
     * 发送腾讯云短信示例
     */
    private static void sendTencentSmsExample(SmsToolFactory factory) {
        log.info("=== 腾讯云短信发送示例 ===");
        
        TencentSmsTool smsTool = factory.createTencentSmsTool();
        
        String toolInput = "{\n" +
                "  \"phoneNumbers\": \"13800138000\",\n" +
                "  \"templateId\": \"123456\",\n" +
                "  \"templateParam\": [\"123456\"],\n" +
                "  \"signName\": \"腾讯云\"\n" +
                "}";
        
        ToolExecuteResult result = smsTool.run(toolInput, null);
        log.info("发送结果: {}", result.getResult());
    }
    
    /**
     * 发送华为云短信示例
     */
    private static void sendHuaweiSmsExample(SmsToolFactory factory) {
        log.info("=== 华为云短信发送示例 ===");
        
        HuaweiSmsTool smsTool = factory.createHuaweiSmsTool();
        
        String toolInput = "{\n" +
                "  \"phoneNumbers\": \"13800138000\",\n" +
                "  \"templateId\": \"123456\",\n" +
                "  \"templateParam\": \"{\\\"code\\\":\\\"123456\\\"}\",\n" +
                "  \"signName\": \"华为云\"\n" +
                "}";
        
        ToolExecuteResult result = smsTool.run(toolInput, null);
        log.info("发送结果: {}", result.getResult());
    }
    
    /**
     * 使用所有工具示例
     */
    private static void useAllToolsExample(SmsToolFactory factory) {
        log.info("=== 短信服务所有工具使用示例 ===");
        
        List<BaseTool> tools = factory.getAllTools();
        
        for (BaseTool tool : tools) {
            log.info("工具: {} - {}", tool.getName(), tool.getDescription());
        }
    }
}
