package com.alibaba.langengine.wework.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wework.WeWorkConfiguration;
import com.alibaba.langengine.wework.service.WeWorkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class WeWorkMessageTool extends BaseTool {
    
    private final WeWorkService weWorkService;
    
    public WeWorkMessageTool() {
        this(new WeWorkConfiguration());
    }
    
    public WeWorkMessageTool(WeWorkConfiguration config) {
        this.weWorkService = new WeWorkService(config);
        setName("wework_message");
        setHumanName("企业微信消息工具");
        setDescription("发送企业微信消息");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"touser\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"接收消息的用户ID列表，多个用|分隔\"\n" +
                "    },\n" +
                "    \"content\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"消息内容\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"touser\", \"content\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String touser = (String) params.get("touser");
            String content = (String) params.get("content");
            
            if (StringUtils.isBlank(touser) || StringUtils.isBlank(content)) {
                return new ToolExecuteResult("错误：接收用户和消息内容不能为空");
            }
            
            var response = weWorkService.sendTextMessage(touser, content);
            
            String result;
            if (response.getErrcode() == 0) {
                result = "消息发送成功，消息ID：" + response.getMsgid();
            } else {
                result = "消息发送失败：" + response.getErrmsg();
            }
            
            ToolExecuteResult toolResult = new ToolExecuteResult(result);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            String errorMsg = "企业微信消息发送失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
