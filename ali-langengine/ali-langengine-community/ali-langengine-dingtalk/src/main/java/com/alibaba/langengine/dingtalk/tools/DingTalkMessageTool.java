package com.alibaba.langengine.dingtalk.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.dingtalk.DingTalkConfiguration;
import com.alibaba.langengine.dingtalk.service.DingTalkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class DingTalkMessageTool extends BaseTool {
    
    private final DingTalkService dingTalkService;
    
    public DingTalkMessageTool() {
        this(new DingTalkConfiguration());
    }
    
    public DingTalkMessageTool(DingTalkConfiguration config) {
        this.dingTalkService = new DingTalkService(config);
        setName("dingtalk_message");
        setHumanName("钉钉消息工具");
        setDescription("发送钉钉消息，支持文本消息和链接消息");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"userIds\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"接收消息的用户ID列表，多个用逗号分隔\"\n" +
                "    },\n" +
                "    \"messageType\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"text\", \"link\"],\n" +
                "      \"description\": \"消息类型\"\n" +
                "    },\n" +
                "    \"content\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"消息内容\"\n" +
                "    },\n" +
                "    \"title\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"链接消息标题（仅link类型需要）\"\n" +
                "    },\n" +
                "    \"messageUrl\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"链接地址（仅link类型需要）\"\n" +
                "    },\n" +
                "    \"picUrl\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"图片链接（仅link类型需要）\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"userIds\", \"messageType\", \"content\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            onToolStart(this, toolInput, executionContext);
            
            Map<String, Object> params = JSON.parseObject(toolInput, Map.class);
            String userIds = (String) params.get("userIds");
            String messageType = (String) params.get("messageType");
            String content = (String) params.get("content");
            
            if (StringUtils.isBlank(userIds) || StringUtils.isBlank(messageType) || StringUtils.isBlank(content)) {
                return new ToolExecuteResult("错误：用户ID、消息类型和内容不能为空");
            }
            
            String result;
            if ("text".equals(messageType)) {
                var response = dingTalkService.sendTextMessage(userIds, content);
                if (response.getErrcode() == 0) {
                    result = "文本消息发送成功，任务ID：" + response.getTaskId();
                } else {
                    result = "文本消息发送失败：" + response.getErrmsg();
                }
            } else if ("link".equals(messageType)) {
                String title = (String) params.get("title");
                String messageUrl = (String) params.get("messageUrl");
                String picUrl = (String) params.get("picUrl");
                
                if (StringUtils.isBlank(title) || StringUtils.isBlank(messageUrl)) {
                    return new ToolExecuteResult("错误：链接消息需要提供标题和链接地址");
                }
                
                var response = dingTalkService.sendLinkMessage(userIds, title, content, messageUrl, picUrl);
                if (response.getErrcode() == 0) {
                    result = "链接消息发送成功，任务ID：" + response.getTaskId();
                } else {
                    result = "链接消息发送失败：" + response.getErrmsg();
                }
            } else {
                return new ToolExecuteResult("错误：不支持的消息类型：" + messageType);
            }
            
            ToolExecuteResult toolResult = new ToolExecuteResult(result);
            onToolEnd(this, toolInput, toolResult, executionContext);
            return toolResult;
            
        } catch (Exception e) {
            String errorMsg = "钉钉消息发送失败：" + e.getMessage();
            log.error(errorMsg, e);
            ToolExecuteResult errorResult = new ToolExecuteResult(errorMsg);
            onToolError(this, e, executionContext);
            return errorResult;
        }
    }
}
