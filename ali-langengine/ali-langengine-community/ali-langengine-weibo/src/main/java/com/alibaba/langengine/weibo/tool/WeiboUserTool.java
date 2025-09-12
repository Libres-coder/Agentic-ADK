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
package com.alibaba.langengine.weibo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.weibo.sdk.WeiboClient;
import com.alibaba.langengine.weibo.sdk.WeiboException;
import com.alibaba.langengine.weibo.sdk.UserResponse;
import com.alibaba.langengine.weibo.sdk.WeiboTimelineResponse;
import com.alibaba.langengine.weibo.sdk.HotTopicsResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class WeiboUserTool extends BaseTool {

    private WeiboClient weiboClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"uid\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"微博用户ID\"\n" +
            "\t\t},\n" +
            "\t\t\"action\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"操作类型：profile（获取用户信息）、timeline（获取用户时间线）或 hot（获取热门话题）\",\n" +
            "\t\t\t\"enum\": [\"profile\", \"timeline\", \"hot\"],\n" +
            "\t\t\t\"default\": \"profile\"\n" +
            "\t\t},\n" +
            "\t\t\"maxResults\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"获取时间线或热门话题时的最大数量，默认10\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"action\"]\n" +
            "}";

    public WeiboUserTool() {
        setName("weibo_user");
        setHumanName("微博用户工具");
        setDescription("获取微博用户信息、时间线或热门话题");
        setParameters(PARAMETERS);
        this.weiboClient = new WeiboClient();
    }

    public WeiboUserTool(WeiboClient weiboClient) {
        setName("weibo_user");
        setHumanName("微博用户工具");
        setDescription("获取微博用户信息、时间线或热门话题");
        setParameters(PARAMETERS);
        this.weiboClient = weiboClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("WeiboUserTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String uid = (String) toolInputMap.get("uid");
            String action = (String) toolInputMap.getOrDefault("action", "profile");
            Integer maxResults = (Integer) toolInputMap.getOrDefault("maxResults", 10);

            if ("profile".equals(action)) {
                if (uid == null || uid.trim().isEmpty()) {
                    return new ToolExecuteResult("错误：获取用户信息时需要提供用户ID");
                }
                return getUserProfile(uid);
            } else if ("timeline".equals(action)) {
                if (uid == null || uid.trim().isEmpty()) {
                    return new ToolExecuteResult("错误：获取时间线时需要提供用户ID");
                }
                return getUserTimeline(uid, maxResults);
            } else if ("hot".equals(action)) {
                return getHotTopics(maxResults);
            } else {
                return new ToolExecuteResult("错误：不支持的操作类型，请使用 'profile'、'timeline' 或 'hot'");
            }

        } catch (WeiboException e) {
            log.error("微博 API调用失败", e);
            return new ToolExecuteResult("微博 API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("微博用户工具执行失败", e);
            return new ToolExecuteResult("操作失败: " + e.getMessage());
        }
    }

    private ToolExecuteResult getUserProfile(String uid) throws WeiboException {
        UserResponse response = weiboClient.getUserInfo(uid);
        
        StringBuilder result = new StringBuilder();
        result.append("用户信息:\n");
        result.append("用户ID: ").append(response.getId()).append("\n");
        result.append("昵称: ").append(response.getScreenName()).append("\n");
        result.append("姓名: ").append(response.getName()).append("\n");
        result.append("简介: ").append(response.getDescription() != null ? response.getDescription() : "无").append("\n");
        result.append("关注者: ").append(response.getFollowersCount()).append("\n");
        result.append("关注中: ").append(response.getFriendsCount()).append("\n");
        result.append("微博数: ").append(response.getStatusesCount()).append("\n");
        result.append("认证状态: ").append(response.isVerified() ? "已认证" : "未认证").append("\n");
        if (response.isVerified() && response.getVerifiedReason() != null) {
            result.append("认证原因: ").append(response.getVerifiedReason()).append("\n");
        }
        result.append("创建时间: ").append(response.getCreatedAt()).append("\n");
        result.append("位置: ").append(response.getLocation() != null ? response.getLocation() : "无").append("\n");
        result.append("性别: ").append(response.getGender() != null ? response.getGender() : "无").append("\n");

        return new ToolExecuteResult(result.toString());
    }

    private ToolExecuteResult getUserTimeline(String uid, int maxResults) throws WeiboException {
        WeiboTimelineResponse response = weiboClient.getUserTimeline(uid, maxResults);
        
        if (response.getStatuses() == null || response.getStatuses().isEmpty()) {
            return new ToolExecuteResult("用户没有发布微博");
        }

        StringBuilder result = new StringBuilder();
        result.append("用户微博时间线:\n\n");
        
        for (WeiboTimelineResponse.Status status : response.getStatuses()) {
            result.append("微博ID: ").append(status.getId()).append("\n");
            result.append("内容: ").append(status.getText()).append("\n");
            result.append("发布时间: ").append(status.getCreatedAt()).append("\n");
            result.append("来源: ").append(status.getSource()).append("\n");
            result.append("互动数据: 转发(").append(status.getRepostsCount())
                  .append(") 评论(").append(status.getCommentsCount())
                  .append(") 点赞(").append(status.getAttitudesCount()).append(")\n");
            result.append("---\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    private ToolExecuteResult getHotTopics(int maxResults) throws WeiboException {
        HotTopicsResponse response = weiboClient.getHotTopics(maxResults);
        
        if (response.getTrends() == null || response.getTrends().isEmpty()) {
            return new ToolExecuteResult("暂无热门话题");
        }

        StringBuilder result = new StringBuilder();
        result.append("热门话题:\n\n");
        
        for (HotTopicsResponse.Trend trend : response.getTrends()) {
            result.append("话题: ").append(trend.getName()).append("\n");
            result.append("搜索量: ").append(trend.getAmount()).append("\n");
            result.append("变化: ").append(trend.getDelta() > 0 ? "+" : "").append(trend.getDelta()).append("\n");
            result.append("---\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    public WeiboClient getWeiboClient() {
        return weiboClient;
    }

    public void setWeiboClient(WeiboClient weiboClient) {
        this.weiboClient = weiboClient;
    }
}