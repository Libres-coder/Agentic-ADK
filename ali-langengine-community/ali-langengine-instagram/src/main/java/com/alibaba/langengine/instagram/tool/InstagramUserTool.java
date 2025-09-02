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
package com.alibaba.langengine.instagram.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.instagram.sdk.InstagramClient;
import com.alibaba.langengine.instagram.sdk.InstagramException;
import com.alibaba.langengine.instagram.sdk.UserResponse;
import com.alibaba.langengine.instagram.sdk.MediaResponse;
import com.alibaba.langengine.instagram.sdk.MediaDetailResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class InstagramUserTool extends BaseTool {

    private InstagramClient instagramClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"userId\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"Instagram用户ID\"\n" +
            "\t\t},\n" +
            "\t\t\"action\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"操作类型：profile（获取用户信息）或 media（获取用户媒体）或 detail（获取媒体详情）\",\n" +
            "\t\t\t\"enum\": [\"profile\", \"media\", \"detail\"],\n" +
            "\t\t\t\"default\": \"profile\"\n" +
            "\t\t},\n" +
            "\t\t\"mediaId\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"当action为detail时，需要提供媒体ID\"\n" +
            "\t\t},\n" +
            "\t\t\"maxResults\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"获取媒体时的最大数量，默认10，最大100\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"userId\"]\n" +
            "}";

    public InstagramUserTool() {
        setName("instagram_user");
        setHumanName("Instagram用户工具");
        setDescription("获取Instagram用户信息、媒体内容或媒体详情");
        setParameters(PARAMETERS);
        this.instagramClient = new InstagramClient();
    }

    public InstagramUserTool(InstagramClient instagramClient) {
        setName("instagram_user");
        setHumanName("Instagram用户工具");
        setDescription("获取Instagram用户信息、媒体内容或媒体详情");
        setParameters(PARAMETERS);
        this.instagramClient = instagramClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("InstagramUserTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String userId = (String) toolInputMap.get("userId");
            String action = (String) toolInputMap.getOrDefault("action", "profile");
            String mediaId = (String) toolInputMap.get("mediaId");
            Integer maxResults = (Integer) toolInputMap.getOrDefault("maxResults", 10);

            if (userId == null || userId.trim().isEmpty()) {
                return new ToolExecuteResult("错误：用户ID不能为空");
            }

            if ("profile".equals(action)) {
                return getUserProfile(userId);
            } else if ("media".equals(action)) {
                return getUserMedia(userId, maxResults);
            } else if ("detail".equals(action)) {
                if (mediaId == null || mediaId.trim().isEmpty()) {
                    return new ToolExecuteResult("错误：获取媒体详情时需要提供媒体ID");
                }
                return getMediaDetail(mediaId);
            } else {
                return new ToolExecuteResult("错误：不支持的操作类型，请使用 'profile'、'media' 或 'detail'");
            }

        } catch (InstagramException e) {
            log.error("Instagram API调用失败", e);
            return new ToolExecuteResult("Instagram API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Instagram用户工具执行失败", e);
            return new ToolExecuteResult("操作失败: " + e.getMessage());
        }
    }

    private ToolExecuteResult getUserProfile(String userId) throws InstagramException {
        UserResponse response = instagramClient.getUserInfo(userId);
        
        StringBuilder result = new StringBuilder();
        result.append("用户信息:\n");
        result.append("用户ID: ").append(response.getId()).append("\n");
        result.append("用户名: ").append(response.getUsername()).append("\n");
        result.append("账户类型: ").append(response.getAccountType()).append("\n");
        result.append("媒体数量: ").append(response.getMediaCount()).append("\n");

        return new ToolExecuteResult(result.toString());
    }

    private ToolExecuteResult getUserMedia(String userId, int maxResults) throws InstagramException {
        MediaResponse response = instagramClient.getUserMedia(userId, maxResults);
        
        if (response.getData() == null || response.getData().isEmpty()) {
            return new ToolExecuteResult("用户没有发布媒体内容");
        }

        StringBuilder result = new StringBuilder();
        result.append("用户媒体内容:\n\n");
        
        for (MediaResponse.Media media : response.getData()) {
            result.append("媒体ID: ").append(media.getId()).append("\n");
            result.append("类型: ").append(media.getMediaType()).append("\n");
            result.append("描述: ").append(media.getCaption() != null ? media.getCaption() : "无").append("\n");
            result.append("发布时间: ").append(media.getTimestamp()).append("\n");
            result.append("链接: ").append(media.getPermalink()).append("\n");
            if (media.getMediaUrl() != null) {
                result.append("媒体链接: ").append(media.getMediaUrl()).append("\n");
            }
            result.append("---\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    private ToolExecuteResult getMediaDetail(String mediaId) throws InstagramException {
        MediaDetailResponse response = instagramClient.getMediaDetail(mediaId);
        
        StringBuilder result = new StringBuilder();
        result.append("媒体详情:\n");
        result.append("媒体ID: ").append(response.getId()).append("\n");
        result.append("类型: ").append(response.getMediaType()).append("\n");
        result.append("描述: ").append(response.getCaption() != null ? response.getCaption() : "无").append("\n");
        result.append("发布时间: ").append(response.getTimestamp()).append("\n");
        result.append("点赞数: ").append(response.getLikeCount()).append("\n");
        result.append("评论数: ").append(response.getCommentsCount()).append("\n");
        result.append("链接: ").append(response.getPermalink()).append("\n");
        if (response.getMediaUrl() != null) {
            result.append("媒体链接: ").append(response.getMediaUrl()).append("\n");
        }

        return new ToolExecuteResult(result.toString());
    }

    public InstagramClient getInstagramClient() {
        return instagramClient;
    }

    public void setInstagramClient(InstagramClient instagramClient) {
        this.instagramClient = instagramClient;
    }
}