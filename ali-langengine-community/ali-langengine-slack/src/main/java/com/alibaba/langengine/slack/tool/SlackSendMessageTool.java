/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.slack.tool;

import com.alibaba.fastjson2.JSON;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.slack.client.SlackClient;
import com.alibaba.langengine.slack.model.SlackChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@Slf4j
@Data
public class SlackSendMessageTool extends DefaultTool {

    private SlackClient slackClient;

    public SlackSendMessageTool() {
        setName("SlackSendMessage");
        setDescription("Send a message to a Slack channel. Parameters: channelId (required), message (required), threadTs (optional)");
    }

    public SlackSendMessageTool(SlackClient slackClient) {
        this();
        this.slackClient = slackClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("SlackSendMessageTool input: {}", toolInput);

        try {
            // 解析输入参数
            SendMessageRequest request = JSON.parseObject(toolInput, SendMessageRequest.class);
            
            if (request == null) {
                return new ToolExecuteResult("Invalid input format. Expected JSON with channelId and message fields.", false);
            }

            if (StringUtils.isBlank(request.getChannelId())) {
                return new ToolExecuteResult("channelId is required and cannot be empty.", false);
            }

            if (StringUtils.isBlank(request.getMessage())) {
                return new ToolExecuteResult("message is required and cannot be empty.", false);
            }

            // 初始化客户端（如果未设置）
            if (slackClient == null) {
                slackClient = new SlackClient();
            }

            // 发送消息
            boolean success = slackClient.sendMessage(
                request.getChannelId(), 
                request.getMessage(), 
                request.getThreadTs()
            );

            if (success) {
                String result = String.format("Message sent successfully to channel %s", request.getChannelId());
                log.info("SlackSendMessageTool success: {}", result);
                return new ToolExecuteResult(result, true);
            } else {
                String error = String.format("Failed to send message to channel %s", request.getChannelId());
                log.error("SlackSendMessageTool error: {}", error);
                return new ToolExecuteResult(error, false);
            }

        } catch (Exception e) {
            String error = "Error sending Slack message: " + e.getMessage();
            log.error("SlackSendMessageTool error: {}", error, e);
            return new ToolExecuteResult(error, false);
        }
    }

    /**
     * 发送消息请求参数
     */
    @Data
    public static class SendMessageRequest {
        private String channelId;
        private String message;
        private String threadTs;
    }
}
