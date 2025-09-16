package com.alibaba.langengine.wecom.tools;

import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.wecom.WeComConfiguration;
import com.alibaba.langengine.wecom.client.WeComClient;
import com.alibaba.langengine.wecom.exception.WeComException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;


@EqualsAndHashCode(callSuper = true)
@Data
@Component
public class WeComMessageTool extends BaseWeComTool {

    private WeComClient client;
    private WeComConfiguration configuration;

    public WeComMessageTool() {
        setName("WeComMessageTool");
        setDescription("企业微信消息发送工具，支持发送JSON格式的消息参数：{\"toUser\":\"用户ID\",\"msgType\":\"text\",\"content\":\"消息内容\"}");
    }

    public WeComMessageTool(WeComConfiguration configuration) {
        this();
        this.configuration = configuration;
        this.client = new WeComClient(configuration);
    }

    public WeComMessageTool(WeComClient client) {
        this();
        this.client = client;
    }
    
    /**
     * 消息发送请求参数
     */
    public static class MessageRequest {
        private String toUser;      // 接收消息的用户ID
        private String toParty;     // 接收消息的部门ID
        private String toTag;       // 接收消息的标签ID
        private String msgType;     // 消息类型：text, markdown等
        private String content;     // 消息内容
        private String title;       // 消息标题（markdown类型时使用）
        
        // Getters and Setters
        public String getToUser() { return toUser; }
        public void setToUser(String toUser) { this.toUser = toUser; }
        
        public String getToParty() { return toParty; }
        public void setToParty(String toParty) { this.toParty = toParty; }
        
        public String getToTag() { return toTag; }
        public void setToTag(String toTag) { this.toTag = toTag; }
        
        public String getMsgType() { return msgType; }
        public void setMsgType(String msgType) { this.msgType = msgType; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        // 验证方法
        public void validate() throws WeComException {
            if (content == null || content.trim().isEmpty()) {
                throw new WeComException("消息内容不能为空");
            }
            
            if (toUser == null && toParty == null && toTag == null) {
                throw new WeComException("必须指定消息接收人（toUser）、部门（toParty）或标签（toTag）");
            }
            
            if (msgType == null || msgType.trim().isEmpty()) {
                msgType = "text"; // 默认文本类型
            }
            
            if (content.length() > 2048) {
                throw new WeComException("消息内容长度不能超过2048个字符");
            }
        }
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        try {
            // 解析输入参数
            MessageRequest request = parseInput(toolInput, MessageRequest.class);
            request.validate();
            
            // 发送消息
            boolean success = sendMessage(request);
            
            if (success) {
                return handleSuccess("消息发送成功");
            } else {
                return handleError(new WeComException("消息发送失败，未知原因"));
            }
            
        } catch (Exception e) {
            return handleError(e);
        }
    }
    
    @Override
    protected String getToolName() {
        return "WeComMessageTool";
    }

    /**
     * 发送消息的核心逻辑
     * 
     * @param request 消息请求
     * @return 是否发送成功
     * @throws WeComException 发送失败时抛出异常
     */
    private boolean sendMessage(MessageRequest request) throws WeComException {
        try {
            validateRequired(request.getContent(), "content");
            
            // 根据消息类型调用不同的发送方法
            switch (request.getMsgType().toLowerCase()) {
                case "text":
                    return sendTextMessage(request);
                case "markdown":
                    return sendMarkdownMessage(request);
                default:
                    throw new WeComException("不支持的消息类型: " + request.getMsgType());
            }
        } catch (WeComException e) {
            throw e;
        } catch (Exception e) {
            logger.error("发送企业微信消息失败", e);
            throw new WeComException("发送消息时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送文本消息
     */
    private boolean sendTextMessage(MessageRequest request) throws WeComException {
        if (request.getToUser() != null) {
            return client.sendTextMessage(request.getToUser(), request.getContent());
        } else {
            // TODO: 支持部门和标签发送
            throw new WeComException("暂不支持向部门或标签发送消息");
        }
    }
    
    /**
     * 发送Markdown消息
     */
    private boolean sendMarkdownMessage(MessageRequest request) throws WeComException {
        // TODO: 实现Markdown消息发送
        throw new WeComException("暂不支持Markdown消息发送");
    }
}