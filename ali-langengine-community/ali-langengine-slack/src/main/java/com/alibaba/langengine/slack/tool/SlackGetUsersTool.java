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
import com.alibaba.langengine.slack.model.SlackUser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@Data
public class SlackGetUsersTool extends DefaultTool {

    private SlackClient slackClient;

    public SlackGetUsersTool() {
        setName("SlackGetUsers");
        setDescription("Get list of Slack users in the workspace. No parameters required.");
    }

    public SlackGetUsersTool(SlackClient slackClient) {
        this();
        this.slackClient = slackClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("SlackGetUsersTool input: {}", toolInput);

        try {
            // 初始化客户端（如果未设置）
            if (slackClient == null) {
                slackClient = new SlackClient();
            }

            // 获取用户列表
            List<SlackUser> users = slackClient.getUsers();

            // 返回结果
            String result = JSON.toJSONString(users);
            log.info("SlackGetUsersTool success: found {} users", users.size());
            return new ToolExecuteResult(result, true);

        } catch (Exception e) {
            String error = "Error getting Slack users: " + e.getMessage();
            log.error("SlackGetUsersTool error: {}", error, e);
            return new ToolExecuteResult(error, false);
        }
    }
}
