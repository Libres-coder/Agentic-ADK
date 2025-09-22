package com.alibaba.langengine.sms.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.sms.SmsConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class AliyunSmsTool extends BaseTool {
    
    private final SmsConfiguration config;
    
    public AliyunSmsTool() {
        this(new SmsConfiguration());
    }
    
    public AliyunSmsTool(SmsConfiguration config) {
        this.config = config;
        setName("aliyun_sms");
        setHumanName("阿里云短信工具");
        setDescription("发送阿里云短信");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"phoneNumbers\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"接收短信的手机号码，多个用逗号分隔\"\n" +
                "    },\n" +
                "    \"templateCode\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"短信模板代码\"\n" +
                "    },\n" +
                "    \"templateParam\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"短信模板参数，JSON格式\"\n" +
                "    },\n" +
                "    \"signName\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"短信签名\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"phoneNumbers\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String phoneNumbers = (String) params.get("phoneNumbers");
            String templateCode = (String) params.getOrDefault("templateCode", config.getAliyunTemplateCode());
            String templateParam = (String) params.get("templateParam");
            String signName = (String) params.getOrDefault("signName", config.getAliyunSignName());
            
            if (StringUtils.isBlank(phoneNumbers)) {
                return new ToolExecuteResult("错误：手机号码不能为空");
            }
            
            if (StringUtils.isBlank(templateCode)) {
                return new ToolExecuteResult("错误：短信模板代码不能为空");
            }
            
            if (StringUtils.isBlank(signName)) {
                return new ToolExecuteResult("错误：短信签名不能为空");
            }
            
            // 这里应该调用阿里云短信SDK
            // 为了示例，我们返回模拟结果
            String result = String.format(
                "阿里云短信发送成功\n" +
                "手机号码：%s\n" +
                "模板代码：%s\n" +
                "签名：%s\n" +
                "模板参数：%s",
                phoneNumbers, templateCode, signName, templateParam
            );
            
            ToolExecuteResult toolResult = new ToolExecuteResult(result);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            String errorMsg = "阿里云短信发送失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
