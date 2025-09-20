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
public class TencentSmsTool extends BaseTool {
    
    private final SmsConfiguration config;
    
    public TencentSmsTool() {
        this(new SmsConfiguration());
    }
    
    public TencentSmsTool(SmsConfiguration config) {
        this.config = config;
        setName("tencent_sms");
        setHumanName("腾讯云短信工具");
        setDescription("发送腾讯云短信");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"phoneNumbers\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"接收短信的手机号码，多个用逗号分隔\"\n" +
                "    },\n" +
                "    \"templateId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"短信模板ID\"\n" +
                "    },\n" +
                "    \"templateParam\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"description\": \"短信模板参数数组\"\n" +
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
            String templateId = (String) params.getOrDefault("templateId", config.getTencentTemplateId());
            Object templateParam = params.get("templateParam");
            String signName = (String) params.getOrDefault("signName", config.getTencentSignName());
            
            if (StringUtils.isBlank(phoneNumbers)) {
                return new ToolExecuteResult("错误：手机号码不能为空");
            }
            
            if (StringUtils.isBlank(templateId)) {
                return new ToolExecuteResult("错误：短信模板ID不能为空");
            }
            
            if (StringUtils.isBlank(signName)) {
                return new ToolExecuteResult("错误：短信签名不能为空");
            }
            
            // 这里应该调用腾讯云短信SDK
            // 为了示例，我们返回模拟结果
            String result = String.format(
                "腾讯云短信发送成功\n" +
                "手机号码：%s\n" +
                "模板ID：%s\n" +
                "签名：%s\n" +
                "模板参数：%s",
                phoneNumbers, templateId, signName, templateParam
            );
            
            ToolExecuteResult toolResult = new ToolExecuteResult(result);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            String errorMsg = "腾讯云短信发送失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
