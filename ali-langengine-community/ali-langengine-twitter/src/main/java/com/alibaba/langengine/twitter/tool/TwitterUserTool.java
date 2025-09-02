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
package com.alibaba.langengine.twitter.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.twitter.sdk.TwitterClient;
import com.alibaba.langengine.twitter.sdk.TwitterException;
import com.alibaba.langengine.twitter.sdk.UserResponse;
import com.alibaba.langengine.twitter.sdk.TweetTimelineResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TwitterUserTool extends BaseTool {

    private TwitterClient twitterClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"username\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"Twitter用户名（不包含@符号）\"\n" +
            "\t\t},\n" +
            "\t\t\"action\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"操作类型：profile（获取用户信息）或 timeline（获取用户时间线）\",\n" +
            "\t\t\t\"enum\": [\"profile\", \"timeline\"],\n" +
            "\t\t\t\"default\": \"profile\"\n" +
            "\t\t},\n" +
            "\t\t\"maxResults\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"获取时间线时的最大推文数，默认10，最大100\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"username\"]\n" +
            "}";

    public TwitterUserTool() {
        setName("twitter_user");
        setHumanName("Twitter用户工具");
        setDescription("获取Twitter用户的个人信息或时间线推文");
        setParameters(PARAMETERS);
        this.twitterClient = new TwitterClient();
    }

    public TwitterUserTool(TwitterClient twitterClient) {
        setName("twitter_user");
        setHumanName("Twitter用户工具");
        setDescription("获取Twitter用户的个人信息或时间线推文");
        setParameters(PARAMETERS);
        this.twitterClient = twitterClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("TwitterUserTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String username = (String) toolInputMap.get("username");
            String action = (String) toolInputMap.getOrDefault("action", "profile");
            Integer maxResults = (Integer) toolInputMap.getOrDefault("maxResults", 10);

            if (username == null || username.trim().isEmpty()) {
                return new ToolExecuteResult("错误：用户名不能为空");
            }

            // 移除@符号如果存在
            if (username.startsWith("@")) {
                username = username.substring(1);
            }

            if ("profile".equals(action)) {
                return getUserProfile(username);
            } else if ("timeline".equals(action)) {
                return getUserTimeline(username, maxResults);
            } else {
                return new ToolExecuteResult("错误：不支持的操作类型，请使用 'profile' 或 'timeline'");
            }

        } catch (TwitterException e) {
            log.error("Twitter API调用失败", e);
            return new ToolExecuteResult("Twitter API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Twitter用户工具执行失败", e);
            return new ToolExecuteResult("操作失败: " + e.getMessage());
        }
    }

    private ToolExecuteResult getUserProfile(String username) throws TwitterException {
        UserResponse response = twitterClient.getUserByUsername(username);
        
        if (response.getData() == null) {
            return new ToolExecuteResult("未找到用户: @" + username);
        }

        UserResponse.User user = response.getData();
        StringBuilder result = new StringBuilder();
        result.append("用户信息:\n");
        result.append("用户名: @").append(user.getUsername()).append("\n");
        result.append("显示名: ").append(user.getName()).append("\n");
        result.append("用户ID: ").append(user.getId()).append("\n");
        result.append("简介: ").append(user.getDescription() != null ? user.getDescription() : "无").append("\n");
        result.append("创建时间: ").append(user.getCreatedAt()).append("\n");
        result.append("认证状态: ").append(user.isVerified() ? "已认证" : "未认证").append("\n");
        
        if (user.getPublicMetrics() != null) {
            result.append("关注者: ").append(user.getPublicMetrics().getFollowersCount()).append("\n");
            result.append("关注中: ").append(user.getPublicMetrics().getFollowingCount()).append("\n");
            result.append("推文数: ").append(user.getPublicMetrics().getTweetCount()).append("\n");
            result.append("列表数: ").append(user.getPublicMetrics().getListedCount()).append("\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    private ToolExecuteResult getUserTimeline(String username, int maxResults) throws TwitterException {
        // 首先获取用户信息以获取用户ID
        UserResponse userResponse = twitterClient.getUserByUsername(username);
        if (userResponse.getData() == null) {
            return new ToolExecuteResult("未找到用户: @" + username);
        }

        String userId = userResponse.getData().getId();
        TweetTimelineResponse response = twitterClient.getUserTimeline(userId, maxResults);
        
        if (response.getData() == null || response.getData().isEmpty()) {
            return new ToolExecuteResult("用户 @" + username + " 没有发布推文");
        }

        StringBuilder result = new StringBuilder();
        result.append("用户 @").append(username).append(" 的最新推文:\n\n");
        
        for (TweetTimelineResponse.Tweet tweet : response.getData()) {
            result.append("推文ID: ").append(tweet.getId()).append("\n");
            result.append("内容: ").append(tweet.getText()).append("\n");
            result.append("发布时间: ").append(tweet.getCreatedAt()).append("\n");
            if (tweet.getPublicMetrics() != null) {
                result.append("互动数据: 转发(").append(tweet.getPublicMetrics().getRetweetCount())
                      .append(") 点赞(").append(tweet.getPublicMetrics().getLikeCount())
                      .append(") 回复(").append(tweet.getPublicMetrics().getReplyCount()).append(")\n");
            }
            result.append("---\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    public TwitterClient getTwitterClient() {
        return twitterClient;
    }

    public void setTwitterClient(TwitterClient twitterClient) {
        this.twitterClient = twitterClient;
    }
}