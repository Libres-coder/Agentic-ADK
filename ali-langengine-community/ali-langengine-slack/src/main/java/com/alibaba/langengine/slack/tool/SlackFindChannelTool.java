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


@Slf4j
@Data
public class SlackFindChannelTool extends DefaultTool {

    private SlackClient slackClient;

    public SlackFindChannelTool() {
        setName("SlackFindChannel");
        setDescription("Find a Slack channel by name. Parameters: channelName (required)");
    }

    public SlackFindChannelTool(SlackClient slackClient) {
        this();
        this.slackClient = slackClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("SlackFindChannelTool input: {}", toolInput);

        try {
            // 解析输入参数
            FindChannelRequest request = JSON.parseObject(toolInput, FindChannelRequest.class);
            
            if (request == null) {
                return new ToolExecuteResult("Invalid input format. Expected JSON with channelName field.", false);
            }

            if (StringUtils.isBlank(request.getChannelName())) {
                return new ToolExecuteResult("channelName is required and cannot be empty.", false);
            }

            // 初始化客户端（如果未设置）
            if (slackClient == null) {
                slackClient = new SlackClient();
            }

            // 查找频道
            SlackChannel channel = slackClient.findChannelByName(request.getChannelName());

            if (channel != null) {
                // 返回找到的频道信息
                String result = JSON.toJSONString(channel);
                log.info("SlackFindChannelTool success: found channel {}", request.getChannelName());
                return new ToolExecuteResult(result, true);
            } else {
                // 未找到频道
                String result = String.format("Channel '%s' not found", request.getChannelName());
                log.info("SlackFindChannelTool: channel {} not found", request.getChannelName());
                return new ToolExecuteResult(result, false);
            }

        } catch (Exception e) {
            String error = "Error finding Slack channel: " + e.getMessage();
            log.error("SlackFindChannelTool error: {}", error, e);
            return new ToolExecuteResult(error, false);
        }
    }

    /**
     * 查找频道请求参数
     */
    @Data
    public static class FindChannelRequest {
        private String channelName;
    }
}
