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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.slack.client.SlackClient;
import com.alibaba.langengine.slack.model.SlackMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


@Slf4j
@Data
public class SlackGetChannelHistoryTool extends DefaultTool {

    private SlackClient slackClient;

    public SlackGetChannelHistoryTool() {
        setName("SlackGetChannelHistory");
        setDescription("Get message history from a Slack channel. Parameters: channelId (required), limit (optional, default: 10), oldest (optional), latest (optional)");
    }

    public SlackGetChannelHistoryTool(SlackClient slackClient) {
        this();
        this.slackClient = slackClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("SlackGetChannelHistoryTool input: {}", toolInput);

        try {
            // 解析输入参数
            GetHistoryRequest request = JSON.parseObject(toolInput, GetHistoryRequest.class);
            
            if (request == null) {
                return new ToolExecuteResult("Invalid input format. Expected JSON with channelId field.", false);
            }

            if (StringUtils.isBlank(request.getChannelId())) {
                return new ToolExecuteResult("channelId is required and cannot be empty.", false);
            }

            // 设置默认值
            if (request.getLimit() <= 0) {
                request.setLimit(10);
            }

            // 初始化客户端（如果未设置）
            if (slackClient == null) {
                slackClient = new SlackClient();
            }

            // 获取历史消息
            List<SlackMessage> messages;
            if (StringUtils.isNotBlank(request.getOldest()) || StringUtils.isNotBlank(request.getLatest())) {
                messages = slackClient.getChannelHistory(
                    request.getChannelId(), 
                    request.getOldest(), 
                    request.getLatest(), 
                    request.getLimit()
                );
            } else {
                messages = slackClient.getChannelHistory(request.getChannelId(), request.getLimit());
            }

            // 返回结果
            String result = JSON.toJSONString(messages);
            log.info("SlackGetChannelHistoryTool success: found {} messages in channel {}", 
                    messages.size(), request.getChannelId());
            return new ToolExecuteResult(result, true);

        } catch (Exception e) {
            String error = "Error getting Slack channel history: " + e.getMessage();
            log.error("SlackGetChannelHistoryTool error: {}", error, e);
            return new ToolExecuteResult(error, false);
        }
    }

    /**
     * 获取频道历史消息请求参数
     */
    @Data
    public static class GetHistoryRequest {
        private String channelId;
        private int limit = 10;
        private String oldest;
        private String latest;
    }
}
