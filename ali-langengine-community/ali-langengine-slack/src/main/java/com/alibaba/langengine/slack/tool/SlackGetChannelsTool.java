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
import com.alibaba.langengine.slack.model.SlackChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@Data
public class SlackGetChannelsTool extends DefaultTool {

    private SlackClient slackClient;

    public SlackGetChannelsTool() {
        setName("SlackGetChannels");
        setDescription("Get list of Slack channels. Parameters: includePrivate (optional, boolean, default: false)");
    }

    public SlackGetChannelsTool(SlackClient slackClient) {
        this();
        this.slackClient = slackClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("SlackGetChannelsTool input: {}", toolInput);

        try {
            // 解析输入参数
            GetChannelsRequest request = null;
            if (toolInput != null && !toolInput.trim().isEmpty()) {
                try {
                    request = JSON.parseObject(toolInput, GetChannelsRequest.class);
                } catch (Exception e) {
                    // 如果解析失败，使用默认参数
                    request = new GetChannelsRequest();
                }
            } else {
                request = new GetChannelsRequest();
            }

            // 初始化客户端（如果未设置）
            if (slackClient == null) {
                slackClient = new SlackClient();
            }

            // 获取频道列表
            List<SlackChannel> channels = slackClient.getChannels();
            
            // 如果需要包含私有频道
            if (request.isIncludePrivate()) {
                List<SlackChannel> privateChannels = slackClient.getPrivateChannels();
                channels.addAll(privateChannels);
            }

            // 返回结果
            String result = JSON.toJSONString(channels);
            log.info("SlackGetChannelsTool success: found {} channels", channels.size());
            return new ToolExecuteResult(result, true);

        } catch (Exception e) {
            String error = "Error getting Slack channels: " + e.getMessage();
            log.error("SlackGetChannelsTool error: {}", error, e);
            return new ToolExecuteResult(error, false);
        }
    }

    /**
     * 获取频道列表请求参数
     */
    @Data
    public static class GetChannelsRequest {
        private boolean includePrivate = false;
    }
}
