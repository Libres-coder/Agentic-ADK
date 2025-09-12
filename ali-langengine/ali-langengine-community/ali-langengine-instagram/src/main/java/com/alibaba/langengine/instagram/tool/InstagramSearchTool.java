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
import com.alibaba.langengine.instagram.sdk.HashtagMediaResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class InstagramSearchTool extends BaseTool {

    private InstagramClient instagramClient;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"hashtag\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"搜索的标签名（不包含#符号）\"\n" +
            "\t\t},\n" +
            "\t\t\"maxResults\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"最大返回结果数，默认10，最大100\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"hashtag\"]\n" +
            "}";

    public InstagramSearchTool() {
        setName("instagram_search");
        setHumanName("Instagram搜索工具");
        setDescription("搜索Instagram上指定标签的媒体内容");
        setParameters(PARAMETERS);
        this.instagramClient = new InstagramClient();
    }

    public InstagramSearchTool(InstagramClient instagramClient) {
        setName("instagram_search");
        setHumanName("Instagram搜索工具");
        setDescription("搜索Instagram上指定标签的媒体内容");
        setParameters(PARAMETERS);
        this.instagramClient = instagramClient;
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("InstagramSearchTool toolInput:" + toolInput);

        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String hashtag = (String) toolInputMap.get("hashtag");
            Integer maxResults = (Integer) toolInputMap.getOrDefault("maxResults", 10);

            if (hashtag == null || hashtag.trim().isEmpty()) {
                return new ToolExecuteResult("错误：标签名不能为空");
            }

            // 移除#符号如果存在
            if (hashtag.startsWith("#")) {
                hashtag = hashtag.substring(1);
            }

            HashtagMediaResponse response = instagramClient.getHashtagMedia(hashtag, maxResults);
            
            if (response.getData() == null || response.getData().isEmpty()) {
                return new ToolExecuteResult("未找到标签 #" + hashtag + " 的相关内容");
            }

            StringBuilder result = new StringBuilder();
            result.append("找到标签 #").append(hashtag).append(" 的 ").append(response.getData().size()).append(" 条内容：\n\n");
            
            for (HashtagMediaResponse.Media media : response.getData()) {
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

        } catch (InstagramException e) {
            log.error("Instagram API调用失败", e);
            return new ToolExecuteResult("Instagram API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Instagram搜索工具执行失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage());
        }
    }

    public InstagramClient getInstagramClient() {
        return instagramClient;
    }

    public void setInstagramClient(InstagramClient instagramClient) {
        this.instagramClient = instagramClient;
    }
}